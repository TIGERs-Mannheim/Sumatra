/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleSupportiveAttackerState extends OffensiveRoleStopState
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
	public class SupportiveAttackerState implements IRoleState
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
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().updateDestination(calcMovePosition());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.SUPPORTIVE_ATTACKER;
		}
		
		
		private IVector2 calcMovePosition()
		{
			
			IVector2 ballPos = getWFrame().getBall().getPos();
			IVector2 goal = AIConfig.getGeometry().getGoalOur().getGoalCenter();
			IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
			TrackedTigerBot bot = AiMath.getNearestBot(getAiFrame().getWorldFrame().getFoeBots(), getWFrame().getBall()
					.getPos());
			
			if ((bot != null) && (bot.getPos().x() > ballPos.x()))
			{
				dir = ballPos.subtractNew(bot.getPos()).normalizeNew();
			}
			
			return ballPos.addNew(dir.multiplyNew(AIConfig.getGeometry().getBotToBallDistanceStop()
					+ (AIConfig.getGeometry().getBotRadius() * 2)));
		}
	}
	
}
