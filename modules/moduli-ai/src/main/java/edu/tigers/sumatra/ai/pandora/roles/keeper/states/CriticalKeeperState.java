/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;


/**
 * Implementation of existing Keeper Roles NORMAL, INTERCEPT_BALL, GO_OUT and DEFEND_REDIRECT in a skill for performance
 * reasons.
 * transition between these two is also handles in the skill.
 */
public class CriticalKeeperState extends AKeeperState
{
	public CriticalKeeperState(KeeperRole parent)
	{
		super(parent, EKeeperState.CRITICAL);
	}
	
	
	@Override
	public void doEntryActions()
	{
		// Transition between these states is handled in the skill for performance reasons
		
		CriticalKeeperSkill keeperSkill = new CriticalKeeperSkill(getAiFrame().getKeeperId());
		setNewSkill(keeperSkill);
	}
}
