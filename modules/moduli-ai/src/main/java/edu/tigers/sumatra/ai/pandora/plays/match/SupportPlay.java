/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.match;

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
