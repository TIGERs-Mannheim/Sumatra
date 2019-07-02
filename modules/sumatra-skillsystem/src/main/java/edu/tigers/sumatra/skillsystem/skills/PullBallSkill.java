/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.botmanager.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.ChargingValue;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;


/**
 * Pull the ball to the given position (when no disturbance expected)
 */
public class PullBallSkill extends AMoveSkill
{
	@Configurable(comment = "Speed when near ball", defValue = "1.0")
	private static double defaultChillVel = 1.0;
	@Configurable(comment = "How fast to accelerate when near ball", defValue = "1.0")
	private static double chillAcc = 1.0;
	@Configurable(comment = "Tolerated distance of ball to target position", defValue = "50.0")
	private static double placementTolerance = 50;
	@Configurable(comment = "[ms] Time to wait after Turnoff of dribbler and release of Ball", defValue = "0.5")
	private static double waitTimeBeforeRelease = 0.5;
	@Configurable(comment = "The distance between kicker and ball to keep before dribbling the ball", defValue = "50.0")
	private static double minDistBeforeDribble = 50;
	@Configurable(comment = "The distance the bot should go to after ball release", defValue = "50.0")
	private static double distToBallAfterRelease = 50;
	@Configurable(comment = "dribbler speed for Pullback", defValue = "10000.0")
	private static double dribblerSpeed = 10000;
	@Configurable(comment = "Target detection precision", defValue = "5.0")
	private static double botPositioningTolerance = 5;
	@Configurable(comment = "Target detection precision", defValue = "0.3")
	private static double ballPlacedWaitTime = 0.3;

	private final IVector2 target;
	private final BallReleaseState ballReleaseState = new BallReleaseState();
	private final MoveToBallState moveToBallState = new MoveToBallState();
	private final TimestampTimer ballPlacedTimer = new TimestampTimer(ballPlacedWaitTime);

	private double newDribblerSpeed = 0;
	private double targetOrientation;
	private double chillVel = defaultChillVel;


	/**
	 * @param target new ball position
	 */
	public PullBallSkill(final IVector2 target)
	{
		super(ESkill.PULL_BALL);
		this.target = target;

		final BallPlacementState ballPlacementState = new BallPlacementState();
		final GetBallContactState ballContactState = new GetBallContactState();

		setInitialState(moveToBallState);

		addTransition(EEvent.MOVE_TO_BALL, moveToBallState);
		addTransition(EEvent.GET_BALL_CONTACT, ballContactState);
		addTransition(EEvent.PLACE_BALL, ballPlacementState);
		addTransition(EEvent.BALL_PLACED, ballReleaseState);
	}


	public void setChillVel(final double chillVel)
	{
		this.chillVel = chillVel;
	}


	private boolean isBallPlaced(final double offset)
	{
		final boolean placed = getBall().getPos().distanceTo(target) < placementTolerance + offset;
		if (placed)
		{
			ballPlacedTimer.update(getWorldFrame().getTimestamp());
		} else
		{
			ballPlacedTimer.reset();
		}
		return ballPlacedTimer.isTimeUp(getWorldFrame().getTimestamp());
	}


