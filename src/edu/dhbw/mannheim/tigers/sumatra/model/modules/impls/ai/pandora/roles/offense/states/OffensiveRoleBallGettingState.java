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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition.EOffensiveMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AOffensiveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleBallGettingState extends AOffensiveRole
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * This state tries to obtain ball control.
	 * 
	 * @author Mark Geiger <Mark.Geiger@dlr.de>
	 */
	public class BallGettingState implements IRoleState
	{
		private IMoveToSkill	move	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			setNewSkill(move);
		}
		
		
		@Override
		public void doEntryActions()
		{
			move = AMoveSkill.createMoveToSkill();
			move.getMoveCon().setDriveFast(true);
			setNewSkill(move);
		}
		
		
		@Override
		public void doExitActions()
		{
			if (move != null)
			{
				move.getMoveCon().setDribbleDuration(0);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			OffensiveMovePosition movePos = getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID());
			move.getMoveCon().setArmKicker(false);
			EOffensiveMoveType type = movePos.getType();
			switch (type)
			{
				case IGNORE_BALL:
					move.getMoveCon().setBallObstacle(false);
					break;
				case NORMAL:
				case UNREACHABLE:
					move.getMoveCon().setBallObstacle(true);
					break;
				default:
					break;
			}
			
			if (AIConfig.getGeometry().getPenaltyAreaTheir()
					.isPointInShape(movePos, OffensiveConstants.getDistanceToPenaltyArea()))
			{
				movePos.set(AIConfig.getGeometry().getPenaltyAreaTheir()
						.nearestPointOutside(movePos, OffensiveConstants.getDistanceToPenaltyArea()));
			} else if (AIConfig.getGeometry().getPenaltyAreaOur()
					.isPointInShape(movePos, OffensiveConstants.getDistanceToPenaltyArea()))
			{
				movePos.set(AIConfig.getGeometry().getPenaltyAreaOur()
						.nearestPointOutside(movePos, OffensiveConstants.getDistanceToPenaltyArea()));
			}
			if (GeoMath.distancePP(getWFrame().getBall().getPos(), movePos) < OffensiveConstants.getMinDistToBall())
			{
				IVector2 dir = getWFrame().getBall().getPos().subtractNew(getPos()).normalizeNew();
				if (dir.isZeroVector())
				{
					dir = new Vector2(-1, 0);
				}
				movePos.set(movePos.subtractNew(dir.multiplyNew(OffensiveConstants.getMinDistToBall())));
			}
			IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
			if (target == null)
			{
				target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
			}
			move.getMoveCon().updateLookAtTarget(
					new DynamicPosition(target));
			
			if (!AIConfig.getGeometry().getField().isPointInShape(movePos))
			{
				movePos.set(AIConfig.getGeometry().getField().nearestPointInside(movePos, 200f));
			}
			
			
			if (AIConfig.getGeometry().getPenaltyAreaTheir()
					.isPointInShape(movePos, AIConfig.getGeometry().getBotRadius() + 30))
			{
				movePos.set(AIConfig.getGeometry().getPenaltyAreaTheir().nearestPointOutside(movePos, 200f));
			}
			
			move.getMoveCon().updateDestination(movePos);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.GET;
		}
		
	}
	
}
