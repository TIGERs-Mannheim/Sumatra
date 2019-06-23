/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.statemachine.AState;
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
	
	
	private ITrackedBot getTrackedBot()
	{
		return getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
	}
	
	
	private class MoveToGoalCenter extends AState
	{
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
			if (VectorMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 100
					&& getTrackedBot() != null)
			{
				triggerEvent(EEvent.KEEPER_ON_GOAL_CENTER);
			}
		}
	}
	
	
	private class BlockShootingLine extends AState
	{
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
			if (getTrackedBot() != null)
			{
				skill.setShooterPos(new DynamicPosition(getTrackedBot().getPos()));
			}
		}
	}
}
