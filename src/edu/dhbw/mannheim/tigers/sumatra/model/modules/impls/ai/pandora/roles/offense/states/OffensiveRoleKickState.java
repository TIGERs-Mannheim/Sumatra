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
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensiveConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction.EOffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	private BotID					passTargetID			= null;
	private IVector2				passTargetPosition	= null;
	
	private boolean				finalStrategySet		= false;
	private EOffensiveAction	currentAction			= null;
	
	private DynamicPosition		actionTarget			= null;
	
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
		private KickSkill	kickSkill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			// try again
			doEntryActions();
		}
		
		
		@Override
		public void doEntryActions()
		{
			passTargetID = null;
			passTargetPosition = null;
			finalStrategySet = false;
			currentAction = null;
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			DrawableCircle dc1 = new DrawableCircle(new Circle(getWFrame().getBall().getPos(),
					OffensiveConstants.getFinalKickStateDistance()), new Color(200, 0, 0, 66));
			DrawableCircle dc2 = new DrawableCircle(new Circle(getWFrame().getBall().getPos(),
					OffensiveConstants.getFinalKickStateUpdate()), new Color(200, 100, 0, 66));
			dc1.setFill(true);
			dc2.setFill(true);
			getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dc1);
			if (!finalStrategySet)
			{
				if (checkSetFinalStrategy())
				{
					// setFinalStrategy();
					updateStrategy();
					finalStrategySet = true;
				} else
				{
					updateStrategy();
				}
			} else
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dc2);
				updateFinalStrategy();
			}
			
			if (finalStrategySet)
			{
				getAiFrame().getAICom().setOffensiveRolePassTargetID(passTargetID);
				getAiFrame().getAICom().setOffensiveRolePassTarget(passTargetPosition);
				if (passTargetPosition != null)
				{
					IVector2 kickerPos = AiMath.getBotKickerPos(getPos(), getBot().getAngle(), getBot().getBot()
							.getCenter2DribblerDist());
					if (!kickerPos.equals(passTargetPosition, 5f))
					{
						getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(
								new DrawableLine(Line.newLine(kickerPos, passTargetPosition)));
					}
					else
					{
						log.warn("Invalid pass Position, this should not happen: Call Mark!: " + passTargetPosition + " "
								+ passTargetID);
					}
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EOffensiveStrategy.KICK;
		}
		
		
		private boolean checkSetFinalStrategy()
		{
			if (GeoMath.distancePP(getPos(), getAiFrame().getWorldFrame().getBall().getPos()) < OffensiveConstants
					.getFinalKickStateDistance())
			{
				return true;
			}
			return false;
		}
		
		
		// private void setFinalStrategy()
		// {
		// if (getAiFrame().getTacticalField().getOffensiveActions() != null)
		// {
		// OffensiveAction newOffensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		// if (newOffensiveAction != null)
		// {
		// if ((currentAction == null) || (newOffensiveAction.getType() != currentAction))
		// {
		// currentAction = newOffensiveAction.getType();
		// printDebugInformation("setting final kick strategy: " + currentAction);
		// switch (currentAction)
		// {
		// case GOAL_SHOT:
		// shootOnGoal(newOffensiveAction);
		// break;
		// case MOVING_KICK:
		// throw new NotImplementedException();
		// case PASS:
		// passToBestPassTarget(newOffensiveAction);
		// break;
		// case PULL_BACK:
		// throw new NotImplementedException();
		// case CLEARING_KICK:
		// // TODO
		// doKickoff(newOffensiveAction);
		// // throw new NotImplementedException()
		// break;
		// case KICKOFF:
		// doKickoff(newOffensiveAction);
		// break;
		// case PUSHING_KICK:
		// throw new NotImplementedException();
		// default:
		// throw new RuntimeException();
		// }
		// }
		// }
		// }
		// }
		
		
		private void updateStrategy()
		{
			if (getAiFrame().getTacticalField().getOffensiveActions() != null)
			{
				OffensiveAction newOffensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
				if (newOffensiveAction != null)
				{
					if ((currentAction == null) || (newOffensiveAction.getType() != currentAction))
					{
						currentAction = newOffensiveAction.getType();
						switch (currentAction)
						{
							case GOAL_SHOT:
								shootOnGoal(newOffensiveAction);
								break;
							case MOVING_KICK:
								throw new NotImplementedException();
							case PASS:
								passToBestPassTarget(newOffensiveAction);
								break;
							case PULL_BACK:
								throw new NotImplementedException();
							case CLEARING_KICK:
							case KICKOFF:
								doKickoff(newOffensiveAction);
								break;
							case PUSHING_KICK:
								throw new NotImplementedException();
							default:
								throw new RuntimeException();
						}
					} else
					{
						// update Skill
						
					}
				}
			}
		}
		
		
		private void updateFinalStrategy()
		{
			// update EKickDevice in KickSkill
			
			// for (TrackedTigerBot bot : getWFrame().tigerBotsVisible.values())
			// {
			// GeoMath.p2pVisibility(getWFrame(), getWFrame().getBall().getPos(), passTargetPosition, null);
			//
			// }
			IVector2 ballToTarget = actionTarget.subtractNew(getWFrame().getBall().getPos());
			if (ballToTarget.getLength2() > 1500f)
			{
				ballToTarget = ballToTarget.normalizeNew().multiplyNew(1500f);
			}
			
			IVector2 normal = ballToTarget.getNormalVector();
			
			
			IVector2 triB1 = getWFrame().getBall().getPos()
					.addNew(normal.multiplyNew(25 + AIConfig.getGeometry().getBotRadius()));
			IVector2 triB2 = getWFrame().getBall().getPos()
					.addNew(normal.multiplyNew(-25 - AIConfig.getGeometry().getBotRadius()));
			IVector2 triT1 = triB1.addNew(ballToTarget).addNew(
					normal.multiplyNew(100 + AIConfig.getGeometry().getBotRadius()));
			IVector2 triT2 = triB1.addNew(ballToTarget).addNew(
					normal.multiplyNew(-100 - AIConfig.getGeometry().getBotRadius()));
			
			DrawableTriangle triangle1 = new DrawableTriangle(triB1, triT1, triT2);
			DrawableTriangle triangle2 = new DrawableTriangle(triB1, triB2, triT2);
			triangle2.setColor(new Color(0, 0, 0, 125));
			triangle1.setColor(new Color(0, 0, 0, 125));
			triangle1.setFill(true);
			triangle2.setFill(true);
			
			BotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>(getWFrame().getFoeBots());
			bots.putAll(getWFrame().getTigerBotsVisible());
			bots.remove(getBotID());
			if (passTargetID != null)
			{
				bots.remove(passTargetID);
			}
			
			
			boolean chip = false;
			for (TrackedTigerBot bot : bots.values())
			{
				Circle c1 = new Circle(bot.getPos(), AIConfig.getGeometry().getBotRadius());
				DrawableCircle dc1 = new DrawableCircle(c1, Color.black);
				if (triangle1.isPointInShape(bot.getPos()) || triangle2.isPointInShape(bot.getPos()))
				{
					if (GeoMath.distancePP(getWFrame().getBall().getPos(), actionTarget) > 1500)
					{
						chip = true;
					}
				}
				getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(dc1);
			}
			
			getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(triangle1);
			getAiFrame().getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(triangle2);
			visualizeTarget(actionTarget);
			
			if (chip)
			{
				kickSkill.setDevice(EKickerDevice.CHIP);
			} else
			{
				kickSkill.setDevice(EKickerDevice.STRAIGHT);
			}
		}
		
		
		private void shootOnGoal(final OffensiveAction action)
		{
			actionTarget = action.getDirectShotAndClearingTarget();
			kickSkill = new KickSkill(actionTarget, EKickMode.MAX);
			setNewSkill(kickSkill);
		}
		
		
		private void passToBestPassTarget(final OffensiveAction action)
		{
			try
			{
				passTargetID = action.getPassTarget().getBotId();
			} catch (NullPointerException e)
			{
				log.warn("doing desperate shoot when pass was called, this is not good, you should call Mark!");
				doDesperateShoot(action);
			}
			passTargetPosition = action.getPassTarget();
			if ((passTargetID != null) && (passTargetPosition != null))
			{
				if (((getAiFrame().getTacticalField().getGameState() == EGameState.DIRECT_KICK_WE)
						||
						(getAiFrame().getTacticalField().getGameState() == EGameState.THROW_IN_WE)
						|| (getAiFrame().getTacticalField().getGameState() == EGameState.CORNER_KICK_WE)))
				{
					actionTarget = new DynamicPosition(passTargetPosition);
					kickSkill = new KickSkill(actionTarget, EKickMode.PASS,
							EMoveMode.CHILL);
					kickSkill.setDevice(EKickerDevice.STRAIGHT);
					setNewSkill(kickSkill);
				} else
				{
					actionTarget = new DynamicPosition(passTargetPosition);
					kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
					kickSkill.setDevice(EKickerDevice.STRAIGHT);
					setNewSkill(kickSkill);
				}
				
			} else
			{
				doDesperateShoot(action);
			}
		}
		
		
		private void doDesperateShoot(final OffensiveAction action)
		{
			if (((getAiFrame().getTacticalField().getGameState() == EGameState.DIRECT_KICK_WE)
					||
					(getAiFrame().getTacticalField().getGameState() == EGameState.THROW_IN_WE)
					||
					(getAiFrame().getTacticalField().getGameState() == EGameState.CORNER_KICK_WE))
					&& (getWFrame().getBall().getPos().x() > 0))
			{
				IVector2 target = AIConfig.getGeometry().getPenaltyMarkTheir();
				
				Random rn = new Random();
				int i = rn.nextInt() % 3; // 0 - 1 - 2
				int randomNum = Math.abs(i);
				
				if (getPos().y() > 0)
				{
					if (randomNum < 1)
					{
						target = target.addNew(new Vector2(-1000, 400));
					} else
					{
						target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
						target = target.subtractNew(new Vector2(0, AIConfig.getGeometry().getPenaltyAreaTheir()
								.getLengthOfPenaltyAreaFrontLineHalf()
								+ AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()));
						target = target.addNew(new Vector2(-600, 100f));
					}
				} else
				{
					if (randomNum < 1)
					{
						target = target.addNew(new Vector2(-1000, -400));
					} else
					{
						target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
						target = target.addNew(new Vector2(0, AIConfig.getGeometry().getPenaltyAreaTheir()
								.getLengthOfPenaltyAreaFrontLineHalf()
								+ AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()));
						target = target.addNew(new Vector2(-600, -100));
					}
				}
				
				actionTarget = new DynamicPosition(target);
				kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
				kickSkill.setDevice(EKickerDevice.CHIP);
				setNewSkill(kickSkill);
				
				IBotIDMap<TrackedTigerBot> supportBots = new BotIDMap<TrackedTigerBot>();
				for (BotID key : getWFrame().getTigerBotsAvailable().keySet())
				{
					if (key != getBotID())
					{
						supportBots.put(key, getWFrame().getTigerBotsAvailable().get(key));
					}
				}
				if (!supportBots.isEmpty())
				{
					TrackedTigerBot nearestBot = AiMath.getNearestBot(supportBots, target);
					if (nearestBot != null)
					{
						passTargetID = nearestBot.getId();
						passTargetPosition = target;
					}
				}
			} else
			{
				ValuePoint potentialChipKickTarget = AiMath.determineChipShotTarget(getWFrame(), 1000,
						AIConfig.getGeometry()
								.getGoalTheir().getGoalCenter().x());
				if (potentialChipKickTarget == null)
				{
					potentialChipKickTarget = new ValuePoint(AIConfig.getGeometry().getGoalTheir().getGoalCenter(), 0);
				}
				
				TrackedTigerBot nearestBot = AiMath.getNearestBot(getWFrame().foeBots, getPos());
				if ((nearestBot != null)
						&& (GeoMath.distancePP(getPos(), AIConfig.getGeometry().getGoalTheir().getGoalCenter()) > 3500)
						&& (GeoMath.distancePP(nearestBot.getPos(), getPos()) < 1000)
						&& (nearestBot.getPos().x() > getPos().x()))
				{
					actionTarget = new DynamicPosition(potentialChipKickTarget);
					kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
					kickSkill.setDevice(EKickerDevice.CHIP);
					setNewSkill(kickSkill);
				} else
				{
					IVector2 bestTarget = getAiFrame().getTacticalField().getBestDirectShootTarget();
					if (bestTarget == null)
					{
						bestTarget = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
					}
					IBotIDMap<TrackedTigerBot> foeBots = getWFrame().foeBots;
					if (!foeBots.isEmpty())
					{
						TrackedTigerBot foe = AiMath.getNearestBot(foeBots, getWFrame().getBall().getPos());
						if (foe != null)
						{
							float dist = GeoMath.distancePP(getWFrame().getBall().getPos(), foe.getPos());
							if (dist > 2000f)
							{
								actionTarget = new DynamicPosition(bestTarget);
								kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
								kickSkill.setDevice(EKickerDevice.STRAIGHT);
								setNewSkill(kickSkill);
							} else
							{
								actionTarget = new DynamicPosition(bestTarget);
								kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
								kickSkill.setDevice(EKickerDevice.CHIP);
								setNewSkill(kickSkill);
							}
						} else
						{
							actionTarget = new DynamicPosition(bestTarget);
							kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
							kickSkill.setDevice(EKickerDevice.STRAIGHT);
							setNewSkill(kickSkill);
						}
					} else
					{
						actionTarget = new DynamicPosition(bestTarget);
						kickSkill = new KickSkill(actionTarget, EKickMode.PASS, EMoveMode.NORMAL);
						kickSkill.setDevice(EKickerDevice.STRAIGHT);
						setNewSkill(kickSkill);
					}
				}
			}
		}
		
		
		private void doKickoff(final OffensiveAction action)
		{
			doDesperateShoot(action);
			return;
		}
	}
}
