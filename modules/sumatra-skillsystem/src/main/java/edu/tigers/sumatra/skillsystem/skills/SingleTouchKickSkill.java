/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.time.TimestampTimer;
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

	@Configurable(defValue = "-150", comment = "Max dist to push ball")
	private static double maxPushDist = -150;

	@Configurable(defValue = "-100", comment = "Charge rate for min margin charger")
	private static double minMarginChargeRate = -100;

	@Configurable(defValue = "50", comment = "Lower threshold for min margin charger")
	private static double minMarginLowerThreshold = 50;

	@Configurable(defValue = "80", comment = "Upper threshold for min margin charger")
	private static double minMarginUpperThreshold = 80;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	private final TimestampTimer readyTimeoutTimer = new TimestampTimer(0.3);

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


	private boolean isReady()
	{
		boolean ready = isReadyAndFocused();
		if (ready)
		{
			readyTimeoutTimer.start(getWorldFrame().getTimestamp());
			return true;
		}
		return readyTimeoutTimer.isRunning() && !readyTimeoutTimer.isTimeUp(getWorldFrame().getTimestamp());
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
		initBallPos = getBall().getPos();
		dist2Ball = minDistBeforeKick;

		minMarginChargeValue = MinMarginChargeValue.aMinMargin()
				.withDefaultValue(0)
				.withInitValue(dist2Ball)
				.withChargeRate(minMarginChargeRate)
				.withLowerThreshold(minMarginLowerThreshold)
				.withUpperThreshold(minMarginUpperThreshold)
				.withLimit(maxPushDist)
				.build();

		readyTimeoutTimer.reset();
	}


	@Override
	public void doUpdate()
	{
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

		if (readyForKick)
		{
			double kickSpeed = getKickSpeed();
			setKickParams(KickParams.of(desiredKickParams.getDevice(), kickSpeed));
		} else
		{
			setKickParams(KickParams.disarm());
		}

		getShapes().get(ESkillShapesLayer.KICK_SKILL).add(new DrawablePoint(getBallPos(), Color.green));
		getShapes().get(ESkillShapesLayer.KICK_SKILL)
				.add(new DrawableLine(getBall().getPos(), target, getBotId().getTeamColor().getColor()));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
				Lines.segmentFromOffset(dest, Vector2.fromAngle(getOrientationFromFilter()).scaleTo(5000)), Color.BLACK));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
				Lines.segmentFromOffset(dest, Vector2.fromAngle(targetOrientation).scaleTo(5000)), Color.RED));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableAnnotation(
				getPos(), "Focussed: " + isReadyAndFocused()).withOffset(Vector2.fromX(200)));
		getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableAnnotation(
				getPos(), "Ready: " + isReady()).withOffset(Vector2.fromXY(200, 100)));

		if (initBallPos.distanceTo(getBall().getPos()) > 500)
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
		} else if (!Geometry.getFieldWBorders().withMargin(-200).isPointInShape(getBallPos()))
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
		if (isReady())
		{
			double dist = dest.distanceTo(getPos());
			getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(
					new DrawableAnnotation(getPos(), "dist: " + Math.round(dist)).withOffset(Vector2.fromX(-200)));
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
