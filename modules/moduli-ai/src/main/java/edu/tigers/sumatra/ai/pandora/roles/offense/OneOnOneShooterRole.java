/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.penaltyshootout.DribbleState;
import edu.tigers.sumatra.ai.pandora.roles.offense.penaltyshootout.EBallDribbleEvent;
import edu.tigers.sumatra.ball.trajectory.IChipBallConsultant;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.Optional;


public class OneOnOneShooterRole extends ARole
{
	@Configurable(comment = "[s] time to finish the shootout, should be less equal the allowed time, the ball travel time is not included", defValue = "9.0")
	private static double allowedTime = 9.0;

	@Configurable(comment = "[s] the time the TouchKickSkill needs to prepare for kick, after receiving the target", defValue = "0.4")
	private static double prepareKickTime = 0.4;

	@Configurable(comment = "Bot is not allowed to drive into their penalty area", defValue = "true")
	private static boolean penaltyTheirIsObstacle = false;

	@Configurable(comment = "Bot is not allowed to drive into our penalty area", defValue = "false")
	private static boolean penaltyOurIsObstacle = false;

	@Configurable(comment = "Whether to use chip kicks", defValue = "true")
	private static boolean chipKickEnabled = true;

	@Configurable(comment = "[mm] Keeper shall not get closer then this to intercept the goal attempt", defValue = "500.0")
	private static double safetyDistanceToOpponentKeeper = 500.0;

	@Configurable(comment = "[mm] distBall2Goal offset for initChipVel calculation", defValue = "-300.0")
	private static double distBall2GoalOffset = -300.0;

	@Configurable(comment = "distBall2Goal factor for initChipVel calculation", defValue = "0.95")
	private static double distBall2GoalFactor = 0.95;

	@Configurable(comment = "x-coordinate of dribble target", defValue = "4000.0")
	private static double xCoordinateTarget = 4000.0;

	@Configurable(comment = "[mm] safetyMargin between BotHeight and ChipHeight", defValue = "50.0")
	private static double heightMargin = 50.0;

	private TimestampTimer time = new TimestampTimer(allowedTime - prepareKickTime);


