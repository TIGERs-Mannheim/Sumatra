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
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleStopState extends OffensiveRoleInterceptionState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
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
		private IMoveToSkill	skill	= null;
		
		
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
			skill = AMoveSkill.createMoveToSkill();
			skill.getMoveCon().getAngleCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 moveTarget = null;
			if ((getAiFrame().getTacticalField().getOffenseMovePositions() != null) &&
					getAiFrame().getTacticalField().getOffenseMovePositions().containsKey(getBotID())
					&& (getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID()) != null))
			{
				OffensiveMovePosition movePos = getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID());
				moveTarget = movePos;
			} else
			{
				moveTarget = getPos();
			}
			skill.getMoveCon()
					.getDestCon().updateDestination(moveTarget);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.STOP;
		}
	}
}
