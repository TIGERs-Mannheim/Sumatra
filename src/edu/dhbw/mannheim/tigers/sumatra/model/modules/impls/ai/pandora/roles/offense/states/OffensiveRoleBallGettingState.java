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
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AOffensiveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
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
		private MoveAndStaySkill	skill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void doEntryActions()
		{
			switch (getAiFrame().getTacticalField().getGameState())
			{
				case DIRECT_KICK_THEY:
				case GOAL_KICK_THEY:
				case PREPARE_PENALTY_THEY:
				case PREPARE_KICKOFF_THEY:
				case STOPPED:
				case THROW_IN_THEY:
					nextState(EEvent.STOP);
					break;
				default:
					break;
			}
			
			skill = new MoveAndStaySkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			/*
			 * Früher zu kick wenn ball langsam oder stehend.
			 * nicht zu kick wenn redirect move ( anpassen in MoveCalc ).
			 */
			ValuePoint movePos = getAiFrame().getTacticalField().getOffenseMovePositions().get(getBotID());
			
			skill.getMoveCon().setBallObstacle(true);
			skill.getMoveCon().setArmKicker(false);
			if (((movePos.value - 5) > -0.001) && ((movePos.value - 5) < 0.001))
			{
				// Redirect Ball
				IVector3 poss = AiMath.calcRedirectPositions(getBot(), getWFrame().getBall(), getAiFrame()
						.getTacticalField().getBestDirectShootTarget(), 4.0f);
				skill.getMoveCon().updateTargetAngle(poss.z());
				skill.getMoveCon().setBallObstacle(false);
				skill.getMoveCon().setArmKicker(true);
			} else if (((movePos.value - 3) > -0.001) && ((movePos.value - 3) < 0.001))
			{
				// follow Ball
				skill.getMoveCon().setBallObstacle(false);
				skill.getMoveCon().updateLookAtTarget(
						new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget()));
			}
			else if (((movePos.value - 7) > -0.001) && ((movePos.value - 7) < 0.001))
			{
				// BALL_STANDING_STILL
				if (GeoMath.distancePP(getPos(), getWFrame().ball.getPos()) < 440)
				{
					nextState(EEvent.BALL_CONTROL_OBTAINED);
				}
				skill.getMoveCon().updateLookAtTarget(
						new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget()));
			} else if (((movePos.value - 4) > -0.001) && ((movePos.value - 4) < 0.001))
			{
				// BALL_MOVING_SLOW
				if (GeoMath.distancePP(getPos(), getWFrame().ball.getPos()) < 440)
				{
					nextState(EEvent.BALL_CONTROL_OBTAINED);
				}
				skill.getMoveCon().updateLookAtTarget(
						new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget()));
			}
			else
			{
				if (checkBallObtained(movePos))
				{
					nextState(EEvent.BALL_CONTROL_OBTAINED);
				}
				skill.getMoveCon().updateLookAtTarget(
						new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget()));
			}
			
			skill.getMoveCon().updateDestination(movePos);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET;
		}
	}
}
