/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


public class ApproachAndStopBallSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.6")
	private static double maxLookahead = 0.6;

	@Configurable(comment = "The target velocity difference between bot and ball to aim for when trying catch up ball", defValue = "0.8")
	private static double catchUpBallTargetVelDiff = 0.8;

	@Configurable(defValue = "0.1")
	private static double contactFor = 0.1;

	@Configurable(comment = "Margin to our Penalty Area", defValue = "100.0")
	private static double marginToOurPenArea = 100.0;

	@Configurable(comment = "Brake acc when stopping the ball", defValue = "2.0")
	private static double accMaxBrake = 2.0;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.1, 0.6);
	private final PositionValidator positionValidator = new PositionValidator();
	private final TimestampTimer stopBallTimer = new TimestampTimer(0.1);
	private final DoubleChargingValue lookaheadChargingValue = new DoubleChargingValue(
			0,
			1.0,
			-0.9,
			0,
			maxLookahead);

	@Setter
	private double marginToTheirPenArea = 0;

	private IVector2 primaryDirection;


	private void updateDribbler()
	{
		EDribblerMode dribbleMode = EDribblerMode.OFF;
		if (getPos().distanceTo(getBallPos()) < 300 && !ballStoppedByBot())
		{
			dribbleMode = EDribblerMode.DEFAULT;
		}
		setKickParams(KickParams.disarm().withDribblerMode(dribbleMode));
	}


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
		if (getBall().getPos().distanceTo(getPos()) < 200)
		{
			// Do not switch if close to ball
			return false;
		}
		var ballDir = getBall().getVel();
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.deg2rad(60);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGameStateObstacle(false);
		updateLookAtTarget(getBall());
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
		}
		IVector2 ballPos = getBallPos();
		IVector2 botPos = getTBot().getFilteredState().orElse(getTBot().getBotState()).getPos();
		if (!Lines.halfLineFromDirection(botPos, primaryDirection).isPointInFront(ballPos))
		{
			primaryDirection = primaryDirection.multiplyNew(-1);
		}
		getMoveConstraints().setPrimaryDirection(primaryDirection);

		updateTargetOrientation();

		double botBallSpeedDiff = getTBot().getVel().subtractNew(getBall().getVel()).getLength2();
		boolean lookingToBall =
				Math.abs(AngleMath.difference(getBall().getPos().subtractNew(getPos()).getAngle(0), getAngle())) < 0.3;
		double lookahead = lookingToBall ? calcLookahead(botBallSpeedDiff) : 0;

		updateDestination(lookahead);
		stayStillIfBotHasBallContact();

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL).add(new DrawableAnnotation(getPos(),
				String.format("%.2f|%.2f", lookahead, botBallSpeedDiff))
				.withOffset(Vector2.fromY(300)));

		super.doUpdate();

		updateDribbler();
		updateSkillState();

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL)
				.add(new DrawableLine(Lines.segmentFromOffset(getBallPos(), primaryDirection.scaleToNew(1000))));
	}


	private void stayStillIfBotHasBallContact()
	{
		setComeToAStop(isTouchingBall());
		if (isTouchingBall())
		{
			getMoveConstraints().setAccMax(accMaxBrake);
		} else
		{
			getMoveConstraints().setAccMax(getBot().getBotParams().getMovementLimits().getAccMax());
		}
	}


	private void updateSkillState()
	{
		boolean stopped = ballStoppedByBot();
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


	private void updateDestination(double lookahead)
	{
		IVector2 dest = getBallPos(lookahead).subtractNew(primaryDirection
				.scaleToNew(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius()));

		dest = positionValidator.movePosInFrontOfOpponent(dest);
		dest = positionValidator.movePosInsideFieldWrtBallPos(dest);
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);
		updateDestination(dest);
	}


	private void updateTargetOrientation()
	{
		ILine ballLine = Lines.lineFromDirection(getBall().getPos(), getBall().getVel());
		if (ballLine.distanceTo(getPos()) < 50)
		{
			updateTargetAngle(primaryDirection.getAngle());
		} else
		{
			updateLookAtTarget(getBall());
		}
	}


	private double calcLookahead(final double botBallSpeedDiff)
	{
		var hysteresis = 0.05;
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
		return getTBot().getBallContact().getContactDuration() > contactFor;
	}


	private IVector2 getBallPos(double lookahead)
	{
		return getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
	}


	private IVector2 getBallPos()
	{
		return getBall().getPos();
	}
}
