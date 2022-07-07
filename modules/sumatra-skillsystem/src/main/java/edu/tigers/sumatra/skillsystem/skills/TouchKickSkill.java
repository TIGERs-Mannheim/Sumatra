/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
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
	@Configurable(comment = "The max margin to the ball for destinations", defValue = "20.0")
	private static double maxMarginToBall = 20.0;

	@Getter
	@Configurable(defValue = "0.15", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.15;

	@Configurable(defValue = "0.15", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.15;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	@Setter
	private double marginToTheirPenArea = 0;

	private DoubleChargingValue chargingValue;
	private IVector2 initBallPos;


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


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		ballStabilizer.update(getBall(), getTBot());
		initBallPos = ballStabilizer.getBallPos();
		var dist2Ball = isNearBall() ? -10 : 0;
		chargingValue = new DoubleChargingValue(
				dist2Ball,
				200,
				-2000,
				-100,
				0
		);
	}


	private boolean isNearBall()
	{
		return getBallPos().distanceTo(getTBot().getBotKickerPos()) < (Geometry.getBallRadius() + 10);
	}


	@Override
	public void doUpdate()
	{
		ballStabilizer.update(getBall(), getTBot());

		var finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
		var targetOrientation = getTargetOrientation(finalTargetOrientation);
		var orientation = getOrientationFromFilter();
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
		
		IVector2 dest = AroundBallCalc
				.aroundBall()
				.withBallPos(getBallPosByTime(0.5))
				.withTBot(getTBot())
				.withDestination(getDestination(dist2Ball))
				.withMaxMargin(maxMarginToBall)
				.withMinMargin(dist2Ball)
				.build()
				.getAroundBallDest();

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
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.DEFAULT));
		}

		if (initBallPos.distanceTo(ballStabilizer.getBallPos()) > 500)
		{
			setSkillState(ESkillState.FAILURE);
		} else if (getBall().getVel().getLength2() > maxBallSpeed)
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
		var focusedTxt = isFocused() ? "focused" : isRoughlyFocussed() ? "roughly focussed" : "unfocused";
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


	private IVector2 getBallPosByTime(final double lookahead)
	{
		return ballStabilizer.getBallPos(lookahead);
	}


	private double getTargetOrientation(double finalTargetOrientation)
	{
		double currentDirection = getBall().getPos().subtractNew(getPos()).getAngle(0);
		double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
		return currentDirection + Math.signum(diff) * Math.min(Math.abs(diff), 0.4);
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
