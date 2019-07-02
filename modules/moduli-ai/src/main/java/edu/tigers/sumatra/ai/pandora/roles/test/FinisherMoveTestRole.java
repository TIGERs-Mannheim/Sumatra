/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.test;

import java.util.List;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.finisher.EFinisherMove;
import edu.tigers.sumatra.ai.metis.offense.finisher.FinisherMoves;
import edu.tigers.sumatra.ai.metis.offense.finisher.IFinisherMove;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.skillsystem.skills.CommandListSkill;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;


public class FinisherMoveTestRole extends ARole
{
	private final CommandListSkill skill;
	private final IFinisherMove finisherMove;
	
	private enum EEvent implements IEvent
	{
		DONE,
	}
	
	
	public FinisherMoveTestRole(final EFinisherMove finisherMoveType)
	{
		super(ERole.FINISHER_MOVE_TEST);
		
		InitialState initState = new InitialState();
		ExecState execState = new ExecState();
		
		setInitialState(initState);
		addTransition(initState, EEvent.DONE, execState);
		
		finisherMove = FinisherMoves.createFinisherMove(finisherMoveType);
		final List<SkillCommand> skillCommands = finisherMove.getSkillCommands();
		skill = new CommandListSkill(skillCommands);
		setNewSkill(skill);
	}
	
	private class InitialState extends AState
	{
		@Override
		public void doUpdate()
		{
			finisherMove.isApplicable(getAiFrame(), getBotID());
			
			triggerEvent(EEvent.DONE);
		}
	}
	
	private class ExecState extends AState
	{
		@Override
		public void doUpdate()
		{
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_FINISHER_MOVE)
					.addAll(finisherMove.generateShapes());
			if (skill.isFinished())
			{
				setCompleted();
			}
		}
	}
}
