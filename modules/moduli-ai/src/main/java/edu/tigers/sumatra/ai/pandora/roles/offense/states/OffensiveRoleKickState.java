/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction.EOffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.trajectory.DribblePath;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleKickState extends AOffensiveRoleState implements IRoleState
{
	
	/**
	 * @param role
	 */
	public OffensiveRoleKickState(final OffensiveRole role)
	{
		super(role);
	}
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	private BotID					passTargetID					= null;
	private IVector2				passTargetPosition			= null;
	
	private boolean				finalStrategySet				= false;
	private EOffensiveAction	currentAction					= null;
	
	private DynamicPosition		actionTarget					= null;
	
	private long					finalStrategySetTimer		= 0;
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	private KickSkill				kickSkill						= null;
	private boolean				inChillMode						= false;
	private boolean				inProtectionMode				= false;
	private boolean				inDribbleMode					= false;
	private IVector2				dribbleToPos					= null;
	private IVector2				finalStrategyInitBallPos	= null;
	
	
	@Override
	public void doEntryActions()
	{
		passTargetID = null;
		passTargetPosition = null;
		finalStrategySet = false;
		currentAction = null;
		inChillMode = false;
		finalStrategySetTimer = 0;
		kickSkill = new KickSkill(new DynamicPosition(Geometry.getGoalTheir().getGoalCenter()));
		setNewSkill(kickSkill);
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
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc1);
		
		if ((finalStrategySet
				&& ((getWFrame().getTimestamp() - finalStrategySetTimer) > OffensiveConstants.getChooseNewStrategyTimer())))
		{
			// reset final Strategy
			finalStrategySet = false;
			finalStrategySetTimer = 0;
			passTargetID = null;
			passTargetPosition = null;
			// log.warn("choosing new Strategy, this is not a real warning :P");
		}
		
		if (finalStrategySet)
		{
			double traveledDistance = GeoMath.distancePP(finalStrategyInitBallPos, getWFrame().getBall().getPos());
			if (((traveledDistance > 700 /* distance check here */) || !checkSetFinalStrategy())
					&& ((getWFrame().getTimestamp() - finalStrategySetTimer) > 500_000_000L))
			{
				// if bot is far away from the ball and strategy was set for 500ms, then the strategy should set again.
				finalStrategySet = false;
				finalStrategySetTimer = 0;
				passTargetID = null;
				passTargetPosition = null;
			}
		}
		
		// if not in chill mode, toogle between aggresive and normal kick mode
		if (!inChillMode || (getAiFrame().getTacticalField().getGameState() == EGameStateTeam.RUNNING))
		{
			List<BotID> foeBots = AiMath.getFoeBotsNearestToPointSorted(getAiFrame(), getPos());
			if ((foeBots != null) && (foeBots.size() > 0) && (foeBots.get(0) != null))
			{
				double distanceToNearestEnemy = GeoMath.distancePP(getPos(),
						getWFrame().getFoeBot(foeBots.get(0)).getPos());
				if ((distanceToNearestEnemy < 1000) || (getWFrame().getBall().getVel().getLength() < 0.5))
				{
					kickSkill.setMoveMode(EMoveMode.AGGRESSIVE);
				} else
				{
					kickSkill.setMoveMode(EMoveMode.NORMAL);
				}
			} else
			{
				kickSkill.setMoveMode(EMoveMode.NORMAL);
			}
			boolean ready4Kick = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID()).isRoleReadyToKick();
			kickSkill.setRoleReady4Kick(ready4Kick);
		}
		
		if (!finalStrategySet)
		{
			finalStrategyInitBallPos = getWFrame().getBall().getPos();
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
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc2);
			updateFinalStrategy();
		}
		
		if (finalStrategySet)
		{
			if (finalStrategySetTimer == 0)
			{
				finalStrategySetTimer = getWFrame().getTimestamp();
			}
			getAiFrame().getAICom().setOffensiveRolePassTargetID(passTargetID);
			getAiFrame().getAICom().setOffensiveRolePassTarget(passTargetPosition);
			if (passTargetPosition != null)
			{
				IVector2 kickerPos = GeoMath.getBotKickerPos(getPos(), getBot().getAngle(), getBot().getBot()
						.getCenter2DribblerDist());
				if (!kickerPos.equals(passTargetPosition, 5))
				{
					getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(
							new DrawableLine(Line.newLine(kickerPos, passTargetPosition)));
				} else
				{
					log.warn("Invalid pass Position, this should not happen: Call Mark!: " + passTargetPosition + " "
							+ passTargetID);
				}
			}
		} else
		{
			finalStrategySetTimer = 0;
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
						// set Skills
						case GOAL_SHOT:
							shootOnGoal(newOffensiveAction);
							break;
						case KICK_INS_BLAUE:
							kickInsBlaue(newOffensiveAction);
							break;
						case PASS:
							passToBestPassTarget(newOffensiveAction);
							break;
						case PULL_BACK:
							throw new NotImplementedException();
						case CLEARING_KICK:
							shootOnClearingTarget(newOffensiveAction);
							break;
						case KICKOFF:
							throw new NotImplementedException();
						case PUSHING_KICK:
							throw new NotImplementedException();
						default:
							throw new RuntimeException();
					}
				} else
				{
					switch (currentAction)
					{
						// update Skills
						case PASS:
							if ((newOffensiveAction.getPassTarget() != null)
									&& (newOffensiveAction.getPassTarget().getBotId() != null))
							{
								passTargetID = newOffensiveAction.getPassTarget().getBotId();
								// date kickSkill receiverTarget
								passTargetPosition = newOffensiveAction.getPassTarget();
								actionTarget = new DynamicPosition(passTargetPosition);
								kickSkill.setReceiver(actionTarget);
							}
							break;
						case GOAL_SHOT:
						case CLEARING_KICK:
							actionTarget = newOffensiveAction.getDirectShotAndClearingTarget();
							kickSkill.setReceiver(actionTarget);
							break;
						case KICK_INS_BLAUE:
							actionTarget = new DynamicPosition(newOffensiveAction.getKickInsBlaueTarget());
							kickSkill.setReceiver(actionTarget);
						default:
							break;
					}
				}
			}
		}
	}
	
	
	private void updateFinalStrategy()
	{
		// always able to change to clearing kick
		if (currentAction != EOffensiveAction.CLEARING_KICK)
		{
			OffensiveAction newOffensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
			if ((newOffensiveAction != null) && (newOffensiveAction.getType() == EOffensiveAction.CLEARING_KICK))
			{
				currentAction = EOffensiveAction.CLEARING_KICK;
				shootOnClearingTarget(newOffensiveAction);
				passTargetID = null;
				passTargetPosition = null;
			}
		}
		
		IVector2 ballToTarget = actionTarget.subtractNew(getWFrame().getBall().getPos());
		if (ballToTarget.getLength2() > 1500)
		{
			ballToTarget = ballToTarget.normalizeNew().multiplyNew(1500f);
		}
		
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		
		boolean chip = calcIsChip(actionTarget, passTargetID);
		
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL)
				.add(new DrawableText(getPos().addNew(new Vector2(0, 200)), "isChip: " + chip, Color.orange));
		if (currentAction == EOffensiveAction.GOAL_SHOT)
		{
			if (chip && (GeoMath.distancePP(actionTarget, Geometry.getGoalTheir().getGoalCenter()) < 1500))
			{
				// if we should chip inside the enemy Goal, we should set the target to the penalty area.
				IVector2 targetToBall = actionTarget.subtractNew(getWFrame().getBall().getPos());
				DynamicPosition helperTarget = new DynamicPosition(actionTarget.addNew(targetToBall.scaleToNew(-2000)));
				kickSkill.setReceiver(helperTarget);
				visualizeTarget(helperTarget);
			} else if (GeoMath.distancePP(actionTarget, Geometry.getGoalTheir().getGoalCenter()) < 1500)
			{
				actionTarget = new DynamicPosition(getAiFrame().getTacticalField().getBestDirectShootTarget());
			}
			kickSkill.setReceiver(actionTarget);
		}
		
		visualizeTarget(actionTarget);
		
		if (passTargetPosition != null)
		{
			IVector2 targetToReceiver = passTargetPosition.subtractNew(Geometry.getGoalTheir().getGoalCenter());
			IVector2 senderToReceiver = passTargetPosition.subtractNew(getWFrame().getBall().getPos());
			double angle = GeoMath.angleBetweenVectorAndVector(targetToReceiver, senderToReceiver)
					* AngleMath.RAD_TO_DEG;
			
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE)
					.add(new DrawableText(passTargetPosition.addNew(new Vector2(400, 0)), "Redirect angle: " + angle,
							Color.BLACK));
		}
		
		if (chip)
		{
			kickSkill.setDevice(EKickerDevice.CHIP);
		} else
		{
			kickSkill.setDevice(EKickerDevice.STRAIGHT);
			if (passTargetPosition != null)
			{
				double passEndVel = OffensiveMath.calcPassSpeedForReceivers(getWFrame().getBall().getPos(),
						passTargetPosition, Geometry.getGoalTheir().getGoalCenter());
				DrawableText dt = new DrawableText(getPos().addNew(new Vector2(100, 0)), "PassTargetEndVel: " + passEndVel,
						Color.black);
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
				kickSkill.setPassEndVel(passEndVel);
			}
		}
		
		// protect here ?
		if ((currentAction != EOffensiveAction.CLEARING_KICK) && (currentAction != EOffensiveAction.PASS))
		{
			DrawableLine targetLine = new DrawableLine(Line.newLine(actionTarget, getWFrame().getBall().getPos()));
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(targetLine);
			IVector2 triC1 = getWFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(800))
					.addNew(normal.scaleToNew(325));
			IVector2 triC2 = getWFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(800))
					.addNew(normal.scaleToNew(-325));
			DrawableTriangle dtp = new DrawableTriangle(getWFrame().getBall().getPos(), triC1, triC2, Color.BLACK);
			
			triC1 = getWFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(270))
					.addNew(normal.scaleToNew(325));
			triC2 = getWFrame().getBall().getPos().addNew(ballToTarget.scaleToNew(270))
					.addNew(normal.scaleToNew(-325));
			DrawableTriangle dtp2 = new DrawableTriangle(getWFrame().getBall().getPos(), triC1, triC2, Color.BLACK);
			
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(dtp);
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(dtp2);
			
			// find nearest EnemyBot here
			IVector2 enemyPos = null;
			ITrackedBot enemyBot = null;
			if (getAiFrame().getTacticalField().getEnemyClosestToBall().getBot() != null)
			{
				enemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
				enemyPos = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot().getPos();
				if (dtp.isPointInShape(enemyPos) && !dtp2.isPointInShape(enemyPos))
				{
					// enemyBot blocks direkt Shoot line.
					DrawableText dtext = new DrawableText(enemyPos, "Protecting this enemy!", Color.YELLOW);
					double hitChance = ProbabilityMath.getDirectShootScoreChance(getWFrame(), getWFrame().getBall().getPos(),
							false);
					DrawableText dtext2 = new DrawableText(enemyPos.addNew(new Vector2(100, 0)), "Chance: "
							+ hitChance,
							Color.YELLOW);
					getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(dtext);
					getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(dtext2);
					if (hitChance < 0.3)
					{
						// set protecting here.
						inProtectionMode = true;
					} else if ((hitChance > 0.40) && !inDribbleMode) // hier eventuell wenn in DribbleMode länger behalten.
					{
						inProtectionMode = false;
					} else if (hitChance > 0.60)
					{
						inProtectionMode = false;
					}
				} else
				{
					inProtectionMode = false;
				}
			} else
			{
				inProtectionMode = false;
			}
			
			// if crucialDefender inProtectionMode == true
			
			if (getAiFrame().getTacticalField().getCrucialDefenders().contains(getBotID()))
			{
				inProtectionMode = true;
				enemyPos = Geometry.getGoalOur().getGoalCenter();
			}
			
			if (inProtectionMode && (enemyPos != null) && (inDribbleMode == false))
			{
				inDribbleMode = false;
				kickSkill.setRoleReady4Kick(false);
				
				// if crucial enemyPos = GoalCenter.
				kickSkill.setProtectPos(enemyPos);
				
				kickSkill.setMoveMode(EMoveMode.NORMAL);
				DrawableText dtext = new DrawableText(enemyPos.addNew(new Vector2(200, 0)), "Protecting!",
						Color.YELLOW);
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(dtext);
				
				// dont exit ProtectionMode so easily....
				// maybe do something else
				if (dtp.isPointInShape(getPos()))
				{
					// ProtectionPosition Reached
					DribblePath bestDribblePath = null;
					double maxVal = 0;
					for (DribblePath key : getAiFrame().getTacticalField().getOffensiveActions().get(getBotID())
							.getDribblePaths())
					{
						if (!AiMath.p2pVisibility(getWFrame().getBall().getPos(), key.getPosition(1),
								Geometry.getBotRadius() * 1.2, enemyBot))
						{
							continue;
						}
						double score = ProbabilityMath.getDirectShootScoreChance(getWFrame(), key.getPosition(1), false);
						if (maxVal < score)
						{
							maxVal = score;
							bestDribblePath = key;
						}
					}
					if (bestDribblePath != null)
					{
						dribbleToPos = bestDribblePath.getPosition(1);
						inDribbleMode = true;
					}
				}
				
			} else if (inDribbleMode && (enemyPos != null) && inProtectionMode)
			{
				DrawableText dtext2 = new DrawableText(enemyPos.addNew(new Vector2(300, 0)), "Dribbling!",
						Color.YELLOW);
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL)
						.add(dtext2);
				kickSkill.setProtectPos(null);
				kickSkill.setRoleReady4Kick(false);
				kickSkill.setDestForAvoidingOpponent(dribbleToPos);
				kickSkill.setMoveMode(EMoveMode.NORMAL);
				
				DrawableLine line = new DrawableLine(Line.newLine(getWFrame().getBall().getPos(), dribbleToPos),
						Color.pink);
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL)
						.add(line);
				
				// deactivate inDribbleMode
				if (GeoMath.distancePP(getPos(), dribbleToPos) < 60)
				{
					inDribbleMode = false;
				}
				
			} else
			{
				kickSkill.setProtectPos(null);
				kickSkill.unsetDestForAvoidingOpponent();
			}
		}
	}
	
	
	private void shootOnGoal(final OffensiveAction action)
	{
		actionTarget = action.getDirectShotAndClearingTarget();
		// kickSkill = new KickSkill(actionTarget, EKickMode.MAX);
		kickSkill.setReceiver(actionTarget);
		kickSkill.setKickMode(EKickMode.MAX);
		kickSkill.setMoveMode(EMoveMode.NORMAL);
		inChillMode = false;
		// setNewSkill(kickSkill);
	}
	
	
	private void kickInsBlaue(final OffensiveAction action)
	{
		actionTarget = new DynamicPosition(action.getKickInsBlaueTarget());
		// kickSkill = new KickSkill(actionTarget, EKickMode.MAX);
		kickSkill.setReceiver(actionTarget);
		kickSkill.setKickMode(EKickMode.POINT);
		kickSkill.setMoveMode(EMoveMode.AGGRESSIVE);
		inChillMode = false;
		// setNewSkill(kickSkill);
	}
	
	
	private void shootOnClearingTarget(final OffensiveAction action)
	{
		actionTarget = action.getDirectShotAndClearingTarget();
		kickSkill.setReceiver(actionTarget);
		kickSkill.setKickMode(EKickMode.MAX);
		kickSkill.setDevice(EKickerDevice.CHIP);
		kickSkill.setMoveMode(EMoveMode.AGGRESSIVE);
		inChillMode = false;
	}
	
	
	private void passToBestPassTarget(final OffensiveAction action)
	{
		try
		{
			passTargetID = action.getPassTarget().getBotId();
		} catch (NullPointerException e)
		{
			log.warn("doing desperate shoot when pass was called, this is not good, you should call Mark, "
					+ "PassTargetBotID is null");
			doDesperateShoot(action);
		}
		passTargetPosition = action.getPassTarget();
		if ((passTargetID != null) && (passTargetPosition != null))
		{
			if (((getAiFrame().getTacticalField().getGameState() == EGameStateTeam.DIRECT_KICK_WE)
					|| (getAiFrame().getTacticalField().getGameState() == EGameStateTeam.THROW_IN_WE)
					|| (getAiFrame().getTacticalField().getGameState() == EGameStateTeam.CORNER_KICK_WE)))
			{
				actionTarget = new DynamicPosition(passTargetPosition);
				kickSkill.setMoveMode(EMoveMode.CHILL);
				kickSkill.setReceiver(actionTarget);
				kickSkill.setKickMode(EKickMode.PASS);
				kickSkill.setDevice(EKickerDevice.STRAIGHT);
				inChillMode = true;
			} else
			{
				actionTarget = new DynamicPosition(passTargetPosition);
				kickSkill.setMoveMode(EMoveMode.NORMAL);
				kickSkill.setReceiver(actionTarget);
				kickSkill.setKickMode(EKickMode.PASS);
				kickSkill.setDevice(EKickerDevice.STRAIGHT);
				inChillMode = false;
			}
			
		} else
		{
			log.warn("This is strange, call Mark");
			doDesperateShoot(action);
		}
	}
	
	
	private void doDesperateShoot(final OffensiveAction action)
	{
		log.info("Offensive: Doing desperate Shoot!");
		IVector2 target = Geometry.getGoalTheir().getGoalCenter().subtractNew(getWFrame().getBall().getPos());
		target = getWFrame().getBall().getPos()
				.addNew(target.scaleToNew(OffensiveConstants.getDesperateShotChipKickLength()));
		actionTarget = new DynamicPosition(target);
		kickSkill.setMoveMode(EMoveMode.AGGRESSIVE);
		kickSkill.setReceiver(actionTarget);
		kickSkill.setKickMode(EKickMode.POINT);
		kickSkill.setDevice(EKickerDevice.CHIP);
		inChillMode = false;
	}
	
	
	// private boolean isEpicCornerKickPossible()
	// {
	//
	// // IVector2 ballToTarget = passTarget.subtractNew(ballPos);
	// // IVector2 d1 = ballToTarget.turnToNew(AngleMath.DEG_TO_RAD * 10).scaleToNew(3500);
	// // IVector2 d2 = ballToTarget.turnToNew(-AngleMath.DEG_TO_RAD * 10).scaleToNew(3500);
	// // DrawableTriangle tria = new DrawableTriangle(ballPos, ballPos.addNew(d1), ballPos.addNew(d2), Color.CYAN);
	// // newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(tria);
	// return false;
	// }
}
