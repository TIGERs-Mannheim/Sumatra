/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


public class ApproachAndStopBallSkill extends AMoveToSkill
{
	@Configurable(comment = "Margin to our Penalty Area", defValue = "100.0")
	private static double marginToOurPenArea = 100.0;

	@Configurable(comment = "Brake acc when stopping the ball", defValue = "3.0")
	private static double accMaxBrake = 3.0;

	@Configurable(comment = "Lower threshold for ball speed hysteresis", defValue = "0.1")
	private static double ballSpeedHysteresisLower = 0.1;

	@Configurable(comment = "Upper threshold for ball speed hysteresis", defValue = "0.6")
	private static double ballSpeedHysteresisUpper = 0.6;

	@Configurable(comment = "Duration [s] for stopBallTimer", defValue = "0.1")
	private static double stopBallTimerDuration = 0.1;

	@Configurable(comment = "Velocity difference [m/s] between ball speed and robot speed to aim for when hitting the ball", defValue = "1.0")
	private static double ballSpeedTargetDiff = 1.0;

	@Configurable(comment = "Velocity at which early succeed timer triggers [m/s]", defValue = "1.5")
	private static double succeedEarlyVel = 1.5;

	@Configurable(comment = "If ball is closer than this distance [mm], ignore when ball is moving away", defValue = "50")
	private static double noSwitchDistCloseToBall = 50;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(ballSpeedHysteresisLower, ballSpeedHysteresisUpper);
	private final PositionValidator positionValidator = new PositionValidator();
	private final TimestampTimer stopBallTimer = new TimestampTimer(stopBallTimerDuration);

	@Setter
	private double marginToTheirPenArea = 0;

	@Setter
	private boolean succeedEarly = false;

	private IVector2 primaryDirection;


	private boolean ballStoppedByBot()
	{
		return ballSpeedHysteresis.isLower() && ballIsNearRobotKicker();
	}


	private boolean ballIsNearRobotKicker()
	{
		return getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < 50;
	}


	private boolean ballStoppedMoving()
	{
		return ballSpeedHysteresis.isLower();
	}


	private boolean ballMovesTowardsMe()
	{
		if (getBall().getPos().distanceTo(getTBot().getBotKickerPos()) < noSwitchDistCloseToBall)
		{
			// Do not switch if close to ball
			return false;
		}
		var ballDir = getBall().getVel();
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.deg2rad(90);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGameStateObstacle(false);

		primaryDirection = getBall().getPos().subtractNew(getPos());

		// initially the ball is moving
		ballSpeedHysteresis.update(RuleConstraints.getMaxKickSpeed());
	}


	@Override
	public void doUpdate()
	{
		ballSpeedHysteresis.update(getBall().getVel().getLength2());

		// do not respect other bots, when on ball line
		getMoveCon().setBotsObstacle((getBall().getTrajectory().distanceTo(getPos()) > 100));

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		positionValidator.getMarginToPenArea().put(ETeam.TIGERS, marginToOurPenArea);

		if (getBall().getVel().getLength2() > 0.5)
		{
			primaryDirection = getBall().getVel();
			updateTargetAngle(primaryDirection.getAngle());
		} else {
			updateLookAtTarget(getBall());
		}

		IVector2 botPos = getTBot().getFilteredState().orElse(getTBot().getBotState()).getPos();
		if (!Lines.halfLineFromDirection(botPos, primaryDirection).isPointInFront(getBall().getPos()))
		{
			primaryDirection = primaryDirection.multiplyNew(-1);
			updateLookAtTarget(getBall());
		}

		getMoveConstraints().setPrimaryDirection(primaryDirection);

		stayStillIfBotHasBallContactOrUpdateDestination();

		super.doUpdate();

		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
		updateSkillState();

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getBall().getPos(), primaryDirection.scaleToNew(1000))));
	}


	private void stayStillIfBotHasBallContactOrUpdateDestination()
	{
		setComeToAStop(isTouchingBall());
		if (isTouchingBall())
		{
			getMoveConstraints().setAccMax(accMaxBrake);
			getMoveConstraints().setBrkMax(accMaxBrake);
			updateTargetAngle(getAngle());
			updateDestination(validDest(getPos()));
		} else
		{
			getMoveConstraints().setAccMax(getBot().getBotParams().getMovementLimits().getAccMax());
			updateTargetOrientation();
			updateDestination();
		}
	}


	private void updateSkillState()
	{

		boolean stopped = ballStoppedByBot() || succeedEarly();
		if (stopped)
		{
			stopBallTimer.update(getWorldFrame().getTimestamp());
			if (stopBallTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.SUCCESS);
			} else
			{
				setSkillState(ESkillState.IN_PROGRESS);
			}
		} else if (ballStoppedMoving() || ballMovesTowardsMe())
		{
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
		if (!stopped)
		{
			stopBallTimer.reset();
		}
	}


	private boolean succeedEarly()
	{
		return succeedEarly && getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG
				&& getBall().getVel().getLength() < succeedEarlyVel;
	}


	private void updateDestination()
	{
		if (ballStoppedMoving())
		{
			return;
		}
		var targetSpeed = getBall().getVel().getLength2() + ballSpeedTargetDiff;
		var ballSlideAcc = -Geometry.getBallParameters().getAccSlide() / 1000;
		var dist = targetSpeed * targetSpeed / ballSlideAcc / 2 * 1000;
		IVector2 dest = getBall().getPos().addNew(getBall().getVel().scaleToNew(dist));
		updateDestination(validDest(dest));
	}


	private IVector2 validDest(IVector2 originalDest)
	{
		IVector2 dest = originalDest;
		dest = positionValidator.movePosInFrontOfOpponent(dest);
		dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
		return positionValidator.movePosOutOfPenAreaWrtBall(dest);
	}


	private void updateTargetOrientation()
	{
		ILine ballLine = Lines.lineFromDirection(getBall().getPos(), getBall().getVel());
		if (ballLine.distanceTo(getPos()) > 50)
		{
			updateLookAtTarget(getBall());
		} else if (getBall().getPos().distanceTo(getTBot().getBotKickerPos()) > 50)
		{
			updateTargetAngle(primaryDirection.getAngle());
		}
	}


	private boolean isTouchingBall()
	{
		return getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.LIGHT ||
				getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG;
	}
}
