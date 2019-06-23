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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.DelayedKickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleDelayState extends OffensiveRoleRedirectCatchSpecialMovementState
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
	public class DelayState implements IRoleState
	{
		// private IMoveToSkill skill = null;
		
		private DelayedKickSkill	skill	= null;
		
		
		// private boolean doSpecial = false;
		
		
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
			// Random rn = new Random();
			// int i = rn.nextInt() % 100; // 0 - 1 - 2
			// int randomNum = Math.abs(i);
			//
			// if (randomNum < ((int) (OffensiveConstants.getChanceToDoSpecialMove() * 100)))
			// {
			// doSpecial = true;
			// }
			//
			// skill = AMoveSkill.createMoveToSkill();
			// skill.getMoveCon().updateTargetAngle(getPos().subtractNew(getWFrame().getBall().getPos()).getAngle());
			// setNewSkill(skill);
			skill = new DelayedKickSkill(new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget()));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			// if (((getAiFrame().getTacticalField().getGameState() == EGameState.DIRECT_KICK_WE)
			// ||
			// (getAiFrame().getTacticalField().getGameState() == EGameState.THROW_IN_WE)
			// ||
			// (getAiFrame().getTacticalField().getGameState() == EGameState.CORNER_KICK_WE))
			// && (getWFrame().getBall().getPos().x() > 0))
			// {
			// if (doSpecial)
			// {
			// float distanceToMid = Math.abs(getWFrame().getBall().getPos().x());
			// IVector2 supportingPoint = new Vector2(-300 + (distanceToMid / 3f),
			// AIConfig.getGeometry().getFieldWidth() / 3.5f);
			// getAiFrame().getAICom().getDelayMoves().add(supportingPoint);
			// supportingPoint = new Vector2(-300 + (distanceToMid / 3f),
			// -AIConfig.getGeometry().getFieldWidth() / 3.5f);
			// getAiFrame().getAICom().getDelayMoves().add(supportingPoint);
			// supportingPoint = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
			//
			// if (getWFrame().getBall().getPos().x() > (AIConfig.getGeometry().getFieldLength() / 5f))
			// {
			// if (getWFrame().getBall().getPos().y() > 0)
			// {
			// supportingPoint = supportingPoint.subtractNew(new Vector2(0, AIConfig.getGeometry()
			// .getPenaltyAreaTheir()
			// .getLengthOfPenaltyAreaFrontLineHalf()
			// + AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()));
			// supportingPoint = supportingPoint.addNew(new Vector2(-600, 100f));
			// getAiFrame().getAICom().getDelayMoves().add(supportingPoint);
			// } else
			// {
			// supportingPoint = supportingPoint.addNew(new Vector2(0, AIConfig.getGeometry()
			// .getPenaltyAreaTheir()
			// .getLengthOfPenaltyAreaFrontLineHalf()
			// + AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()));
			// supportingPoint = supportingPoint.addNew(new Vector2(-600, -100f));
			// getAiFrame().getAICom().getDelayMoves().add(supportingPoint);
			// }
			// }
			// }
			// }
			// IVector2 moveTarget = null;
			// if ((getAiFrame().getTacticalField().getOffenseMovePositions() != null) &&
			// getAiFrame().getTacticalField().getOffenseMovePositions().containsKey(getBotID())
			// && (getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID()) != null))
			// {
			// OffensiveMovePosition movePos = getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID());
			// moveTarget = movePos;
			// } else
			// {
			// moveTarget = getPos();
			// }
			// skill.getMoveCon()
			// .getDestCon().updateDestination(moveTarget);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.DELAY;
		}
	}
	
	
}
