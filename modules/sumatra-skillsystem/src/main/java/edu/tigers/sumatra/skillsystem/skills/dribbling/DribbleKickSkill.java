/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.dribbling;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.awt.Color;


/**
 * Dribble and kick the ball, aka finisher.
 */
public class DribbleKickSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.2", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.2;

	@Configurable(defValue = "0.08", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.08;

	@Configurable(defValue = "1.5")
	private static double initialVelMax = 1.5;

	@Configurable(defValue = "5.0")
	private static double initialAccMaxW = 5.0;

	@Configurable(defValue = "4.0")
	private static double initialVelMaxW = 4.0;

	@Configurable(defValue = "25.0")
	private static double kickNowAccMaxW = 25.0;

	@Configurable(defValue = "10.0")
	private static double kickNowVelMaxW = 10.0;

	@Configurable(defValue = "1.5")
	private static double initialAccMax = 1.5;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	private final TimestampTimer changeStateTimer = new TimestampTimer(0.1);

	private final TimestampTimer safeOrientationLimitsTimer = new TimestampTimer(0.25);

	@Setter
	private IVector2 destination;

	@Setter
	private IVector2 target;

	@Setter
	private boolean kickIfTargetOrientationReached = false;

	@Setter
	private double forceKickSpeed = 0.0;
	private double finalTargetKickSpeed;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
		safeOrientationLimitsTimer.reset();
	}


	@Override
	public void doUpdate()
	{
		// Base stats
		getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);

		updateDestination(destination);
		double finalTargetOrientation = calculateAdjustedTargetOrientation();
		updateTargetAngle(finalTargetOrientation);

		var orientation = getOrientationFromFilter();
		if (kickIfTargetOrientationReached)
		{
			// Robot wants to kick, but has to orientate first. We want to do this a little bit faster now!
			getMoveConstraints().setAccMaxW(kickNowAccMaxW);
			getMoveConstraints().setVelMaxW(kickNowVelMaxW);

			var angleTolerance = Math.max(roughAngleTolerance, -roughAngleTolerance);
			targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);
			targetAngleReachedChecker.update(
					finalTargetOrientation,
					orientation,
					getWorldFrame().getTimestamp()
			);
		}

		safeOrientationLimitsTimer.update(getWorldFrame().getTimestamp());
		if (!safeOrientationLimitsTimer.isTimeUp(getWorldFrame().getTimestamp()) && !kickIfTargetOrientationReached)
		{
			// we reduce our limits if we just started to dribble, the ball has still weak binding to the robot.
			getMoveConstraints().setAccMaxW(initialAccMaxW);
			getMoveConstraints().setVelMaxW(initialVelMaxW);
			getMoveConstraints().setAccMax(initialAccMax);
			getMoveConstraints().setVelMax(initialVelMax);
		}

		getShapes().get(ESkillShapesLayer.DRIBBLING_KICK).add(new DrawableArrow(getPos(), target.subtractNew(getPos())));
		IVector2 adjustedOrientationDirection = Vector2.fromAngle(finalTargetOrientation)
				.scaleToNew(target.subtractNew(getPos()).getLength());
		getShapes().get(ESkillShapesLayer.DRIBBLING_KICK)
				.add(new DrawableArrow(getPos(), adjustedOrientationDirection).setColor(Color.GREEN.darker()));
		getShapes().get(ESkillShapesLayer.DRIBBLING_KICK)
				.add(new DrawableArrow(getPos(), Vector2.fromAngle(getOrientationFromFilter()).scaleTo(1000))
						.setColor(Color.ORANGE));

		// try being a bit more precise than the pass range, but have a minimum tolerance
		if ((isFocused() && kickIfTargetOrientationReached))
		{
			// kick now!
			setKickParams(KickParams.straight(finalTargetKickSpeed).withDribblerMode(EDribblerMode.HIGH_POWER));
			getShapes().get(ESkillShapesLayer.DRIBBLING_KICK)
					.add(new DrawableAnnotation(getPos(), "is focused", Vector2.fromY(50)));
		} else if (forceKickSpeed > 0)
		{
			var kickSpeed = SumatraMath.min(forceKickSpeed, finalTargetKickSpeed);
			setKickParams(KickParams.straight(kickSpeed).withDribblerMode(EDribblerMode.HIGH_POWER));
			getShapes().get(ESkillShapesLayer.DRIBBLING_KICK)
					.add(new DrawableAnnotation(getPos(), "force kick", Vector2.fromY(50)));
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));
		}

		super.doUpdate();

		if (!getTBot().getBallContact().hasContactFromVisionOrBarrier())
		{
			changeStateTimer.update(getWorldFrame().getTimestamp());
			if (changeStateTimer.isTimeUp(getWorldFrame().getTimestamp()))
			{
				setSkillState(ESkillState.FAILURE);
			}
		} else
		{
			changeStateTimer.reset();
		}
	}


	private double calculateAdjustedTargetOrientation()
	{
		var ballOffset = getTBot().getBotKickerPos().subtractNew(getTBot().getPos()).scaleToNew(Geometry.getBallRadius());
		double plannedBallVelAngle = target.subtractNew(getTBot().getBotKickerPos().addNew(ballOffset)).getAngle();
		IVector2 plannedKick = Vector2.fromAngleLength(plannedBallVelAngle,
				KickParams.maxStraight().getKickSpeed());

		IVector2 kick = plannedKick.subtractNew(getVel());
		finalTargetKickSpeed = kick.getLength();
		return kick.getAngle();
	}


	private boolean isFocused()
	{
		return targetAngleReachedChecker.isReached();
	}


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}
}
