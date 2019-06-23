/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2014
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Prepare penalty they -> Keeper to goal center
 * normal start -> keeper left / right on goal line (shooter is allowed to shoot)
 * 
 * @author dirk
 */
public class PenaltyKeeperRole extends ARole
{
	private enum EStateId
	{
		MOVE_TO_GOAL_CENTER,
		BLOCK_SHOOTING_LINE
	}
	
	private enum EEvent
	{
		KEEPER_ON_GOAL_CENTER,
	}
	
	
	/**
	  * 
	  */
	public PenaltyKeeperRole()
	{
		super(ERole.PENALTY_KEEPER);
		setInitialState(new MoveToGoalCenter());
		addTransition(EStateId.MOVE_TO_GOAL_CENTER, EEvent.KEEPER_ON_GOAL_CENTER, new BlockShootingLine());
	}
	
	
	/**
	 * Move to the goal center with Playfinder
	 * 
	 * @author Dirk
	 */
	private class MoveToGoalCenter implements IRoleState
	{
		private AMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(Geometry.getGoalOur().getGoalCenter());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (skill.isDestinationReached())
			{
				triggerEvent(EEvent.KEEPER_ON_GOAL_CENTER);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_TO_GOAL_CENTER;
		}
	}
	
	
	/**
	 * Block the shooting line
	 * 
	 * @author Dirk
	 */
	private class BlockShootingLine implements IRoleState
	{
		private PenaltyKeeperSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new PenaltyKeeperSkill(new DynamicPosition(getTrackedBot().getPos()));
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.setShooterPos(new DynamicPosition(getTrackedBot().getPos()));
		}
		
		
		private ITrackedBot getTrackedBot()
		{
			return getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.BLOCK_SHOOTING_LINE;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
