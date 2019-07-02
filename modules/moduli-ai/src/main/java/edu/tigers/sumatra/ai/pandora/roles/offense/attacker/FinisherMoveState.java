/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import edu.tigers.sumatra.ai.metis.offense.finisher.IFinisherMove;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.skillsystem.skills.CommandListSkill;


/**
 * Execute a finisher move
 */
public class FinisherMoveState extends AOffensiveState
{
	private CommandListSkill skill;
	
	
	public FinisherMoveState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		IFinisherMove finisherMove = getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(getBotID())
				.getFinisherMove();
		skill = new CommandListSkill(finisherMove.getSkillCommands());
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (skill.isFinished())
		{
			triggerEvent(EBallHandlingEvent.FINISHER_MOVE_EXECUTED);
		}
	}
}
