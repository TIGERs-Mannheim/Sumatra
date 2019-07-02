/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Approach the ball, assuming that the ball is still and we are not close to the ball
 */
public class ApproachBallState extends AOffensiveState
{
	@Configurable(defValue = "300.0")
	private static double switchToKickDist = 300;

	@Configurable(defValue = "0.5")
	private static double maxBallVel = 0.5;

	@Configurable(defValue = "true")
	private static boolean protectionModeEnabled = true;

	@Configurable(defValue = "1.3")
	private static double protectionDistanceGain = 1.3;

	private TouchKickSkill skill;

	static
	{
		ConfigRegistration.registerClass("roles", ApproachBallState.class);
	}


	public ApproachBallState(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		DynamicPosition receiver = new DynamicPosition(Geometry.getGoalTheir().getCenter());
		skill = new TouchKickSkill(receiver, KickParams.straight(0.0));
		setNewSkill(skill);
	}


	@Override
	public void doUpdate()
	{
		skill.setMarginToTheirPenArea(getMarginToTheirPenArea());
		if (getBall().getPos().distanceTo(getPos()) < switchToKickDist)
		{
			triggerEvent(EBallHandlingEvent.BALL_APPROACHED);
		} else if (getBall().getVel().getLength2() > maxBallVel)
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES);
		} else if (protectionModeEnabled && ballPossessionIsThreatened(protectionDistanceGain))
		{
			triggerEvent(EBallHandlingEvent.BALL_POSSESSION_THREATENED);
		}
	}
}
