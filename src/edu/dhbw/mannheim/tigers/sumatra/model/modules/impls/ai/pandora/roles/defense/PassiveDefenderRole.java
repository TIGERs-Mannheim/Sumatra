/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon.EDestFreeMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Passive Role. Gets a destination position and a lookAt position
 * by the controlling Play.
 * 
 * @author Malte
 * 
 */
public class PassiveDefenderRole extends ADefenseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean	isKeeper	= false;
	
	private enum EStateId
	{
		NORMAL
	}
	
	private enum EEvent
	{
		DONE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PassiveDefenderRole()
	{
		this(new Vector2f(-AIConfig.getGeometry().getFieldLength() / 4, 0), new Vector2f(AIConfig.getGeometry()
				.getCenter()));
	}
	
	
	// --------------------------------------------------------------------------
	/**
	 * @param dest
	 * @param target
	 */
	public PassiveDefenderRole(IVector2 dest, IVector2 target)
	{
		super(ERole.PASSIVE_DEFENDER, false, true);
		
		setInitialState(new NormalMoveState());
		addEndTransition(EStateId.NORMAL, EEvent.DONE);
		
		updateDestinationFree(EDestFreeMode.FREE_OF_TIGERS);
		updateDestination(dest);
		updateLookAtTarget(target);
	}
	
	// --------------------------------------------------------------------------
	// ---------------------InnerClass-------------------------------------
	// --------------------------------------------------------------------------
	private class NormalMoveState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			// nothing to do
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.NORMAL;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param target
	 */
	public void setTarget(IVector2 target)
	{
		updateLookAtTarget(target);
	}
	
	
	/**
	 * @param isKeeper
	 */
	public void setKeeper(boolean isKeeper)
	{
		this.isKeeper = isKeeper;
	}
	
	
	@Override
	public boolean isKeeper()
	{
		return isKeeper;
	}
	
	
	@Override
	protected void updateMoveCon(AIInfoFrame aiFrame)
	{
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
}
