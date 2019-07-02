/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import static edu.tigers.sumatra.math.SumatraMath.relative;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;


public class ApproachAndStopBallSkill extends AMoveSkill
{
	@Configurable(defValue = "0.7")
	private static double maxLookahead = 0.7;

	@Configurable(comment = "The target velocity difference between bot and ball to aim for when trying catch up ball", defValue = "0.7")
	private static double catchUpBallTargetVelDiff = 0.7;

	@Configurable(comment = "Maximum dribble speed (when dribbling enabled)", defValue = "10000")
	private static int maxDribbleSpeed = 10000;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.2, 0.6);
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final TimestampTimer dribbleTimer = new TimestampTimer(0.1);
	private double marginToTheirPenArea = 0;


	public ApproachAndStopBallSkill()
	{
		super(ESkill.APPROACH_AND_STOP_BALL);

		setInitialState(new DefaultState());

		// initially the ball is moving
		ballSpeedHysteresis.update(RuleConstraints.getMaxBallSpeed());
	}


	public void setMarginToTheirPenArea(final double marginToTheirPenArea)
	{
		this.marginToTheirPenArea = marginToTheirPenArea;
	}


	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);

		if (getTBot().hasBallContact())
		{
			if (getBall().getVel().getLength2() < 0.3)
			{
				dribbleTimer.update(getWorldFrame().getTimestamp());
			}
		} else if (getBall().getVel().getLength2() > 0.2)
		{
			dribbleTimer.reset();
		}

		double minDribbleSpeed = 5000;
		double dribbleSpeed = minDribbleSpeed
				+ relative(getBall().getVel().getLength2(), 0.1, 0.3) * (maxDribbleSpeed - minDribbleSpeed);
		if (dribbleTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			dribbleSpeed = 0;
		}
		kickerDribblerOutput.setDribblerSpeed(dribbleSpeed);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		ballSpeedHysteresis.update(getBall().getVel().getLength2());
		ballStabilizer.update(getBall(), getTBot());
	}


	public boolean ballStoppedByBot()
	{
		return ballSpeedHysteresis.isLower() && ballIsNearRobotKicker();
	}


	private boolean ballIsNearRobotKicker()
	{
		return ballStabilizer.getBallPos().distanceTo(getTBot().getBotKickerPos()) < 50;
	}


	public boolean ballStoppedMoving()
	{
		return ballSpeedHysteresis.isLower();
	}

	private class DefaultState extends MoveToState
	{
		private final PositionValidator positionValidator = new PositionValidator();
		private final DoubleChargingValue lookaheadChargingValue = new DoubleChargingValue(
				0,
				0.6,
				-0.9,
				0,
				maxLookahead);
		private IVector2 primaryDirection;


		private DefaultState()
		{
			super(ApproachAndStopBallSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			getMoveCon().setBallObstacle(false);
			getMoveCon().updateLookAtTarget(getBall());
			primaryDirection = getBallPos().subtractNew(getPos());
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getTrajectory().getTravelLineRolling().distanceTo(getPos()) < 100)
			{
				// do not respect other bots, when on ball line
				getMoveCon().setBotsObstacle(false);
			}

			positionValidator.update(getWorldFrame(), getMoveCon());
			positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);

			double distBallToKickerPos = getBallPos().distanceTo(getTBot().getBotKickerPos());
			if (ballSpeedHysteresis.isUpper() && !getTBot().hasBallContact()
					&& distBallToKickerPos > Geometry.getBallRadius() + 10)
			{
				primaryDirection = getBall().getVel();
				IVector2 ballPos = getBallPos();
				if (getPos().distanceTo(ballPos) < getTBot().getCenter2DribblerDist())
				{
					ballPos = getTBot().getBotKickerPos();
				}
				if (!Line.fromDirection(getPos(), primaryDirection).isPointInFront(ballPos))
				{
					primaryDirection = primaryDirection.multiplyNew(-1);
				}
				getMoveCon().getMoveConstraints().setPrimaryDirection(primaryDirection);
			}

			updateTargetOrientation();

			if (ballSpeedHysteresis.isUpper())
			{
				double botBallSpeedDiff = getTBot().getVel().subtractNew(getBall().getVel()).getLength2();
				double lookahead = calcLookahead(botBallSpeedDiff);

				updateDestination(lookahead);

				getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL).add(new DrawableAnnotation(getPos(),
						String.format("%.2f|%.2f", lookahead, botBallSpeedDiff))
								.withOffset(Vector2.fromY(300)));
			}

			super.doUpdate();

			getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL)
					.add(new DrawableLine(Line.fromDirection(getBallPos(), primaryDirection.scaleToNew(1000))));
		}


		private void updateDestination(double lookahead)
		{
			double offset = getTBot().hasBallContact() ? 50 : -50;
			IVector2 dest = getBallPos(lookahead).subtractNew(primaryDirection
					.scaleToNew(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + offset));

			dest = positionValidator.movePosInFrontOfOpponent(dest);
			dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
			dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
			getMoveCon().updateDestination(dest);
		}


		private void updateTargetOrientation()
		{
			getMoveCon().updateTargetAngle(primaryDirection.getAngle());
		}


		private double calcLookahead(final double botBallSpeedDiff)
		{
			double hysteresis = 0.05;
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


		private boolean isTouchingBall()
		{
			// the barrier may not always working correctly, so use vision to check distance to ball
			// also note, that it may happen, that the bot is predicted too far, such that it is located
			// in front of the ball, in which case it looks like the ball is on the dribbler, while
			// really it pushes the ball forward.
			final double radius = Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
			return getTBot().hasBallContact() || getBallPos().distanceTo(getTBot().getPos()) < radius;
		}


		private IVector2 getBallPos(double lookahead)
		{
			return ballStabilizer.getBallPos(lookahead);
		}


		private IVector2 getBallPos()
		{
			return ballStabilizer.getBallPos();
		}
	}
}
