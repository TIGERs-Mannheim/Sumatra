/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Color;


/**
 * Kick with a single touch (taking care to not double touching the ball)
 */
@NoArgsConstructor
public class SingleTouchKickSkill extends ATouchKickSkill
{
	@Configurable(comment = "Speed in chill mode", defValue = "2.0")
	private static double chillVel = 2.0;

	@Configurable(comment = "Acceleration in chill model", defValue = "2.0")
	private static double chillAcc = 2.0;

	@Configurable(comment = "The distance between kicker and ball to keep before kicking the ball", defValue = "15.0")
	private static double minDistBeforeKick = 15;

	@Configurable(defValue = "120.0")
	private static double maxAroundBallMargin = 120;

	@Configurable(defValue = "0.3", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.3;

	@Configurable(defValue = "1.0", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 1.0;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	@Setter
	private boolean readyForKick = true;

	private double dist2Ball = 0;
	private MinMarginChargeValue minMarginChargeValue;
	private IVector2 initBallPos;


	public SingleTouchKickSkill(final IVector2 target, final KickParams desiredKickParams)
	{
		this.target = target;
		this.desiredKickParams = desiredKickParams;
	}


	private boolean isReadyAndFocused()
	{
		double targetOrientation = target.subtractNew(getBallPos()).getAngle(getAngle());
		return readyForKick
				&& isOrientationReached(targetOrientation)
				&& (getVel().getLength2() < 0.1
				|| Math.abs(AngleMath.difference(getVel().getAngle(), targetOrientation)) < 0.1);
	}


	private boolean isOrientationReached(double targetOrientation)
	{
		targetAngleReachedChecker.update(targetOrientation, getOrientationFromFilter(), getWorldFrame().getTimestamp());
		return targetAngleReachedChecker.isReached();
	}


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveConstraints().setVelMax(chillVel);
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGameStateObstacle(false);
		ballStabilizer.update(getBall(), getTBot());
		initBallPos = ballStabilizer.getBallPos();
		dist2Ball = minDistBeforeKick;

		minMarginChargeValue = MinMarginChargeValue.aMinMargin()
				.withDefaultValue(0)
				.withInitValue(dist2Ball)
				.withChargeRate(-100)
				.withLowerThreshold(50)
				.withUpperThreshold(70)
				.withLimit(-100)
				.build();
	}


	@Override
	public void doUpdate()
	{
		ballStabilizer.update(getBall(), getTBot());

		if (getVel().getLength2() <= getMoveConstraints().getVelMax())
		{
			getMoveConstraints().setAccMax(chillAcc);
		}

		IVector2 dest = AroundBallCalc
				.aroundBall()
				.withBallPos(getBallPos())
				.withTBot(getTBot())
				.withDestination(getDestination(0))
				.withMaxMargin(maxAroundBallMargin)
				.withMinMargin(dist2Ball)
				.build()
				.getAroundBallDest();
		dist2Ball = getMinMargin(dest);

		positionValidator.update(getWorldFrame(), getMoveCon());
		dest = positionValidator.movePosOutOfPenAreaWrtBall(dest);

		updateDestination(dest);
		double targetOrientation = getTargetOrientation();
		updateTargetAngle(targetOrientation);
		super.doUpdate();

		if (readyForKick && isOrientationReached(targetOrientation))
		{
			double kickSpeed = getKickSpeed();
			setKickParams(KickParams.of(desiredKickParams.getDevice(), kickSpeed));
		} else
		{
			setKickParams(KickParams.disarm());
		}

		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(getBallPos(), Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableLine(Line.fromPoints(getBall().getPos(), target),
						getBotId().getTeamColor().getColor()));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
				Line.fromDirection(dest, Vector2.fromAngle(getOrientationFromFilter()).scaleTo(5000)), Color.BLACK));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
				Line.fromDirection(dest, Vector2.fromAngle(targetOrientation).scaleTo(5000)), Color.RED));

		if (initBallPos.distanceTo(ballStabilizer.getBallPos()) > 500)
		{
			setSkillState(ESkillState.FAILURE);
		} else if (getBall().getVel().getLength2() > maxBallSpeed)
		{
			if (AngleMath.difference(getBall().getVel().getAngle(), targetOrientation) < AngleMath.DEG_045_IN_RAD)
			{
				setSkillState(ESkillState.SUCCESS);
			} else
			{
				setSkillState(ESkillState.FAILURE);
			}
		} else if (!Geometry.getField().isPointInShape(getBallPos()))
		{
			setSkillState(ESkillState.FAILURE);
		} else
		{
			setSkillState(ESkillState.IN_PROGRESS);
		}
	}


	private double getTargetOrientation()
	{
		double finalTargetOrientation = target.subtractNew(getBallPos()).getAngle(0);

		double currentDirection = getBallPos().subtractNew(getPos()).getAngle(0);
		double diff = AngleMath.difference(finalTargetOrientation, currentDirection);
		double alteredDiff = Math.signum(diff) * Math.max(0, Math.abs(diff) - 0.4);

		return finalTargetOrientation - alteredDiff;
	}


	private double getMinMargin(final IVector2 dest)
	{
		if (isReadyAndFocused())
		{
			double dist = dest.distanceTo(getPos());
			minMarginChargeValue.updateMinMargin(dist, getWorldFrame().getTimestamp());
			return minMarginChargeValue.getMinMargin();
		}
		minMarginChargeValue.reset();
		return minDistBeforeKick;
	}


	private IVector2 getDestination(double margin)
	{
		return LineMath.stepAlongLine(getBallPos(), target, -getDistance(margin));
	}


	private double getDistance(double margin)
	{
		return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
	}
}
