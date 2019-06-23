/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;


/**
 * Restricted use to OneOnOneKeeper. If the Keeper was going out, but get over chipped, this state tries to catch the
 * ball
 */
public class CatchOverChipState extends AKeeperState
{
	public CatchOverChipState(final KeeperRole parent)
	{
		super(parent, EKeeperState.CATCH_OVER_CHIP);
	}
	
	
	@Override
	public void doEntryActions()
	{
		ReceiveBallSkill receiverSkill = new ReceiveBallSkill(getPos());
		receiverSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		setNewSkill(receiverSkill);
	}
}
