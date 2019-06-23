/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleStopState extends OffensiveRoleKickState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	private static boolean	normalStart	= false;
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * Stop State
	 * 
	 * @author Mark Geiger <Mark.Geiger@dlr.de>
	 */
	public class StopState implements IRoleState
	{
		private MoveAndStaySkill	skill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			skill = new MoveAndStaySkill();
			skill.getMoveCon().getAngleCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().getDestCon().updateDestination(getWFrame().getBall().getPos().addNew(new Vector2(-500, 0)));
			if (normalStart)
			{
				nextState(EEvent.NORMALSTART);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.STOP;
		}
	}
	
	
	/**
	 * normalStart called -> go on with game
	 */
	public static void normalStartCalled()
	{
		normalStart = true;
	}
	
	
	/**
	 * setter for normalStart
	 * 
	 * @param normalStart
	 */
	public void setNormalStart(final boolean normalStart)
	{
		OffensiveRoleStopState.normalStart = normalStart;
	}
	
	
	/**
	 * getter for normalStart
	 * 
	 * @return
	 */
	public boolean getNormalStart()
	{
		return normalStart;
	}
}
