/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2014
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyKeeperSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * Prepare penalty they -> Keeper to goal center
 * normal start -> keeper left / right on goal line (shooter is allowed to shoot)
 * 
 * @author dirk
 */
public class PenaltyKeeperRoleV2 extends ARole
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
	public PenaltyKeeperRoleV2()
	{
		super(ERole.PENALTY_KEEPER_V2);
		setInitialState(new MoveToGoalCenter());
		addTransition(EStateId.MOVE_TO_GOAL_CENTER, EEvent.KEEPER_ON_GOAL_CENTER, new BlockShootingLine());
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		
	}
	
	/**
	 * Move to the goal center with Playfinder
	 * 
	 * @author Dirk
	 */
	private class MoveToGoalCenter implements IRoleState
	{
		private IMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(AIConfig.getGeometry().getGoalOur().getGoalCenter());
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
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			triggerEvent(EEvent.KEEPER_ON_GOAL_CENTER);
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
		
		
		private TrackedBot getTrackedBot()
		{
			return getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
		}
		
		
		@Override
		public void doExitActions()
		{
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
			return EStateId.BLOCK_SHOOTING_LINE;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
