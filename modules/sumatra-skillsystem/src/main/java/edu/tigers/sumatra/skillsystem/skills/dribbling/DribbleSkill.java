/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.dribbling;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.util.BotLastTouchedBallCalculator;
import lombok.Setter;

import java.awt.Color;


/**
 * A skill to move the ball on a straight line from a to b
 * Doesn't really work in simulation, needs to be trimmed in real live
 * 1 - Trim kickSpeedDecelerate by setting kickSpeed to 0. The target is to let the bot just chip far enough to avoid
 * dribble fouls
 * 2 - Trim kickSpeed by setting the decelerate Distance low. Take care to not crash the bot in the wall
 * 3 - Trim decelerate distance
 * Other parameters should be fine
 */
public class DribbleSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.1", comment = "[rad] The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.1;

	@Configurable(defValue = "2.5", comment = "[m/s] kickSpeed to dribble in normal behaviour")
	private static double kickSpeed = 2.5;
	@Configurable(defValue = "1.85", comment = "[m/s] kickSpeed to avoid dribble foul in decelerateStage")
	private static double kickSpeedDecelerate = 1.85;
	@Configurable(defValue = "0.7", comment = "[m/s] The target velocity difference between bot and ball to aim for when trying catch up ball")
	private static double catchUpBallTargetVelDiff = 0.7;

	@Configurable(defValue = "3250.0", comment = "[mm] decelerate distance")
	private static double decelerateDistance = 3250.0;
	@Configurable(defValue = "20.0", comment = "[mm] The max margin to the ball for destinations")
	private static double maxMarginToBall = 20.0;
	@Configurable(defValue = "800.0", comment = "[mm] The max dribbling distance for the skill, should be smaller as the distance from the rules")
	private static double maxDribblingLength = 800.0;

	@Configurable(defValue = "1.0", comment = "[s] The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 1.0;
	@Configurable(defValue = "0.0", comment = "[s] Delay before kick to synchronize dribble rpm to ball rpm")
	private static double delayBeforeKick = 0.0;
	@Configurable(defValue = "0.5", comment = "[s] Time until the ball will be considered not correctly accepted")
	private static double correctlyAcceptBallTime = 0.20;
	@Configurable(defValue = "0.2", comment = "[s] Time to chip the ball a small distance before the bot switches into Follow-Mode")
	private static double timeToQuickAvoidDribbleFoul = 0.2;
	@Configurable(defValue = "0.7", comment = "[s] Max lookahead for Push and Kick States")
	private static double maxLookahead = 0.7;

	private final PositionValidator positionValidator = new PositionValidator();
	private final DoubleChargingValue lookaheadChargingValue = new DoubleChargingValue(
			0,
			0.6,
			-0.9,
			0,
			maxLookahead);
	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);
	private final TimestampTimer timerControllingBall = new TimestampTimer(delayBeforeKick);
	private final TimestampTimer timerTouchingBall = new TimestampTimer(correctlyAcceptBallTime);
	private final TimestampTimer timerQuickDribbleFoulAvoidance = new TimestampTimer(timeToQuickAvoidDribbleFoul);
	private final BotLastTouchedBallCalculator botLastTouchedBallCalculator = new BotLastTouchedBallCalculator();

	@Setter
	private DynamicPosition targetPos;
	@Setter
	private double safeDistance = 0;

	private IVector2 touchingStartPosition;
	private EBallContactLevel ballContactLevel = EBallContactLevel.NONE;


	public DribbleSkill()
	{
		IState kickState = new KickState();
		IState pushState = new PushState();
		IState followState = new FollowState();

		addTransition(EEvent.DRIBBLE_FOUL_QUICK_AVOIDANCE_FAILED, followState);
		addTransition(EEvent.DRIBBLE_FOUL_PENDING, kickState);
		addTransition(EEvent.LOST_BALL_CONTROL, followState);
		addTransition(EEvent.CLOSE_TO_TARGET, pushState);
		addTransition(EEvent.FOLLOWED_TO_PUSH, pushState);
		addTransition(EEvent.FOLLOWED_TO_KICK, kickState);

		setInitialState(followState);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();

		targetAngleReachedChecker.setOuterAngleDiffTolerance(roughAngleTolerance);

		EBallContactLevel lastBallContactLevel = ballContactLevel;
		ballContactLevel = ballContactLevel();
		if (lastBallContactLevel == EBallContactLevel.NONE && ballContactLevel != EBallContactLevel.NONE)
		{
			touchingStartPosition = getBallPos();
		} else if (ballContactLevel == EBallContactLevel.NONE)
		{
			touchingStartPosition = null;
		}
		updateTimer();

		final IVector2 kickDir = targetPos.getPos().subtractNew(getBallPos());
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawablePoint(getBallPos(), Color.green));
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getBallPos(), kickDir.turnNew(roughAngleTolerance)),
						Color.green));
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getBallPos(), kickDir.turnNew(-roughAngleTolerance)),
						Color.green));
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL)
				.add(new DrawableCircle(Circle.createCircle(targetPos.getPos(), DribbleSkill.decelerateDistance),
						Color.RED));
	}


	private boolean isFocused()
	{
		double ball2TargetAngle = targetPos.getPos().subtractNew(getBallPos()).getAngle(getAngle());
		return isOrientationReached(ball2TargetAngle);
	}


	private boolean dribbleFoulPending()
	{
		return touchingStartPosition != null && touchingStartPosition.distanceTo(getBallPos()) > maxDribblingLength;
	}


	private boolean ballControlLost()
	{
		return timerTouchingBall.isTimeUp(timestamp()) && !timerControllingBall.isRunning();
	}


	private void updateTimer()
	{
		switch (ballContactLevel)
		{
			case TOUCHING ->
			{
				timerTouchingBall.update(timestamp());
				timerControllingBall.reset();
			}
			case CONTROL ->
			{
				timerTouchingBall.update(timestamp());
				timerControllingBall.update(timestamp());
			}
			default ->
			{
				timerTouchingBall.reset();
				timerControllingBall.reset();
				timerQuickDribbleFoulAvoidance.reset();
			}
		}
	}


	private EBallContactLevel ballContactLevel()
	{
		EBallContactLevel contactLevel = EBallContactLevel.NONE;

		if (getTBot().hasBallContact())
		{
			contactLevel = EBallContactLevel.CONTROL;
		} else if (isTouchingBall())
		{
			contactLevel = EBallContactLevel.TOUCHING;
		}

		return contactLevel;
	}


	private boolean isTouchingBall()
	{
		return botLastTouchedBallCalculator.currentlyTouchingBots(getWorldFrame()).stream()
				.anyMatch(e -> e.equals(getBotId()));
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
	}


	private long timestamp()
	{
		return getWorldFrame().getTimestamp();
	}


	private boolean isOrientationReached(final double targetOrientation)
	{
		targetAngleReachedChecker.update(targetOrientation, getOrientationFromFilter(), getWorldFrame().getTimestamp());
		return targetAngleReachedChecker.isReached();
	}


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}


	private IVector2 getBallPosByTime(final double lookahead)
	{
		return getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
	}


	private double getTargetOrientation()
	{
		return targetPos.getPos().subtractNew(getBallPos()).getAngle(0);
	}


	private double getMinMargin(final IVector2 dest, MinMarginChargeValue minMarginChargeValue)
	{
		double dist = dest.distanceTo(getPos());
		minMarginChargeValue.updateMinMargin(dist, getWorldFrame().getTimestamp());
		return minMarginChargeValue.getMinMargin();
	}


	private IVector2 calculateDestinationAroundBall(final double lookahead, final double dist2Ball,
			final double maxMarginToBall,
			final double minMarginToBall)
	{
		return AroundBallCalc
				.aroundBall()
				.withBallPos(getBallPosByTime(lookahead))
				.withTBot(getTBot())
				.withDestination(getDestination(dist2Ball))
				.withMaxMargin(maxMarginToBall)
				.withMinMargin(minMarginToBall)
				.build()
				.getAroundBallDest();
	}


	private IVector2 getDestination(final double dist2Ball)
	{
		return LineMath.stepAlongLine(getBallPos(), targetPos.getPos(), -getDistance(dist2Ball));
	}


	private double getDistance(final double dist2Ball)
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball;
	}


	private IVector2 validatePosition(final IVector2 position)
	{
		IVector2 validatedPosition = position;
		positionValidator.update(getWorldFrame(), getMoveCon());
		validatedPosition = positionValidator.movePosInsideField(validatedPosition);
		validatedPosition = positionValidator.movePosOutOfPenAreaWrtBall(validatedPosition);
		return validatedPosition;
	}


	private void drawStateShapes(final double dist2Ball, IVector2 dest)
	{
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawableAnnotation(getPos(),
				String.format("%.2f | %.2f", dist2Ball, dest.distanceTo(getPos()))).withOffset(Vector2.fromY(300)));
		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL)
				.add(new DrawableLine(getBallPos(), targetPos.getPos(), getBotId().getTeamColor().getColor()));
	}


	private double calcLookahead()
	{
		final double botBallSpeedDiff = getTBot().getVel().subtractNew(getBall().getVel()).getLength2();
		final double hysteresis = 0.05;
		if (botBallSpeedDiff > catchUpBallTargetVelDiff + hysteresis || isTouchingBall())
		{
			lookaheadChargingValue.setChargeMode(DoubleChargingValue.ChargeMode.DECREASE);
		} else if (botBallSpeedDiff < catchUpBallTargetVelDiff - hysteresis)
		{
			lookaheadChargingValue.setChargeMode(DoubleChargingValue.ChargeMode.INCREASE);
		} else
		{
			lookaheadChargingValue.setChargeMode(DoubleChargingValue.ChargeMode.STALL);
		}
		lookaheadChargingValue.update(getWorldFrame().getTimestamp());
		return lookaheadChargingValue.getValue();
	}


	private boolean isNearBall()
	{
		return getBallPos().distanceTo(getTBot().getBotKickerPos()) < (Geometry.getBallRadius() + 10);
	}


	private enum EBallContactLevel
	{
		NONE,
		TOUCHING,
		CONTROL
	}

	private enum EEvent implements IEvent
	{
		DRIBBLE_FOUL_QUICK_AVOIDANCE_FAILED,
		DRIBBLE_FOUL_PENDING,
		LOST_BALL_CONTROL,
		CLOSE_TO_TARGET,
		FOLLOWED_TO_PUSH,
		FOLLOWED_TO_KICK
	}


	private class KickState extends AState
	{
		private MinMarginChargeValue minMarginChargeValue;
		private double dist2Ball = 0;


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			dist2Ball = 20;
			getMoveCon().setBallObstacle(false);

			minMarginChargeValue = MinMarginChargeValue.aMinMargin()
					.withDefaultValue(20)
					.withInitValue(isNearBall() ? -10 : 20)
					.withLimit(-50)
					.withChargeRate(-200)
					.withLowerThreshold(70)
					.withUpperThreshold(90)
					.build();

			if (dribbleFoulPending())
			{
				tryToArmKick(kickSpeedDecelerate);
			} else
			{
				setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
			}
		}


		@Override
		public void doUpdate()
		{
			final double minMarginToBall = dist2Ball;
			IVector2 dest = calculateDestinationAroundBall(calcLookahead(), dist2Ball, maxMarginToBall,
					minMarginToBall);
			dist2Ball = getMinMargin(dest, minMarginChargeValue);

			dest = validatePosition(dest);
			updateDestination(dest);
			updateTargetAngle(getTargetOrientation());

			super.doUpdate();

			drawStateShapes(dist2Ball, dest);

			if (dribbleFoulPending())
			{
				timerQuickDribbleFoulAvoidance.update(timestamp());
				if (timerQuickDribbleFoulAvoidance.isTimeUp(timestamp()))
				{
					triggerEvent(EEvent.DRIBBLE_FOUL_QUICK_AVOIDANCE_FAILED);
				} else
				{
					tryToArmKick(kickSpeedDecelerate);
				}
			} else if (ballControlLost())
			{
				triggerEvent(EEvent.LOST_BALL_CONTROL);
			} else if (safeDistance < decelerateDistance)
			{
				triggerEvent(EEvent.CLOSE_TO_TARGET);
			} else if (timerControllingBall.isTimeUp(timestamp()))
			{
				tryToArmKick(kickSpeed);
			}
		}


		private void tryToArmKick(double kickSpeed)
		{
			if (isFocused())
			{
				setKickParams(KickParams.chip(kickSpeed).withDribblerMode(EDribblerMode.DEFAULT));
			}
		}
	}

	private class PushState extends AState
	{
		private MinMarginChargeValue minMarginChargeValue;
		private double dist2Ball = 0;


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			dist2Ball = 20;
			getMoveCon().setBallObstacle(false);

			minMarginChargeValue = MinMarginChargeValue.aMinMargin()
					.withDefaultValue(10)
					.withInitValue(isNearBall() ? -10 : 100)
					.withLimit(-70)
					.withChargeRate(-300)
					.withLowerThreshold(170)
					.withUpperThreshold(250)
					.build();

			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
		}


		@Override
		public void doUpdate()
		{
			final double minMarginToBall = dist2Ball;
			IVector2 dest = calculateDestinationAroundBall(calcLookahead(), dist2Ball, maxMarginToBall,
					minMarginToBall);
			dist2Ball = getMinMargin(dest, minMarginChargeValue);

			dest = validatePosition(dest);

			double maxVel = getBall().getStraightConsultant()
					.getInitVelForDist(targetPos.getPos().distanceTo(getBallPos()), 0.25);
			maxVel = getTBot().hasBallContact() ? Math.max(maxVel, getTBot().getVel().getLength() - 0.1)
					: getTBot().getMoveConstraints().getVelMax();

			getMoveConstraints().setVelMax(maxVel);
			updateDestination(dest);
			updateTargetAngle(getTargetOrientation());

			super.doUpdate();

			drawStateShapes(dist2Ball, dest);

			if (dribbleFoulPending())
			{
				timerQuickDribbleFoulAvoidance.update(timestamp());
				triggerEvent(EEvent.DRIBBLE_FOUL_PENDING);
			} else if (ballControlLost())
			{
				triggerEvent(EEvent.LOST_BALL_CONTROL);
			}

		}
	}

	private class FollowState extends AState
	{
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(true);

			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));
		}


		@Override
		public void doUpdate()
		{
			final double dist2Ball = 200;
			IVector2 dest = calculateDestinationAroundBall(0.2, dist2Ball, dist2Ball + maxMarginToBall,
					dist2Ball - maxMarginToBall);

			dest = validatePosition(dest);

			updateDestination(dest);
			updateTargetAngle(getTargetOrientation());

			super.doUpdate();

			drawStateShapes(100, dest);

			if (ballContactLevel == EBallContactLevel.NONE && isFocused() && isBehindBall())
			{
				if (safeDistance < decelerateDistance)
				{
					triggerEvent(EEvent.FOLLOWED_TO_PUSH);
				} else
				{
					triggerEvent(EEvent.FOLLOWED_TO_KICK);
				}
			}
		}


		private boolean isBehindBall()
		{
			double ball2TargetAngle = targetPos.getPos().subtractNew(getBallPos()).getAngle(getAngle());
			double bot2BallAngle = getBallPos().subtractNew(getTBot().getBotKickerPos()).getAngle(getAngle());
			return (Math.abs(AngleMath.difference(ball2TargetAngle, bot2BallAngle)) < 0.1);
		}
	}
}