	/**
	 * The penalty attacker role
	 */
	public OneOnOneShooterRole()
	{
		super(ERole.PENALTY_ATTACKER);
		IState ballPlacementState = new DribbleState(this, new DynamicPosition(Vector2.fromX(xCoordinateTarget)),
				penaltyOurIsObstacle, penaltyTheirIsObstacle);
		IState prepareState = new PrepareState();
		IState delayState = new DelayState();

		setInitialState(prepareState);
		addTransition(prepareState, EEvent.PREPARATION_COMPLETE, ballPlacementState);

		addTransition(EBallDribbleEvent.DRIBBLING_FAILED, delayState);
		addTransition(EBallDribbleEvent.DRIBBLING_FINISHED, delayState);

		addTransition(EEvent.PREPARED_POSITIONED_STRAIGHT, new StraightPlacedShootState());
		addTransition(EEvent.PREPARED_CENTERED_STRAIGHT, new StraightCenteredShootState());
		addTransition(EEvent.PREPARED_CENTERED_CHIPPED, new ChippedCenteredShootState());
		addTransition(EEvent.HURRY_SHOOT, new HurryShootState());

		addTransition(EEvent.SHOT_CONFIRMED, prepareState);
	}


	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();
		time.update(timestamp());
		if (canChipToGoal(getOpponentGoalCenter(), 0.1) && !getCurrentState().getClass().equals(PrepareState.class))
		{
			triggerEvent(EBallDribbleEvent.DRIBBLING_FINISHED);
		}
	}


	@Override
	protected void onCompleted()
	{
		super.onCompleted();
		time.reset();
	}


	private boolean canChipToGoal(IVector2 shotTarget, double lookahead)
	{
		// If no Keeper, just kick straight, it's safer
		if (!chipKickEnabled || getOpponentKeeper() == null)
		{
			return false;
		}
		IChipBallConsultant chipConsultant = getBall().getChipConsultant();

		ILineSegment lineBall2Goal = Lines.segmentFromPoints(getBallPos(), shotTarget);
		final double distBall2Goal = lineBall2Goal.getLength();
		final double initChipVel = getInitChipVel(distBall2Goal);

		double distBall2NearInterceptionBarrier = chipConsultant.getMinimumDistanceToOverChip(initChipVel,
				RuleConstraints.getMaxRobotHeight() + heightMargin);
		distBall2NearInterceptionBarrier += safetyDistanceToOpponentKeeper;

		double distBall2FarInterceptionBarrier = chipConsultant.getMaximumDistanceToOverChip(initChipVel,
				RuleConstraints.getMaxRobotHeight() + heightMargin);
		distBall2FarInterceptionBarrier -= safetyDistanceToOpponentKeeper;

		final double keeperTimeToReact = chipConsultant.getTimeForKick(distBall2NearInterceptionBarrier, initChipVel);
		final IVector2 keeperPos = lineBall2Goal
				.closestPointOnLine(getOpponentKeeper().getPosByTime(keeperTimeToReact + lookahead));
		final double distBall2Keeper = keeperPos.distanceTo(getBallPos());

		getShapes(EAiShapesLayer.PENALTY_ONE_ON_ONE)
				.add(new DrawableCircle(Circle.createCircle(getBallPos(), distBall2NearInterceptionBarrier), Color.GREEN));
		getShapes(EAiShapesLayer.PENALTY_ONE_ON_ONE).add(new DrawableCircle(
				Circle.createCircle(getBallPos(), Math.max(distBall2FarInterceptionBarrier, 1)), Color.YELLOW));
		getShapes(EAiShapesLayer.PENALTY_ONE_ON_ONE)
				.add(new DrawableCircle(Circle.createCircle(keeperPos, Geometry.getBotRadius() * 1.25), Color.BLACK));

		// Can we chip safely over the opponent keeper into the goal?
		return (distBall2Keeper > distBall2NearInterceptionBarrier)
				&& (distBall2Keeper < distBall2FarInterceptionBarrier);

	}


	private ITrackedBot getOpponentKeeper()
	{
		return getWFrame().getOpponentBot(getAiFrame().getKeeperOpponentId());
	}


	private IVector2 getOpponentGoalCenter()
	{
		return Geometry.getGoalTheir().getCenter();
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
	}


	private long timestamp()
	{
		return getWFrame().getTimestamp();
	}


	private KickParams getKickParamsForChip(IVector2 target)
	{
		return KickParams.of(EKickerDevice.CHIP, getInitChipVel(target.distanceTo(getBallPos())));
	}


	private double getInitChipVel(final double distBall2Goal)
	{
		double distance = (distBall2Goal + distBall2GoalOffset) * distBall2GoalFactor;
		final double desiredChipSpeed = getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(Math.max(0, distance), 0);
		return Math.min(getBot().getRobotInfo().getBotParams().getKickerSpecs().getMaxAbsoluteChipVelocity(),
				desiredChipSpeed);
	}


	private void calculateShotEvent()
	{
		if (getAiFrame().getGameState().getState() != EGameState.PENALTY)
		{
			triggerEvent(EEvent.SHOT_CONFIRMED);
		}
	}


	private enum EKeeperEvents
	{
		NOT_EXISTING,
		COMING_TOWARDS_US,
		STAYS_IN_GOAL,
		DOES_NOT_MOVE,
		NO_CLUE
	}

	private enum EEvent implements IEvent
	{
		PREPARATION_COMPLETE,
		PREPARED_POSITIONED_STRAIGHT,
		PREPARED_CENTERED_STRAIGHT,
		PREPARED_CENTERED_CHIPPED,
		HURRY_SHOOT,
		SHOT_CONFIRMED
	}

	private class PrepareState extends AState
	{
		MoveToSkill skill;


		@Override
		public void doEntryActions()
		{
			skill = MoveToSkill.createMoveToSkill();

			IVector2 standbyPos = getBallPos()
					.addNew(Vector2.fromX(RuleConstraints.getStopRadius() + Geometry.getBotRadius() + 20));

			skill.updateDestination(standbyPos);
			skill.updateLookAtTarget(getWFrame().getBall());
			setNewSkill(skill);
		}


		@Override
		public void doUpdate()
		{
			IVector2 standbyPos = getBallPos()
					.subtractNew(Vector2.fromX(RuleConstraints.getStopRadius() + Geometry.getBotRadius()));

			skill.updateDestination(standbyPos);

			if ((getAiFrame().getRefereeMsg() != null)
					&& getAiFrame().getRefereeMsg().getCommand().equals(Command.NORMAL_START))
			{
				triggerEvent(EEvent.PREPARATION_COMPLETE);
				time.start(timestamp());
			}
		}
	}

	private class DelayState extends AState
	{
		private ApproachAndStopBallSkill skill;
		private boolean isIdling;


		@Override
		public void doEntryActions()
		{
			skill = new ApproachAndStopBallSkill();
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveCon().setBallObstacle(false);
			setNewSkill(skill);

			isIdling = false;
		}


		@Override
		public void doUpdate()
		{
			EKeeperEvents keeper = getKeeperBehaviour(0.3);
			if (keeper == EKeeperEvents.NOT_EXISTING)
			{
				triggerEvent(EEvent.PREPARED_CENTERED_STRAIGHT);
			} else if (time.isTimeUp(timestamp()))
			{
				triggerEvent(EEvent.HURRY_SHOOT);
			} else if ((goalIsEmpty(0) && keeper == EKeeperEvents.DOES_NOT_MOVE) || goalIsEmpty(0.4))
			{
				triggerEvent(EEvent.PREPARED_CENTERED_STRAIGHT);
			} else if (canChipToGoal(getOpponentGoalCenter(), 0.0))
			{
				triggerEvent(EEvent.PREPARED_CENTERED_CHIPPED);
			} else if (skill.isInitialized() && (skill.ballStoppedByBot() || skill.ballStoppedMoving()))
			{
				arrivedAtDribbleTarget(keeper);
			}
		}


		private void arrivedAtDribbleTarget(EKeeperEvents keeper)
		{
			if (canChipToGoalSoon(getOpponentGoalCenter(), keeper) && time.getRemainingTime(timestamp()) > 0.5)
			{
				if (!isIdling)
				{
					setNewSkill(new IdleSkill());
				}
			} else
			{
				triggerEvent(EEvent.PREPARED_POSITIONED_STRAIGHT);
			}
		}


		private boolean canChipToGoalSoon(IVector2 shotTarget, EKeeperEvents keeper)
		{
			if (keeper != EKeeperEvents.COMING_TOWARDS_US)
			{
				return false;
			}

			boolean lastIteration = false;
			for (double lookahead = 0.0; lookahead <= 1.0; lookahead += 0.1)
			{
				boolean thisIteration = canChipToGoal(shotTarget, lookahead);
				if (thisIteration && lastIteration)
				{
					return true;
				}
				lastIteration = thisIteration;
			}
			return false;
		}


		private EKeeperEvents getKeeperBehaviour(final double lookahead)
		{
			if (getOpponentKeeper() == null)
			{
				return EKeeperEvents.NOT_EXISTING;
			}
			if (getOpponentKeeper().getVelByTime(lookahead).getLength() < 0.1)
			{
				return EKeeperEvents.DOES_NOT_MOVE;
			}
			double angle = AngleMath.difference(getOpponentKeeper().getVelByTime(lookahead).getAngle(),
					Vector2.fromY(1).getAngle());
			final double precision = 0.15;
			if ((angle > (1.0 - precision) * AngleMath.PI || angle < precision * AngleMath.PI)
					&& (Geometry.getGoalTheir().getLineSegment().distanceTo(getOpponentKeeper().getPosByTime(lookahead))
					< 250))
			{
				return EKeeperEvents.STAYS_IN_GOAL;
			}

			angle = AngleMath.difference(getOpponentKeeper().getVelByTime(lookahead).getAngle(),
					getPos().subtractNew(getOpponentKeeper().getPosByTime(lookahead)).getAngle());
			if (angle < precision * 2
					&& !Geometry.getPenaltyAreaTheir().isPointInShape(getOpponentKeeper().getPos()))
			{
				return EKeeperEvents.COMING_TOWARDS_US;
			}
			return EKeeperEvents.NO_CLUE;
		}


		private boolean goalIsEmpty(final double precision)
		{
			final double leftPostAngle = getBallPos().subtractNew(Geometry.getGoalTheir().getLeftPost()).getAngle();
			final double rightPostAngle = getBallPos().subtractNew(Geometry.getGoalTheir().getRightPost()).getAngle();
			final double keeperAngle = getBallPos().subtractNew(getOpponentKeeper().getPos()).getAngle();

			// Ball behind opponent goal (very unlikely)
			if (getBallPos().x() > Geometry.getFieldLength() / 2)
			{
				return false;
			}
			// Keeper behind ball
			if (getBallPos().x() > getOpponentKeeper().getPos().x())
			{
				return true;
			}

			// Angle to goal is to narrow
			if (Math.abs(AngleMath.difference(leftPostAngle, rightPostAngle)) < 0.5)
			{
				return false;
			}

			return (leftPostAngle > keeperAngle - precision) || (rightPostAngle < keeperAngle + precision);
		}
	}

	private class StraightPlacedShootState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var target = calculateBestShotTarget();
			var kickParams = KickParams.maxStraight();

			TouchKickSkill shootSkill = new TouchKickSkill(target, kickParams);
			shootSkill.getMoveCon().setPenaltyAreaTheirObstacle(penaltyTheirIsObstacle);
			shootSkill.getMoveCon().setPenaltyAreaOurObstacle(penaltyOurIsObstacle);
			shootSkill.getMoveCon().setBotsObstacle(false);
			setNewSkill(shootSkill);
		}


		@Override
		public void doUpdate()
		{
			if (time.isTimeUp(timestamp()))
			{
				triggerEvent(EEvent.HURRY_SHOOT);
			}
			calculateShotEvent();
		}


		private IVector2 calculateBestShotTargetBackup()
		{
			OneOnOneShooterRole.this.beforeFirstUpdate();
			final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
			rater.setObstacles(getWFrame().getOpponentBots().values());
			rater.setTimeToKick(-1);
			return rater.rate(getBallPos()).map(IRatedTarget::getTarget)
					.orElse(getOpponentGoalCenter());
		}


		private IVector2 calculateBestShotTarget()
		{
			return Optional.ofNullable(getAiFrame().getTacticalField().getBestGoalKick())
					.map(GoalKick::getKick)
					.map(Kick::getTarget)
					.orElse(calculateBestShotTargetBackup());
		}
	}

	private class StraightCenteredShootState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var target = getOpponentGoalCenter();
			var kickParams = KickParams.maxStraight();

			TouchKickSkill shootSkill = new TouchKickSkill(target, kickParams);
			shootSkill.getMoveCon().setPenaltyAreaTheirObstacle(penaltyTheirIsObstacle);
			shootSkill.getMoveCon().setPenaltyAreaOurObstacle(penaltyOurIsObstacle);
			shootSkill.getMoveCon().setBotsObstacle(false);
			setNewSkill(shootSkill);
		}


		@Override
		public void doUpdate()
		{
			if (time.isTimeUp(timestamp()))
			{
				triggerEvent(EEvent.HURRY_SHOOT);
			}
			calculateShotEvent();
		}
	}

	private class ChippedCenteredShootState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var target = getOpponentGoalCenter();
			var kickParams = getKickParamsForChip(target);

			TouchKickSkill shootSkill = new TouchKickSkill(target, kickParams);
			shootSkill.getMoveCon().setPenaltyAreaTheirObstacle(penaltyTheirIsObstacle);
			shootSkill.getMoveCon().setPenaltyAreaOurObstacle(penaltyOurIsObstacle);
			shootSkill.getMoveCon().setBotsObstacle(false);
			setNewSkill(shootSkill);
		}


		@Override
		public void doUpdate()
		{
			if (time.isTimeUp(timestamp()))
			{
				triggerEvent(EEvent.HURRY_SHOOT);
			}
			calculateShotEvent();
		}
	}

	private class HurryShootState extends AState
	{
		@Override
		public void doEntryActions()
		{
			var target = hurryShoot();
			var kickParams = getKickParamsForChip(target);

			TouchKickSkill shootSkill = new TouchKickSkill(target, kickParams);
			shootSkill.getMoveCon().setPenaltyAreaTheirObstacle(penaltyTheirIsObstacle);
			shootSkill.getMoveCon().setPenaltyAreaOurObstacle(penaltyOurIsObstacle);
			shootSkill.getMoveCon().setBotsObstacle(false);
			setNewSkill(shootSkill);
		}


		@Override
		public void doUpdate()
		{
			calculateShotEvent();
		}


		private IVector2 hurryShoot()
		{
			IVector2 targetPos = Geometry.getGoalTheir().getLine()
					.intersectHalfLine(Lines.halfLineFromDirection(getPos(), getBallPos()))
					.orElse(getOpponentGoalCenter());

			double adaptedY = targetPos.y();
			adaptedY = Math.min(adaptedY, Geometry.getGoalTheir().getRectangle().maxY() - safetyDistanceToOpponentKeeper);
			adaptedY = Math.max(adaptedY, Geometry.getGoalTheir().getRectangle().minY() + safetyDistanceToOpponentKeeper);
			return Vector2.fromXY(targetPos.x(), adaptedY);
		}
	}

}