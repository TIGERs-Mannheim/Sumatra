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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipFastSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleKickState extends OffensiveRoleBallGettingState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * This state kicks the ball. Pass to an friendly bot, or
	 * shoot directly to the bestDirectShootTarget.s
	 * 
	 * @author Mark Geiger <Mark.Geiger@dlr.de>
	 */
	public class KickState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			nextState(EEvent.LOST_BALL);
		}
		
		
		@Override
		public void doEntryActions()
		{
			if (isDirectGoalShootPossible())
			{
				shootOnGoal();
			} else if (isPassToBestPassTargetPossible())
			{
				passToBestPassTarget();
			} else if (isLowScoringChanceDirectGoalShootPossible())
			{
				shootOnGoal();
			} else
			{
				doDesperateShoot();
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!checkBallObtained(getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID())))
			{
				nextState(EEvent.LOST_BALL);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.KICK;
		}
		
		
		private boolean isDirectGoalShootPossible()
		{
			if (AiMath.willBotShoot(getWFrame(), getBotID(), false)
					&& (getAiFrame().getTacticalField().getBestDirectShootTarget() != null))
			{
				return true;
			}
			return false;
		}
		
		
		private boolean isPassToBestPassTargetPossible()
		{
			ValueBot passTarget = AiMath.getBestPasstarget(getAiFrame(), getWFrame().ball.getPos());
			if ((passTarget != null) && (passTarget.getBotID() != getBotID()))
			{
				if (AiMath.getDirectShootScoreChance(getWFrame(), passTarget.getBotID(), false) > AiMath
						.getDirectShootScoreChance(getWFrame(), getBotID(), false))
				{
					return true;
				}
			}
			return false;
		}
		
		
		private boolean isLowScoringChanceDirectGoalShootPossible()
		{
			if (getAiFrame().getTacticalField().getBestDirectShootTarget() != null)
			{
				return true;
			}
			return false;
		}
		
		
		private void shootOnGoal()
		{
			IVector2 bestTarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
			setNewSkill(new KickSkill(new DynamicPosition(bestTarget), EKickMode.MAX));
		}
		
		
		private void passToBestPassTarget()
		{
			ValueBot bestPassTarget = AiMath.getBestPasstarget(getAiFrame(), getWFrame().ball.getPos());
			BotID passreciever = bestPassTarget.getBotID();
			if (bestPassTarget.getValue() < 0)
			{
				setNewSkill(new KickSkill(new DynamicPosition(
						getAiFrame().getWorldFrame().tigerBotsAvailable.get(passreciever)), EKickMode.PASS));
			} else
			{
				setNewSkill(new ChipFastSkill(new DynamicPosition(
						getAiFrame().getWorldFrame().tigerBotsAvailable.get(passreciever))));
			}
		}
		
		
		private void doDesperateShoot()
		{
			
			ValuePoint potentialChipKickTarget = AiMath.determineChipShotTarget(getWFrame(), 1000,
					AIConfig.getGeometry()
							.getGoalTheir().getGoalCenter().x());
			if (potentialChipKickTarget == null)
			{
				potentialChipKickTarget = new ValuePoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), 0);
			}
			
			if (GeoMath.distancePP(getPos(), AIConfig.getGeometry().getGoalTheir().getGoalCenter()) > 3500)
			{
				setNewSkill(new ChipFastSkill(new DynamicPosition(potentialChipKickTarget)));
			} else
			{
				IVector2 bestTarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
				if (bestTarget == null)
				{
					bestTarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
				}
				setNewSkill(new KickSkill(new DynamicPosition(bestTarget), EKickMode.MAX));
			}
		}
	}
}
