/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.states;

import java.awt.Color;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OffensiveMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.SpecialMoveCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiverSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class OffensiveRoleRedirectCatchSpecialMovementState extends OffensiveRoleKickState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	
	private enum ERedirectCatchSpecialMoveState
	{
		/**  */
		SPECIAL_MOVE,
		/**  */
		DEFAULT;
	}
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * Special Move State
	 * 
	 * @author Mark Geiger <Mark.Geiger@dlr.de>
	 */
	public class RedirectCatchSpecialMoveState implements IRoleState
	{
		
		private SpecialMoveCommand					command	= null;
		private IMoveToSkill							move		= null;
		private IVector2								movePos	= null;
		private ERedirectCatchSpecialMoveState	state		= null;
		private int										idx		= 0;
		
		
		// private long initTime = 0;
		
		
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
			getAiFrame().getAICom().setResponded(false);
		}
		
		
		@Override
		public void doEntryActions()
		{
			move = AMoveSkill.createMoveToSkill();
			move.getMoveCon().setDriveFast(true);
			idx = getAiFrame().getPrevFrame().getAICom().getSpecialMoveCounter();
			
			if (idx < getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands().size())
			{
				command = getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands()
						.get(idx);
				movePos = command.getMovePosition().get(0);
				setNewSkill(move);
				getAiFrame().getPrevFrame().getAICom().setSpecialMoveCounter(idx + 1);
				state = ERedirectCatchSpecialMoveState.SPECIAL_MOVE;
			} else
			{
				IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
				if (target == null)
				{
					target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
				}
				if (OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), target))
				{
					setNewSkill(new RedirectSkill(new DynamicPosition(getAiFrame().getTacticalField()
							.getBestDirectShootTarget())));
				} else
				{
					setNewSkill(new ReceiverSkill());
				}
				state = ERedirectCatchSpecialMoveState.DEFAULT;
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			switch (state)
			{
				case DEFAULT:
					if (getCurrentSkill().getSkillName() == ESkillName.RECEIVER)
					{
						IVector2 target = getAiFrame().getTacticalField()
								.getBestDirectShootTarget();
						DynamicPosition dtarget = null;
						if (target == null)
						{
							dtarget = new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
						} else
						{
							dtarget = new DynamicPosition(target);
						}
						if (OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), dtarget))
						{
							setNewSkill(new RedirectSkill(dtarget));
						}
						drawShapes(getWFrame().getBall().getPos());
					} else
					{
						IVector2 target = getAiFrame().getTacticalField()
								.getBestDirectShootTarget();
						DynamicPosition dtarget = null;
						if (target == null)
						{
							dtarget = new DynamicPosition(AIConfig.getGeometry().getGoalTheir().getGoalCenter());
						} else
						{
							dtarget = new DynamicPosition(target);
						}
						if (!OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), dtarget))
						{
							setNewSkill(new ReceiverSkill());
						}
						drawShapes(dtarget);
					}
					break;
				
				case SPECIAL_MOVE:
					if (idx < getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands().size())
					{
						command = getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands()
								.get(idx);
						movePos = command.getMovePosition().get(0);
						move.getMoveCon().setArmKicker(true);
						
						if (AIConfig.getGeometry().getPenaltyAreaTheir()
								.isPointInShape(movePos, AIConfig.getGeometry().getBotRadius() * 1.2f))
						{
							movePos = new ValuePoint(AIConfig.getGeometry().getPenaltyAreaTheir()
									.nearestPointOutside(movePos, AIConfig.getGeometry().getBotRadius() * 1.2f));
						} else if (AIConfig.getGeometry().getPenaltyAreaOur()
								.isPointInShape(movePos, OffensiveConstants.getDistanceToPenaltyArea()))
						{
							movePos = new ValuePoint(AIConfig.getGeometry().getPenaltyAreaOur()
									.nearestPointOutside(movePos, OffensiveConstants.getDistanceToPenaltyArea()));
						}
						if (AIConfig.getGeometry().getField().isPointInShape(movePos))
						{
							AIConfig.getGeometry().getField()
									.nearestPointInside(movePos, OffensiveConstants.getDistanceToPenaltyArea());
						}
						
						IVector2 otarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
						if (otarget == null)
						{
							otarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
						}
						IVector2 target = getAiFrame().getTacticalField()
								.getBestDirectShootTarget();
						float orientation = 0;
						if (OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), otarget))
						{
							if (target == null)
							{
								target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
							}
							IVector3 poss = AiMath.calcRedirectPositions(getBot(), movePos, target.subtractNew(movePos)
									.getAngle(),
									getWFrame().getBall(),
									target,
									4.0f);
							orientation = poss.z();
						} else
						{
							orientation = getWFrame().getBall().getPos().subtractNew(getPos()).getAngle();
						}
						
						
						move.getMoveCon().updateTargetAngle(orientation);
						move.getMoveCon().updateDestination(movePos);
						
						DrawableLine dl = new DrawableLine(Line.newLine(getPos(), movePos), Color.blue);
						getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dl);
						
						if ((GeoMath.distancePP(getPos(), movePos) < 30)
								|| (OffensiveMath.getBestRedirector(getWFrame(),
										getWFrame().tigerBotsAvailable) == getBotID()))
						{
							setNewSkill(new RedirectSkill(new DynamicPosition(target)));
							state = ERedirectCatchSpecialMoveState.DEFAULT;
						}
					} else
					{
						IVector2 target = getAiFrame().getTacticalField()
								.getBestDirectShootTarget();
						setNewSkill(new RedirectSkill(new DynamicPosition(target)));
						state = ERedirectCatchSpecialMoveState.DEFAULT;
					}
					break;
				
				default:
					log.error("This is impossible, call Mark!");
					break;
			
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE;
		}
		
		
		private void drawShapes(final IVector2 target)
		{
			getAiFrame().getTacticalField()
					.getDrawableShapes()
					.get(EDrawableShapesLayer.OFFENSIVE)
					.add(
							new DrawableLine(Line.newLine(getPos(), target), Color.red));
			visualizeTarget(target);
		}
	}
}
