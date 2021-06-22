/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.BallStabilizer;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.PositionValidator;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


public class ApproachAndStopBallSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.6")
	private static double maxLookahead = 0.6;

	@Configurable(comment = "The target velocity difference between bot and ball to aim for when trying catch up ball", defValue = "0.6")
	private static double catchUpBallTargetVelDiff = 0.6;

	@Configurable(comment = "Maximum dribble speed (when dribbling enabled)", defValue = "10000")
	private static int maxDribbleSpeed = 10000;

	@Configurable(comment = "Margin to our Penalty Area", defValue = "100.0")
	private static double marginToOurPenArea = 100.0;

	private final Hysteresis ballSpeedHysteresis = new Hysteresis(0.1, 0.6);
	private final BallStabilizer ballStabilizer = new BallStabilizer();
	private final TimestampTimer dribbleTimer = new TimestampTimer(0.5);
	private final PositionValidator positionValidator = new PositionValidator();
	private final DoubleChargingValue lookaheadChargingValue = new DoubleChargingValue(
			0,
			1.0,
			-0.9,
			0,
			maxLookahead);

	@Setter
	private double marginToTheirPenArea = 0;

	private IVector2 primaryDirection;


	public ApproachAndStopBallSkill ignorePenAreas()
	{
		getMoveCon().setPenaltyAreaOurObstacle(false);
		getMoveCon().setPenaltyAreaTheirObstacle(false);
		return this;
	}


	private void updateDribbler()
	{
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

		double dribbleSpeed = maxDribbleSpeed;
		if (dribbleTimer.isTimeUp(getWorldFrame().getTimestamp()))
		{
			dribbleSpeed = 0;
		}
		setKickParams(KickParams.disarm().withDribbleSpeed(dribbleSpeed));
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
		ballStabilizer.update(getBall(), getTBot());

		if (getBall().getTrajectory().getTravelLineRolling().distanceTo(getPos()) < 100)
		{
			// do not respect other bots, when on ball line
			getMoveCon().setBotsObstacle(false);
		}

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		positionValidator.getMarginToPenArea().put(ETeam.TIGERS, marginToOurPenArea);

		primaryDirection = getBall().getVel();
		IVector2 ballPos = getBallPos();
		IVector2 botPos = getTBot().getFilteredState().orElse(getTBot().getBotState()).getPos();
		if (!Line.fromDirection(botPos, primaryDirection).isPointInFront(ballPos))
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

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL).add(new DrawableAnnotation(getPos(),
				String.format("%.2f|%.2f", lookahead, botBallSpeedDiff))
				.withOffset(Vector2.fromY(300)));

		super.doUpdate();

		updateDribbler();
		updateSkillState();

		getShapes().get(ESkillShapesLayer.APPROACH_AND_STOP_BALL_SKILL)
				.add(new DrawableLine(Line.fromDirection(getBallPos(), primaryDirection.scaleToNew(1000))));
	}


	private void updateSkillState()
	{
		if (ballStoppedByBot())
		{
			setSkillState(ESkillState.SUCCESS);
		} else if (ballStoppedMoving() || ballMovesTowardsMe())
		{
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
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
		if (ballLine.distanceTo(getPos()) < 30)
		{
			updateTargetAngle(primaryDirection.getAngle());
		} else
		{
			updateLookAtTarget(getBall());
		}
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
		return getTBot().hadBallContact(0.1);
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
