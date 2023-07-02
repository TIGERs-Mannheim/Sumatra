/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;


/**
 * Support play manages the support roles. This are all roles that are not offensive or defensive.
 * The purpose of the play is to manage the behaviors ({@link ESupportBehavior}) for all
 * the support roles.
 */
public class SupportPlay extends APlay
{
	@Configurable(defValue = "3000.0", comment = "[mm] Supporter want to avoid a planned kick with an obstacle (max length)")
	private static double maxPlannedKickObstacleLength = 3000.0;


	public SupportPlay()
	{
		super(EPlay.SUPPORT);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		for (var role : findRoles(SupportRole.class))
		{
			var behavior = getTacticalField().getSupportBehaviorAssignment().get(role.getBotID());
			var target = getTacticalField().getSupportViabilities().get(role.getBotID()).get(behavior);
			role.setBehavior(behavior);
			role.setTarget(target);
			role.setAvoidPassesFromOffensive(avoidPassesFromOffensive(behavior));
			role.setMaxPlannedKickObstacleLength(maxPlannedKickObstacleLength);
		}
	}


	private boolean avoidPassesFromOffensive(ESupportBehavior behavior)
	{
		return behavior != ESupportBehavior.FAKE_PASS_RECEIVER;
	}


	@Override
	protected ARole onAddRole()
	{
		return new SupportRole();
	}
}
