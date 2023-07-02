/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.EDribbleTractionState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue;
import edu.tigers.sumatra.skillsystem.skills.util.DoubleChargingValue.ChargeMode;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Color;


/**
 * The 'normal' kick skill for in-game kicks.
 */
@NoArgsConstructor
public class TouchKickSkill extends ATouchKickSkill
{
	@Configurable(comment = "The max margin to the ball for destinations", defValue = "70.0")
	private static double maxMarginToBall = 70.0;

	@Getter
	@Configurable(defValue = "0.15", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.15;

	@Configurable(defValue = "0.15", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.15;

	@Configurable(defValue = "OFF")
	private static EDribblerMode dribblerMode = EDribblerMode.OFF;

	@Configurable(defValue = "10")
	private static double maxAngularVelocity = 10;

	@Configurable(defValue = "0.3")
	private static double aroundBallLookahead = 0.3;

	@Configurable(defValue = "0.3")
	private static double maxTargetOrientationStep = 0.3;

	@Configurable(defValue = "-100")
	private static double maxPushDistance = -100;

	@Configurable(defValue = "-20")
	private static double nearBallPushDistance = -20;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	@Setter
	private double marginToTheirPenArea = 0;

	private DoubleChargingValue chargingValue;
	private IVector2 initBallPos;
	private IVector2 strongDribbleRobotPos;
	private double maxBallSpeedThreshold;


	public TouchKickSkill(final IVector2 target, final KickParams desiredKickParams)
	{
		this.target = target;
		this.desiredKickParams = desiredKickParams;
	}


	private boolean isFocused()
	{
		return targetAngleReachedChecker.isReached();
	}


	private boolean isRoughlyFocussed()
	{
		return isNearBall() && targetAngleReachedChecker.isRoughlyFocussed();
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		getMoveConstraints().setVelMaxW(maxAngularVelocity);
		initBallPos = getBall().getPos();
		strongDribbleRobotPos = getPos();
		var dist2Ball = isNearBall() ? nearBallPushDistance : 0;
		chargingValue = new DoubleChargingValue(
				dist2Ball,
				200,
				-2000,
				maxPushDistance,
				0
		);
		maxBallSpeedThreshold = Math.max(maxBallSpeed, getBall().getVel().getLength2() + 0.1);
	}


	private boolean isNearBall()
	{
		return getBallPos().distanceTo(getTBot().getBotKickerPos()) < (Geometry.getBallRadius() + 10);
	}


	@Override
	public void doUpdate()
	{
		var finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
		var targetOrientation = getTargetOrientation(finalTargetOrientation);
		var orientation = getTBot().getOrientation();
		var passRangeTolerance = passRange / 2;
		// try being a bit more precise than the pass range, but have a minimum tolerance
		var angleTolerance = Math.max(roughAngleTolerance, passRangeTolerance - roughAngleTolerance);
		targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);
		var angleDiff = targetAngleReachedChecker.update(
				finalTargetOrientation,
				orientation,
				getWorldFrame().getTimestamp()
		);

		chargingValue.setChargeMode(isRoughlyFocussed() ? ChargeMode.DECREASE : ChargeMode.INCREASE);
		chargingValue.update(getWorldFrame().getTimestamp());
		var dist2Ball = chargingValue.getValue();

		IVector2 dest = findDest(dist2Ball);

		positionValidator.update(getWorldFrame(), getMoveCon());
		positionValidator.getMarginToPenArea().put(ETeam.OPPONENTS, marginToTheirPenArea);
		dest = positionValidator.movePosInsideField(dest);
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

		updateDestination(dest);
		updateTargetAngle(targetOrientation);

		super.doUpdate();

		if (isFocused())
		{
			setKickParams(KickParams.of(desiredKickParams.getDevice(), getKickSpeed()));
		} else if (dribblerMode == EDribblerMode.HIGH_POWER &&
				getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
		} else if (dribblerMode == EDribblerMode.OFF)
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
		}

		if (initBallPos.distanceTo(getBall().getPos()) > 500)
		{
			setSkillState(ESkillState.FAILURE);
		} else if (getBall().getVel().getLength2() > maxBallSpeedThreshold)
		{
			if (AngleMath.diffAbs(getBall().getVel().getAngle(), targetOrientation) < AngleMath.DEG_045_IN_RAD)
			{
				setSkillState(ESkillState.SUCCESS);
			} else
			{
				setSkillState(ESkillState.FAILURE);
			}
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}

		drawShapes(orientation, angleDiff, angleTolerance);
	}


	private IVector2 findDest(double dist2Ball)
	{
		if (getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
		{
			return strongDribbleRobotPos;
		}
		strongDribbleRobotPos = getPos();
		return AroundBallCalc
				.aroundBall()
				.withBallPos(getBallPosByTime(aroundBallLookahead))
				.withTBot(getTBot())
				.withDestination(getDestination(dist2Ball))
				.withMaxMargin(maxMarginToBall)
				.withMinMargin(dist2Ball)
				.build()
				.getAroundBallDest();
	}


	private void drawShapes(double orientation, double angleDiff, double angleTolerance)
	{
		var ballPos = getBallPos();
		var kickDir = target.subtractNew(ballPos);
		var currentTolerance = targetAngleReachedChecker.getCurrentTolerance();
		var toleranceLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(angleTolerance));
		var toleranceLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-angleTolerance));
		var focusLine1 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(currentTolerance));
		var focusLine2 = Lines.segmentFromOffset(ballPos, kickDir.turnNew(-currentTolerance));
		var targetLine = Lines.segmentFromPoints(getBall().getPos(), target);
		var orientationOffset = Vector2.fromAngleLength(orientation, targetLine.getLength());
		var currentOrientationLine = Lines.segmentFromOffset(getPos(), orientationOffset);
		var teamColor = getBotId().getTeamColor().getColor();
		var focusedTxt = createFocusedText();
		var angleDiffTxt = String.format("%.2f -> %s", angleDiff, focusedTxt);
		var txtOffset = Vector2f.fromY(200);

		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(),
				String.format("dist2Ball: %.2f", chargingValue.getValue())).withOffset(Vector2.fromY(300)));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(ballPos, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine1, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(toleranceLine2, Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine1, Color.magenta));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(focusLine2, Color.magenta));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(targetLine, teamColor));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableLine(currentOrientationLine, Color.orange));
		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawableAnnotation(getPos(), angleDiffTxt, txtOffset));
	}


	private String createFocusedText()
	{
		if (isFocused())
		{
			return "focused";
		}
		return isRoughlyFocussed() ? "roughly focussed" : "unfocused";
	}


	private IVector2 getBallPosByTime(final double lookahead)
	{
		return getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
	}


	private double getTargetOrientation(double finalTargetOrientation)
	{
		if (getTBot().getRobotInfo().getDribbleTraction() == EDribbleTractionState.STRONG)
		{
			return finalTargetOrientation;
		}
		double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
		double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
		return currentDirection + Math.signum(diff) * Math.min(Math.abs(diff), maxTargetOrientationStep);
	}


	private IVector2 getDestination(final double margin)
	{
		return LineMath.stepAlongLine(getBall().getPos(), target, -getDistance(margin));
	}


	private double getDistance(final double margin)
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + margin;
	}
}
