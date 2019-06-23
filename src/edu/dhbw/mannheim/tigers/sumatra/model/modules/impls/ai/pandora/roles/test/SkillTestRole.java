/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Tests arbitrary skills
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class SkillTestRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final AMoveSkill	skill;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use!! Only for RoleFactory
	 */
	public SkillTestRole()
	{
		this(null);
	}
	
	
	/**
	 * @param skill
	 */
	public SkillTestRole(AMoveSkill skill)
	{
		this(ERole.SKILL_TEST, skill);
	}
	
	
	protected SkillTestRole(ERole role, AMoveSkill skill)
	{
		super(role);
		this.skill = skill;
		setInitialState(new ExecuteSkillState());
		addEndTransition(EStateId.EXECUTE_SKILL, EEvent.DONE);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		EXECUTE_SKILL
	}
	
	private enum EEvent
	{
		DONE
	}
	
	private class ExecuteSkillState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ISkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ISkill skill, BotID botID)
		{
			nextState(EEvent.DONE);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.EXECUTE_SKILL;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void fillNeededFeatures(List<EFeature> features)
	{
	}
}
