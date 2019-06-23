/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Prepare penalty they -> Keeper to goal center
 * normal start -> keeper left / right on goal line (shooter is allowed to shoot)
 * 
 * @author dirk
 */
public class PenaltyKeeperRole extends ARole
{
	private enum EEvent implements IEvent
	{
		KEEPER_ON_GOAL_CENTER,
	}
	
	
	/**
	  * 
	  */
	public PenaltyKeeperRole()
	{
		super(ERole.PENALTY_KEEPER);
		IState moveToGoal = new MoveToGoalCenter();
		setInitialState(moveToGoal);
		addTransition(moveToGoal, EEvent.KEEPER_ON_GOAL_CENTER, new BlockShootingLine());
	}
	
	
	/**
	 * Move to the goal center with Playfinder
	 * 
	 * @author Dirk
	 */
	private class MoveToGoalCenter implements IState {
		private AMoveToSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(Geometry.getGoalOur().getCenter());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (VectorMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 100)
			{
				triggerEvent(EEvent.KEEPER_ON_GOAL_CENTER);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}


	}
	
	
	/**
	 * Block the shooting line
	 * 
	 * @author Dirk
	 */
	private class BlockShootingLine implements IState {
		private PenaltyKeeperSkill skill;
		
		
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


	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
