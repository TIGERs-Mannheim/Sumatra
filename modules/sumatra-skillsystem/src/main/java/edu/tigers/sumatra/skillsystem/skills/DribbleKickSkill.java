/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.TargetAngleReachedChecker;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;


/**
 * Protect the ball against a given opponent
 */
public class DribbleKickSkill extends AMoveToSkill
{
	@Configurable(defValue = "0.3", comment = "The approximate tolerance when the angle is considered to be reached")
	private static double roughAngleTolerance = 0.3;

	@Configurable(defValue = "0.05", comment = "The max time to wait until angle is considered reached while within tolerance")
	private static double maxTimeTargetAngleReached = 0.05;

	@Configurable(defValue = "0.6")
	private static double maxBotVelToKick = 0.6;

	private final TargetAngleReachedChecker targetAngleReachedChecker = new TargetAngleReachedChecker(
			roughAngleTolerance, maxTimeTargetAngleReached);

	private final TimestampTimer changeStateTimer = new TimestampTimer(0.1);

	@Setter
	private IVector2 destination;

	@Setter
	private IVector2 target;

	@Setter
	private boolean targetBlocked = true;


	@Override
	public void doEntryActions()
	{
		super.doEntryActions();
		getMoveCon().setBallObstacle(false);
	}


	@Override
	public void doUpdate()
	{
		setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.HIGH_POWER));

		updateDestination(destination);
		updateLookAtTarget(target);

		getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawableArrow(getPos(), target.subtractNew(getPos())));

		var finalTargetOrientation = target.subtractNew(getBall().getPos()).getAngle(0);
		var orientation = getOrientationFromFilter();
		// try being a bit more precise than the pass range, but have a minimum tolerance
		var angleTolerance = Math.max(roughAngleTolerance, -roughAngleTolerance);
		targetAngleReachedChecker.setOuterAngleDiffTolerance(angleTolerance);
		targetAngleReachedChecker.update(
				finalTargetOrientation,
				orientation,
				getWorldFrame().getTimestamp()
		);

		if (isFocused() && !targetBlocked && getTBot().getVel().getLength() < maxBotVelToKick)
		{
			var kickSpeed = adaptKickSpeedToBotVel(target, KickParams.maxStraight().getKickSpeed());
			setKickParams(KickParams.straight(kickSpeed));

			// drive towards target to really get ball on the kicker.
			var botToTarget = target.subtractNew(getPos()).scaleToNew(50);
			updateDestination(destination.addNew(botToTarget));
			getShapes().get(ESkillShapesLayer.DRIBBLE_SKILL).add(new DrawableAnnotation(getPos(), "is focused"));
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


	private boolean isFocused()
	{
		return targetAngleReachedChecker.isReached();
	}


	private double getOrientationFromFilter()
	{
		return getTBot().getFilteredState().map(State::getOrientation).orElseGet(this::getAngle);
	}
}
