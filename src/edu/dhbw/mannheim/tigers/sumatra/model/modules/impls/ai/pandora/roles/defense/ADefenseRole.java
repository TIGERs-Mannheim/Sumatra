/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Abstract class for (most) defense roles.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ADefenseRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param type
	 * @param penaltyAreaAllowed
	 * @param ownState
	 */
	// FIXME ander defensPLays anpassen
	public ADefenseRole(ERole type, boolean penaltyAreaAllowed, boolean ownState)
	{
		super(type, penaltyAreaAllowed);
		if (ownState == false)
		{
			setInitialState(new DefendState());
			addEndTransition(EStateId.DEFEND, EEvent.DONE);
		}
	}
	
	
	/**
	 * 
	 * @param type
	 */
	public ADefenseRole(ERole type)
	{
		this(type, false, false);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		DEFEND
	}
	
	private enum EEvent
	{
		DONE
	}
	
	private class DefendState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			updateMoveCon(getAiFrame());
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DEFEND;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected abstract void updateMoveCon(AIInfoFrame aiFrame);
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
}
