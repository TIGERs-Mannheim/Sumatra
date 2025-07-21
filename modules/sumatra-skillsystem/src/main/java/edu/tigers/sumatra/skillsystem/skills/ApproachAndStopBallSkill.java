/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.awt.Color;
import java.util.List;


/**
 * ApproachAndStop a ball consists of 3 phases.
 * - Approach with a higher velocity than the ball (This ends as soon as ball has any sort of traction)
 * - Acquire control by maintaining the speed (This ends as soon as traction is received for a certain time)
 * - Stopping (Ends when ball is stopped, or with the succeedEarly mechanic)
 */
public class ApproachAndStopBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Margin to our Penalty Area", defValue = "100.0")
	private static double marginToOurPenArea = 100.0;
	@Configurable(comment = "Used dribbler mode to be used", defValue = "DEFAULT")
	private static EDribblerMode usedDribblerMode = EDribblerMode.DEFAULT;
	@Configurable(comment = "[m/s^2] Brake acc when stopping the ball", defValue = "3.0")
	private static double stopAccMax = 3.0;
	@Configurable(comment = "[m/s] Lower threshold for ball speed hysteresis", defValue = "0.1")
	private static double ballSpeedHysteresisLower = 0.1;
	@Configurable(comment = "[m/s] Upper threshold for ball speed hysteresis", defValue = "0.6")
	private static double ballSpeedHysteresisUpper = 0.6;


	@Configurable(comment = "[m/s] Velocity difference between ball and robot to aim for when hitting the ball", defValue = "0.3")
	private static double approachSpeedDiff = 0.3;
	@Configurable(comment = "[mm] If ball is closer than this distance no changes in how the ball will be approached will happen", defValue = "50")
	private static double approachNoStrategySwitchDistance = 50;
	@Configurable(comment = "[s] If the bot is close to the ball traction must be created within this time, or the skill will try to freely rotate towards the ball", defValue = "0.2")
	private static double approachFixedRotationTime = 0.2;
	@Configurable(comment = "[s] If the bot is close to the ball traction must be created within this time, or the skill will start a complete retry", defValue = "0.4")
	private static double approachFailureTime = 0.4;
	@Configurable(comment = "[mm] Distance to the ball line to stop using primary direction", defValue = "150.0")
	private static double approachStopPrimaryDirectionDistance = 150.0;
	@Configurable(comment = "[m/s] Max velocity component orthogonal to tha ball direction to allow stop using of primary direction", defValue = "0.2")
	private static double approachStopPrimaryOrthogonalVelocity = 0.2;
	@Configurable(comment = "Use legacy calculations to determine the catchup destination", defValue = "false")
	private static boolean approachRollingBallUseLegacyCalculations = false;

	@Configurable(comment = "[s] Time the ball must be dribbled by the bot to be considered acquired", defValue = "0.05")
	private static double acquireControlTractionTime = 0.05;
	@Configurable(comment = "Minimum traction level required to be considered touching the ball", defValue = "LIGHT")
	private static EDribbleTractionState acquireControlMinTractionState = EDribbleTractionState.LIGHT;
	@Configurable(comment = "[s] Timeout for acquireControl phase", defValue = "0.3")
	private static double acquireControlTimeout = 0.3;
	@Configurable(comment = "[s] Time the ball must be stopped to mark skill as successful", defValue = "0.1")
	private static double successTime = 0.1;
	@Configurable(comment = "Minimum dribble traction necessary to trigger succeed early", defValue = "STRONG")
	private static EDribbleTractionState succeedEarlyTraction = EDribbleTractionState.STRONG;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(ballSpeedHysteresisLower, ballSpeedHysteresisUpper);
	private final PositionValidator positionValidator = new PositionValidator();
	private final TimestampTimer successTimer = new TimestampTimer(successTime);

	@Setter
	private double marginToTheirPenArea = 0;
	@Setter
	private boolean succeedEarly = false;
	@Setter
	private double succeedEarlyVel = 0;
	@Setter
	private boolean isDribblingFoulImminent = false;
	private IVector2 cachedBallDirection;

	private IBallTrajectory cachedBallTrajectory;
	private long cachedBallTrajectoryTimestamp;


	public ApproachAndStopBallSkill()
	{
		var keepDistToBall = new KeepDistToBall();
		var approachBall = new ApproachBall();
		var acquireControl = new AcquireControl();
		var stopState = new StopBall();

		addTransition(keepDistToBall, EEvent.SUCCESS, approachBall);

		addTransition(approachBall, EEvent.SUCCESS, acquireControl);
		addTransition(approachBall, EEvent.FAILURE, keepDistToBall);

		addTransition(acquireControl, EEvent.SUCCESS, stopState);
		addTransition(acquireControl, EEvent.FAILURE, approachBall);

		addTransition(stopState, EEvent.FAILURE, acquireControl);

		addTransition(EEvent.DRIBBLING_FOUL_IMMINENT, keepDistToBall);

		setInitialState(approachBall);
	}


	private boolean isBallStoppedByBot()
	{
		return ballStoppedMoving() && isBallNearRobotKicker();
	}


	private boolean isBallNearRobotKicker()
	{
		return getTBot().getBotShape().getKickerLine().distanceTo(getCachedBallPos()) < approachNoStrategySwitchDistance;
	}


	private boolean ballStoppedMoving()
	{
		return ballSpeedHysteresis.isLower();
	}


	private boolean isTouchingBall()
	{
		return getTBot().getRobotInfo().getDribbleTraction().getId() >= acquireControlMinTractionState.getId();
	}


	private boolean isStateSwitchAllowed()
	{
		return !isBallNearRobotKicker() && !isTouchingBall();
	}


	private boolean isBallVisible()
	{
		return getBall().invisibleFor() < 0.05;
	}


	private IVector2 getCachedBallPos()
	{
		var timePassed = (getTimestamp() - cachedBallTrajectoryTimestamp) / 1e9;
		return cachedBallTrajectory.getPosByTime(timePassed).getXYVector();
	}


	private IVector2 getCachedBallVel()
	{
		var timePassed = (getTimestamp() - cachedBallTrajectoryTimestamp) / 1e9;
		return cachedBallTrajectory.getVelByTime(timePassed).getXYVector();
	}


	private boolean ballMovesTowardsMe()
	{
		var ballDir = getCachedBallVel();
		var ballToBotDir = getPos().subtractNew(getCachedBallPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.deg2rad(90);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGameStateObstacle(false);

		cachedBallDirection = Vector2.zero();
		cachedBallTrajectory = getBall().getTrajectory();
		cachedBallTrajectoryTimestamp = getTimestamp();

		// initially the ball is moving
		ballSpeedHysteresis.setUpper(true);
	}


	@Override
	protected void beforeStateUpdate()
	{

		if (isBallVisible())
		{
			cachedBallTrajectoryTimestamp = getTimestamp();
			cachedBallTrajectory = getBall().getTrajectory();

			var ballSpeed = getBall().getVel().getLength2();
			ballSpeedHysteresis.setLowerThreshold(ballSpeedHysteresisLower);
			ballSpeedHysteresis.setUpperThreshold(ballSpeedHysteresisUpper);
			ballSpeedHysteresis.update(ballSpeed);
			if (ballSpeed > 0.5)
			{
				// Only consider the direction of the ball if there is some sort of movement to avoid noise in the direction
				cachedBallDirection = getCachedBallVel().normalizeNew();
			}
		} else
		{
			var ballPos = getCachedBallPos();
			var ballVel = getCachedBallVel();
			var botShape = getTBot().getBotShape().withMargin(Geometry.getBallRadius());
			if (botShape.isPointInShape(ballPos))
			{
				var ballExitsBotShapePos = ballPos.nearestToOpt(
						botShape.intersectPerimeterPath(Lines.halfLineFromDirection(ballPos, ballVel))
				).orElseGet(() -> botShape.nearestPointOutside(ballPos));

				cachedBallTrajectory = Geometry.getBallFactory()
						.createTrajectoryFromRollingBall(ballExitsBotShapePos, getVel());

			}
		}

		// do not respect other bots, when on ball line
		getMoveCon().setBotsObstacle((cachedBallTrajectory.distanceTo(getPos()) > 100));

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		positionValidator.getMarginToPenArea().put(ETeam.TIGERS, marginToOurPenArea);

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL).addAll(List.of(
				new DrawablePoint(getCachedBallPos(), Color.ORANGE),
				new DrawableArrow(getCachedBallPos(), getCachedBallVel().multiplyNew(100), Color.ORANGE)
		));

		super.beforeStateUpdate();
	}


	@Override
	public void doUpdate()
	{
		if (isDribblingFoulImminent)
		{
			triggerEvent(EEvent.DRIBBLING_FOUL_IMMINENT);
		}

		super.doUpdate();

		boolean stopped = isBallStoppedByBot() || succeedEarly();
		if (stopped)
		{
			successTimer.update(getWorldFrame().getTimestamp());
			if (successTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.SUCCESS);
			} else
			{
				setSkillState(ESkillState.IN_PROGRESS);
			}
		} else if (isStateSwitchAllowed() && isBallVisible() && (ballMovesTowardsMe() || ballStoppedMoving()))
		{
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
		if (!stopped)
		{
			successTimer.reset();
		}
	}


	private boolean succeedEarly()
	{
		return succeedEarly
				&& getTBot().getRobotInfo().getDribbleTraction().getId() >= succeedEarlyTraction.getId()
				&& getCachedBallVel().getLength() < succeedEarlyVel;
	}


	private IVector2 validDest(IVector2 originalDest)
	{
		IVector2 dest = originalDest;
		dest = positionValidator.movePosInFrontOfOpponent(dest);
		dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
		return positionValidator.movePosOutOfPenAreaWrtBall(dest);
	}


	private void updateCatchupDestinationNew(IVector2 currentPosition, IVector2 wantedVelocity)
	{
		var velLength = wantedVelocity.getLength2();
		var breakDistance = 1000 * (0.5f * velLength * velLength / getTBot().getMoveConstraints().getAccMax());
		var dest = currentPosition.addNew(wantedVelocity.scaleToNew(breakDistance));
		updateDestination(validDest(dest));
	}


	private double calcStopAngle()
	{
		var a0 = getTBot().getAngleByTime(0);
		var v0 = getTBot().getAngularVel();
		var a = v0 > 0 ? getMoveConstraints().getAccMax() : -getMoveConstraints().getAccMax();
		var t = -v0 / a;
		var a1 = a0 + v0 * t + 0.5 * a * t * t;

		return AngleMath.normalizeAngle(a1);
	}


	private enum EEvent implements IEvent
	{
		SUCCESS,
		FAILURE,
		DRIBBLING_FOUL_IMMINENT,
	}

	private class KeepDistToBall extends AState
	{
		@Override
		public void doUpdate()
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));

			var distanceToBall = Geometry.getBotRadius() + approachNoStrategySwitchDistance;

			var dir = Vector2.fromPoints(getCachedBallPos(), getPos());
			var dest = getCachedBallPos().addNew(dir.scaleToNew(5 * distanceToBall));
			updateDestination(validDest(dest));
			updateLookAtTarget(getBall());

			if (isBallVisible() && getCachedBallPos().distanceTo(getPos()) > distanceToBall)
			{
				triggerEvent(EEvent.SUCCESS);
			}
		}
	}

	private class ApproachBall extends AState
	{
		TimestampTimer fixRotationTimer = new TimestampTimer(0);
		TimestampTimer failureTimer = new TimestampTimer(0);
		boolean isStateSwitchAllowed;
		boolean triedReorienting;


		@Override
		public void doEntryActions()
		{
			isStateSwitchAllowed = isStateSwitchAllowed();
			triedReorienting = false;
			failureTimer.reset();
		}


		@Override
		public void doUpdate()
		{
			fixRotationTimer.setDuration(approachFixedRotationTime);
			failureTimer.setDuration(approachFailureTime);


			if (getPos().distanceTo(getCachedBallPos()) < Geometry.getBotRadius() + approachNoStrategySwitchDistance)
			{
				fixRotationTimer.update(getWorldFrame().getTimestamp());
				failureTimer.update(getWorldFrame().getTimestamp());
			} else
			{
				fixRotationTimer.reset();
				failureTimer.reset();
			}

			setKickParams(KickParams.disarm().withDribblerMode(usedDribblerMode));

			if (cachedBallDirection.isZeroVector())
			{
				doLyingApproach();
			} else
			{
				doRollingApproach();
			}

			if (isTouchingBall())
			{
				triggerEvent(EEvent.SUCCESS);
			} else if (failureTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				triggerEvent(EEvent.FAILURE);
			}
		}


		public void doRollingApproach()
		{
			ILine ballLine = Lines.lineFromDirection(getCachedBallPos(), getCachedBallVel());
			double orthogonalSpeed = cachedBallDirection.getNormalVector().projectOntoThis(getVel()).getLength();
			if (ballLine.distanceTo(getPos()) > approachStopPrimaryDirectionDistance
					|| orthogonalSpeed > approachStopPrimaryOrthogonalVelocity)
			{
				updateLookAtTarget(getCachedBallPos());
				getMoveConstraints().setPrimaryDirection(cachedBallDirection);
			} else
			{
				if (!fixRotationTimer.isRunning())
				{
					// Far away -> Rotation not yet fixed
					updateTargetAngle(cachedBallDirection.getAngle());
				} else if (fixRotationTimer.isTimeUp(getWorldFrame().getTimestamp()) && isBallVisible())
				{
					// Too long close -> stop fixing the rotation (if the ball is visible)
					updateLookAtTarget(getCachedBallPos());
				}
				getMoveConstraints().setPrimaryDirection(Vector2.zero());
			}

			var wantedVelocity = getCachedBallVel().addMagnitude(approachSpeedDiff);
			if (approachRollingBallUseLegacyCalculations)
			{
				var targetSpeed = wantedVelocity.getLength2();
				var ballSlideAcc = -Geometry.getBallParameters().getAccSlide() / 1000;
				var dist = targetSpeed * targetSpeed / ballSlideAcc / 2 * 1000;
				var dest = getCachedBallPos().addNew(wantedVelocity.scaleToNew(dist));
				updateDestination(validDest(dest));
			} else
			{
				updateCatchupDestinationNew(getCachedBallPos(), wantedVelocity);
			}
		}


		public void doLyingApproach()
		{
			getMoveConstraints().setPrimaryDirection(Vector2.zero());

			updateDestination(validDest(getBall().getTrajectory().getPosByTime(0.1).getXYVector()));
			updateLookAtTarget(getBall());
		}


		@Override
		public void doExitActions()
		{
			getMoveConstraints().setPrimaryDirection(Vector2.zero());
		}
	}

	private class AcquireControl extends AState
	{
		TimestampTimer timeoutTime = new TimestampTimer(0);
		TimestampTimer touchingTime = new TimestampTimer(0);
		IVector2 startPosition;
		IVector2 startVelocity;
		double angle;


		@Override
		public void doEntryActions()
		{
			timeoutTime.reset();
			timeoutTime.setDuration(acquireControlTimeout);

			touchingTime.reset();
			touchingTime.setDuration(acquireControlTractionTime);

			startPosition = getTBot().getPos();
			startVelocity = getCachedBallVel();
			angle = calcStopAngle();
		}


		@Override
		public void doUpdate()
		{
			setKickParams(KickParams.disarm().withDribblerMode(usedDribblerMode));
			var tNow = getWorldFrame().getTimestamp();
			timeoutTime.update(tNow);

			// Keep moving with constant velocity on a straight line till the ball is controlled
			var timePassed = timeoutTime.getCurrentTime(tNow);
			var expectedBotPositionNow = startPosition.addNew(startVelocity.multiplyNew(timePassed * 1000));
			updateCatchupDestinationNew(expectedBotPositionNow, startVelocity);

			var velLength = startVelocity.getLength2();
			var breakDistance = 1000 * (0.5f * velLength * velLength / getTBot().getMoveConstraints().getAccMax());

			updateDestination(validDest(expectedBotPositionNow.add(startVelocity.scaleToNew(breakDistance))));
			updateTargetAngle(angle);

			if (isTouchingBall())
			{
				touchingTime.update(tNow);
			} else
			{
				touchingTime.reset();
			}

			if (touchingTime.isTimeUp(tNow))
			{
				triggerEvent(EEvent.SUCCESS);
			} else if (timeoutTime.isTimeUp(tNow))
			{
				triggerEvent(EEvent.FAILURE);
			}
		}
	}

	private class StopBall extends AState
	{
		TimestampTimer failureTimer = new TimestampTimer(0.1);
		double angle;
		IVector2 pos;


		@Override
		public void doEntryActions()
		{
			angle = calcStopAngle();
			pos = getPos();
		}


		@Override
		public void doUpdate()
		{
			setKickParams(KickParams.disarm().withDribblerMode(usedDribblerMode));

			getMoveConstraints().setAccMax(stopAccMax);
			getMoveConstraints().setBrkMax(stopAccMax);
			setComeToAStop(true);
			updateTargetAngle(angle);
			updateDestination(validDest(pos));

			var tNow = getWorldFrame().getTimestamp();
			if (isTouchingBall())
			{
				failureTimer.reset();
			} else
			{
				failureTimer.update(tNow);
			}

			if (failureTimer.isTimeUp(tNow))
			{
				triggerEvent(EEvent.FAILURE);
			}
		}


		@Override
		public void doExitActions()
		{
			getMoveConstraints().setAccMax(getBot().getBotParams().getMovementLimits().getAccMax());
			getMoveConstraints().setBrkMax(getBot().getBotParams().getMovementLimits().getBrkMax());
			setComeToAStop(false);
		}
	}
}