	public boolean hasReleasedBall()
	{
		return getCurrentState() == moveToBallState
				|| (getCurrentState() == ballReleaseState
						&& ballReleaseState.released);
	}


	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		kickerDribblerOutput.setDribblerSpeed(newDribblerSpeed);
	}


	private enum EEvent implements IEvent
	{
		MOVE_TO_BALL,
		GET_BALL_CONTACT,
		BALL_PLACED,
		PLACE_BALL
	}

	private class MoveToBallState extends MoveToState
	{
		protected MoveToBallState()
		{
			super(PullBallSkill.this);
		}


		@Override
		public void doEntryActions()
		{
			super.doEntryActions();

			getMoveCon().setBotsObstacle(true);
			getMoveCon().setDestinationOutsideFieldAllowed(true);
			getMoveCon().setPenaltyAreaAllowedOur(true);
			getMoveCon().setPenaltyAreaAllowedTheir(true);
			getMoveCon().setGoalPostObstacle(true);
			getMoveCon().setBallObstacle(false);

			newDribblerSpeed = 0;
		}


		@Override
		public void doUpdate()
		{
			IVector2 finalDest = LineMath.stepAlongLine(getBall().getPos(), target, 100);
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getPos())
					.withTBot(getTBot())
					.withDestination(finalDest)
					.withMaxMargin(150)
					.withMinMargin(minDistBeforeDribble)
					.build()
					.getAroundBallDest();
			if (isBallPlaced(0.0))
			{
				triggerEvent(EEvent.BALL_PLACED);
			} else if (getPos().distanceTo(dest) < botPositioningTolerance && getBall().getVel().getLength2() < 0.1)
			{
				triggerEvent(EEvent.GET_BALL_CONTACT);
			} else
			{
				getMoveCon().updateDestination(dest);
				getMoveCon().updateLookAtTarget(getBall());
				super.doUpdate();
			}
		}
	}

	private class GetBallContactState extends AState
	{
		private IVector2 lastBallPos;
		private TimestampTimer timer;
		private TimestampTimer securityTimer;
		private ChargingValue chargingDistanceToBall = ChargingValue.aChargingValue()
				.withDefaultValue(0.0)
				.withInitValue(0.0)
				.withLimit(50.0)
				.withChargeRate(30.0)
				.build();


		private double getTargetOrientation(final IVector2 dest)
		{
			return getBall().getPos().subtractNew(dest).getAngle(0);
		}


		@Override
		public void doEntryActions()
		{
			lastBallPos = getBall().getPos();
			targetOrientation = getTargetOrientation(getPos());
			getMoveCon().getMoveConstraints().setVelMax(chillVel);
			timer = new TimestampTimer(0.5);
			securityTimer = new TimestampTimer(1.5);
			newDribblerSpeed = dribblerSpeed;
			chargingDistanceToBall.reset();
		}


		@Override
		public void doUpdate()
		{
			if (getVel().getLength2() <= getMoveCon().getMoveConstraints().getVelMax())
			{
				getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			}

			chargingDistanceToBall.update(getWorldFrame().getTimestamp());
			double dist = chargingDistanceToBall.getValue();
			IVector2 dest = LineMath.stepAlongLine(lastBallPos, getPos(), getBot().getCenter2DribblerDist() - dist);
			setTargetPose(dest, targetOrientation);
			if (getTBot().hadBallContact(0.5))
			{
				triggerEvent(EEvent.PLACE_BALL);
			} else if (Math.abs(getTBot().getBotKickerPos().distanceTo(lastBallPos) - Geometry.getBallRadius()) < 5)
			{
				timer.update(getWorldFrame().getTimestamp());
				if (timer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					triggerEvent(EEvent.MOVE_TO_BALL);
				}
			} else
			{
				securityTimer.update(getWorldFrame().getTimestamp());
				if (securityTimer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					triggerEvent(EEvent.MOVE_TO_BALL);
				}
			}
		}
	}

	private class BallPlacementState extends AState
	{
		private final TimestampTimer timeoutTimer = new TimestampTimer(1.5);
		private IVector2 dest;
		private double nearBallTol = Geometry.getBotRadius() + Geometry.getBallRadius() + botPositioningTolerance;


		@Override
		public void doEntryActions()
		{
			getMoveCon().getMoveConstraints().setVelMax(chillVel);

			dest = calcDest();
			newDribblerSpeed = dribblerSpeed;
			timeoutTimer.reset();
		}


		private IVector2 calcDest()
		{
			return LineMath.stepAlongLine(target, getBall().getPos(),
					-(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + 5));
		}


		@Override
		public void doUpdate()
		{
			if (getVel().getLength2() <= getMoveCon().getMoveConstraints().getVelMax())
			{
				getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			}

			timeoutTimer.update(getWorldFrame().getTimestamp());
			setTargetPose(dest, targetOrientation);
			if (getBall().getVel().getLength2() < 0.1)
			{
				if (isBallPlaced(0.0))
				{
					triggerEvent(EEvent.BALL_PLACED);
				} else if ((!getTBot().hadBallContact(0.5) && !isNearBall())
						|| timeoutTimer.isTimeUp(getWorldFrame().getTimestamp()))
				{
					triggerEvent(EEvent.MOVE_TO_BALL);
				}
			} else
			{
				timeoutTimer.reset();
			}
		}


		private boolean isNearBall()
		{
			return getBall().getPos().distanceTo(getPos()) < nearBallTol;
		}


		@Override
		public void doExitActions()
		{
			IBotMovementLimits moveLimits = getBot().getBotParams().getMovementLimits();
			getMoveCon().getMoveConstraints().setVelMax(moveLimits.getVelMax());
			getMoveCon().getMoveConstraints().setAccMax(moveLimits.getAccMax());
		}
	}

	private class BallReleaseState extends AState
	{
		private final TimestampTimer waitTimer = new TimestampTimer(waitTimeBeforeRelease);
		private boolean released = false;


		@Override
		public void doEntryActions()
		{
			waitTimer.reset();
			newDribblerSpeed = 0;
			released = false;
		}


		@Override
		public void doUpdate()
		{
			waitTimer.update(getWorldFrame().getTimestamp());
			if (waitTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), getPos(),
						Geometry.getBotRadius() + Geometry.getBallRadius() + distToBallAfterRelease);
				double orientation = getBall().getPos().subtractNew(getPos()).getAngle();
				setTargetPose(dest, orientation);
				released = dest.isCloseTo(getPos(), 50);
			}
			if (!isBallPlaced(10.0))
			{
				triggerEvent(EEvent.MOVE_TO_BALL);
			}
		}


		@Override
		public void doExitActions()
		{
			released = false;
		}
	}
}
