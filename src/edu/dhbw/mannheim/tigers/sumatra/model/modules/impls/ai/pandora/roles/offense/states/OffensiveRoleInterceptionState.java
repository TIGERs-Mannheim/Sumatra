/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.InterceptionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleInterceptionState extends OffensiveRoleDelayState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * Intercept State
	 * 
	 * @author Mark Geiger <Mark.Geiger@dlr.de>
	 */
	public class InterceptionState implements IRoleState
	{
		private InterceptionSkill	intercept			= null;
		private IMoveToSkill			move					= null;
		private boolean				interceptActive	= false;
		
		
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
			intercept = new InterceptionSkill();
			move = AMoveSkill.createMoveToSkill();
			setNewSkill(move);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 movePos = calcMovePosition();
			if ((GeoMath.distancePP(movePos, getPos()) < 50) && (interceptActive == false))
			{
				setNewSkill(intercept);
				interceptActive = true;
			}
			move.getMoveCon().updateDestination(movePos);
			BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
			if (nearestEnemyBot != null)
			{
				if (nearestEnemyBot.getBot() != null)
				{
					intercept.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
				} else
				{
					intercept.setNearestEnemyBotPos(null);
				}
			}
			else
			{
				intercept.setNearestEnemyBotPos(null);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.INTERCEPT;
		}
		
		
		private IVector2 calcMovePosition()
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			IVector2 goal = AIConfig.getGeometry().getGoalOur().getGoalCenter();
			IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
			
			return ballPos.addNew(dir.multiplyNew(AIConfig.getGeometry().getBotToBallDistanceStop()
					+ (AIConfig.getGeometry().getBotRadius() * 2)));
		}
	}
	
}
