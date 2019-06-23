/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.InterceptionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The InterceptionRole tries the block enemy Offensive Bots while
 * they prepare indirect Shoots.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class InterceptionRole extends ARole
{
	
	
	protected enum EStateId
	{
		BLOCKING
	}
	
	protected enum EEvent
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public InterceptionRole()
	{
		super(ERole.INTERCEPTION);
		
		IRoleState blocker = new BlockerState();
		setInitialState(blocker);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		
	}
	
	private class BlockerState implements IRoleState
	{
		InterceptionSkill	skill	= new InterceptionSkill();
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
			if (nearestEnemyBot != null)
			{
				if (nearestEnemyBot.getBot() != null)
				{
					skill.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
				} else
				{
					skill.setNearestEnemyBotPos(null);
				}
			}
			else
			{
				skill.setNearestEnemyBotPos(null);
			}
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.BLOCKING;
		}
	}
}
