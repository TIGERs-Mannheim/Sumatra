/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.04.2015
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.trajectory.DribblePath;
import edu.tigers.sumatra.trajectory.HermiteSplinePart2D;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculates offensive Actions for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveActionsCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------- ------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Map<BotID, OffensiveAction> offensiveActions = new HashMap<BotID, OffensiveAction>();
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			offensiveActions.put(key, calcOffensiveAction(key, newTacticalField, baseAiFrame));
		}
		newTacticalField.getOffensiveActions().putAll(offensiveActions);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private OffensiveAction calcOffensiveAction(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		boolean bothTouched = newTacticalField.isMixedTeamBothTouchedBall();
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() != EAIControlState.MIXED_TEAM_MODE)
		{
			bothTouched = true;
		}
		
		OffensiveAction action = new OffensiveAction();
		
		boolean clear = isClearingShotNeeded(botID, newTacticalField, baseAiFrame, action);
		boolean kickInsBlaue = calcKickInsBlaueParams(newTacticalField, baseAiFrame, action);
		
		if ((OffensiveConstants.isForcePassWhenIndirectIsCalled()
				&& ((newTacticalField.getGameState() == EGameStateTeam.THROW_IN_WE)
						|| (newTacticalField.getGameState() == EGameStateTeam.CORNER_KICK_WE)
						|| (newTacticalField.getGameState() == EGameStateTeam.GOAL_KICK_WE)))
				|| !bothTouched)
		{
			action.setType(EOffensiveAction.PASS);
			setPassParamsIfPassIsPossible(botID, newTacticalField,
					baseAiFrame, action);
		} else if (newTacticalField.getGameState() == EGameStateTeam.PREPARE_KICKOFF_WE) // Kickoff
		{
			action.setType(EOffensiveAction.GOAL_SHOT);
			action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
		} else if (isDirectGoalShootPossible(newTacticalField,
				baseAiFrame) /* && !(/* forcePass && OffensiveConstants.forcePassWhenIndirectIsCalled */)
		{
			action.setType(EOffensiveAction.GOAL_SHOT);
			action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
		} else if (clear)
		{
			action.setType(EOffensiveAction.CLEARING_KICK);
			IVector2 target = null;
			if (action.getType() == EOffensiveAction.GOAL_SHOT)
			{
				target = action.getDirectShotAndClearingTarget();
			} else // if (action.getType() == EOffensiveAction.PASS)
			{
				target = Geometry.getGoalTheir().getGoalCenter();
			}
			action.setDirectShotAndClearingTarget(new DynamicPosition(target));
		} else if (setPassParamsIfPassIsPossible(botID, newTacticalField,
				baseAiFrame, action))// || OffensiveConstants.isForcePassWhenIndirectIsCalled())
		{
			action.setType(EOffensiveAction.PASS);
		} else if (isLowScoringChanceDirectGoalShootPossible(newTacticalField,
				baseAiFrame))
		{
			// if (isBotInGoodBlockingPosition(botID, newTacticalField, baseAiFrame)
			// && setPassParamsIfPassIsPossible(botID, newTacticalField, baseAiFrame, action))
			// {
			// action.setType(EOffensiveAction.PASS);
			// } else
			// {
			action.setType(EOffensiveAction.GOAL_SHOT);
			action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
			// }
		}
		
		if (action.getType().equals(EOffensiveAction.PASS))
		{
			if ((newTacticalField.getGameState() != EGameStateTeam.THROW_IN_WE)
					&& (newTacticalField.getGameState() != EGameStateTeam.CORNER_KICK_WE)
					&& (newTacticalField.getGameState() != EGameStateTeam.GOAL_KICK_WE)
					&& (newTacticalField.getGameState() != EGameStateTeam.DIRECT_KICK_WE))
			{
				if (kickInsBlaue)
				{
					// if passTarget has bad rating... do kickInsBlaue instead.
					if ((action != null) && (action.getPassTarget() != null))
					{
						// even the best pass Target is terrible..... do something else !!
						if (action.getPassTarget().getValue() < OffensiveConstants.getClassifyPassTargetAsBad())
						{
							action.setType(EOffensiveAction.KICK_INS_BLAUE);
						}
					}
				}
			}
		}
		calculateDribblePahts(botID, newTacticalField, baseAiFrame, action);
		
		
		return action;
	}
	
	
	/*
	 * isBotInGoodBlockingPosition:
	 * this function checks whether current bot is in good position to prevent foe from goal_kick
	 * (maybe then current tiger should not move away for goal_kick if chance is rather low)
	 */
	@SuppressWarnings("unused")
	private boolean isBotInGoodBlockingPosition(final BotID botID, final TacticalField currentTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		BotID ballPossessorsID = currentTacticalField.getBallPossession().getOpponentsId();
		
		// Create List of all Tigers, except botID
		List<BotID> botstoignore = new ArrayList<BotID>();
		for (Entry<BotID, ITrackedBot> tiger : baseAiFrame.getWorldFrame().tigerBotsVisible)
		{
			if (tiger.getKey() == botID)
			{
				continue;
			}
			botstoignore.add(tiger.getKey());
		}
		double raysize = Geometry.getBotRadius() * 2;
		
		IVector2 ourGoalPosition = Geometry.getGoalTheir().getGoalCenter();
		ITrackedBot bot = baseAiFrame.getWorldFrame().getBot(ballPossessorsID);
		if (bot == null)
		{
			return false;
		}
		IVector2 ballPossessorsPos = bot.getPos();
		return !AiMath.p2pVisibility(baseAiFrame.getWorldFrame(), ballPossessorsPos, ourGoalPosition, raysize,
				botstoignore);
	}
	
	
	private boolean isPassTargetReachable(final BaseAiFrame baseAiFrame, final BotID thisbot,
			final AdvancedPassTarget target, final BotID targetbot)
	{
		IVector2 startPos = baseAiFrame.getWorldFrame().getBot(thisbot).getPos();
		List<BotID> ignoredBots = new ArrayList<BotID>(2);
		ignoredBots.add(thisbot);
		ignoredBots.add(targetbot);
		return AiMath.p2pVisibility(baseAiFrame.getWorldFrame(), startPos, target, ignoredBots);
	}
	
	
	private boolean isDirectGoalShootPossible(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (AiMath.willBotShoot(baseAiFrame.getWorldFrame(), false)
				&& (newTacticalField.getBestDirectShootTarget() != null))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean setPassParamsIfPassIsPossible(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		boolean possible = false;
		IVector2 helperPos = null;
		BotID passTarget = null;
		
		List<AdvancedPassTarget> advancedPassTargets = newTacticalField.getAdvancedPassTargetsRanked();
		for (AdvancedPassTarget key : advancedPassTargets)
		{
			helperPos = key;
			passTarget = key.getBotId();
			double distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(id).getPos(), key);
			if ((passTarget != id) && (distance > 1500) && (isPassTargetReachable(baseAiFrame, id, key, passTarget)))
			{
				break;
			}
		}
		
		if ((helperPos != null) && (passTarget != null) && (passTarget != id))
		{
			possible = true;
		}
		
		for (AdvancedPassTarget key : advancedPassTargets)
		{
			passTarget = key.getBotId();
			double distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(id).getPos(), key);
			if ((passTarget != id) && (distance > 2000))
			{
				possible = true;
			}
		}
		
		if (!possible)
		{
			return false;
		}
		
		AdvancedPassTarget passtarget = selectPassTarget(id, newTacticalField, baseAiFrame);
		if (passtarget == null)
		{
			return false;
		}
		action.setPassTarget(passtarget);
		return possible;
	}
	
	
	private boolean isClearingShotNeeded(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		IVector2 target = Geometry.getGoalOur().getGoalCenter();
		
		IVector2 botPos = baseAiFrame.getWorldFrame().getBot(id).getPos();
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 ballToTarget = ballPos.subtractNew(target);
		IVector2 behindBall = ballPos.addNew(ballToTarget.normalizeNew().multiplyNew(-650));
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		IVector2 behindBallOff1 = behindBall.addNew(normal.multiplyNew(500));
		IVector2 behindBallOff2 = behindBall.addNew(normal.multiplyNew(-500));
		DrawableTriangle tria = new DrawableTriangle(ballPos, behindBallOff1, behindBallOff2,
				new Color(100, 200, 100, 20));
		tria.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(tria);
		
		IVector2 behindBallOff11 = ballPos.addNew(ballPos.subtractNew(behindBallOff1));
		IVector2 behindBallOff12 = ballPos.addNew(ballPos.subtractNew(behindBallOff2));
		DrawableTriangle tria2 = new DrawableTriangle(ballPos, behindBallOff11, behindBallOff12,
				new Color(200, 100, 100, 20));
		tria2.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(tria2);
		
		if (tria.isPointInShape(botPos))
		{
			// bot in front of ball
			DrawableCircle dc = new DrawableCircle(new Circle(botPos, 120), Color.green);
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc);
			for (BotID enemy : baseAiFrame.getWorldFrame().foeBots.keySet())
			{
				IVector2 enemeyPos = baseAiFrame.getWorldFrame().foeBots.get(enemy).getPos();
				if (tria2.isPointInShape(enemeyPos))
				{
					DrawableCircle dc2 = new DrawableCircle(new Circle(enemeyPos, 120), Color.red);
					newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc2);
					// one or more enemy bot is behind the ball.. maybe ready to shoot on our goal !
					if (ballPos.x() < 0)
					{
						// ball on our half of the field
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	private void calculateDribblePahts(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 botPos = baseAiFrame.getWorldFrame().getBot(id).getPos();
		IVector2 target = null;
		if (action.getType() == EOffensiveAction.GOAL_SHOT)
		{
			target = action.getDirectShotAndClearingTarget();
		} else // if (action.getType() == EOffensiveAction.PASS)
		{
			target = Geometry.getGoalOur().getGoalCenter();
		}
		// else
		// {
		// // return false;
		// target = Geometry.getGoalTheir().getGoalCenter();
		// }
		
		IVector2 ballToTarget = target.subtractNew(ballPos);
		
		IVector2 behindBall = ballPos.addNew(ballToTarget.normalizeNew().multiplyNew(-450));
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		IVector2 behindBallOff1 = behindBall.addNew(normal.multiplyNew(300));
		IVector2 behindBallOff2 = behindBall.addNew(normal.multiplyNew(-300));
		DrawableTriangle tria = new DrawableTriangle(ballPos, behindBallOff1, behindBallOff2, Color.CYAN);
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(tria);
		
		if (tria.isPointInShape(botPos))
		{
			// bot in front of ball
		}
		
		// IVector2 behindBallOff11 = ballPos.addNew(ballPos.subtractNew(behindBallOff1));
		// IVector2 behindBallOff12 = ballPos.addNew(ballPos.subtractNew(behindBallOff2));
		// DrawableTriangle tria2 = new DrawableTriangle(ballPos, behindBallOff11, behindBallOff12, Color.GRAY);
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(tria2);
		
		// Testing area
		List<DribblePath> paths = new ArrayList<DribblePath>();
		List<ValuePoint> endPoints = new ArrayList<ValuePoint>();
		
		IVector2 endPos = ballPos.addNew(ballToTarget.scaleToNew(500)).add(normal.scaleToNew(-300));
		
		// IVector2 botBallNormal = ballPos.subtractNew(botPos).normalizeNew();
		
		IVector2 initVel = normal.multiplyNew(-800).addNew(ballToTarget.normalizeNew().multiply(300));
		IVector2 endVel = target.subtractNew(endPos).scaleTo(500);
		DribblePath path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(ballToTarget.scaleToNew(500)).add(normal.scaleToNew(300));
		initVel = normal.multiplyNew(800).addNew(ballToTarget.normalizeNew().multiply(300));
		endVel = target.subtractNew(endPos).scaleTo(500);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(normal.scaleToNew(800));
		initVel = new Vector2(0, 0);
		endVel = new Vector2(0, 0);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(normal.scaleToNew(-800));
		initVel = new Vector2(0, 0);
		endVel = new Vector2(0, 0);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(normal.scaleToNew(-300)).addNew(ballToTarget.scaleToNew(-500));
		initVel = new Vector2(0, 0);
		endVel = new Vector2(0, 0);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(normal.scaleToNew(300)).addNew(ballToTarget.scaleToNew(-500));
		initVel = new Vector2(0, 0);
		endVel = new Vector2(0, 0);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		endPos = ballPos.addNew(ballToTarget.scaleToNew(-800));
		initVel = new Vector2(0, 0);
		endVel = new Vector2(0, 0);
		path = new DribblePath(new HermiteSplinePart2D(ballPos, endPos,
				initVel, endVel, 1.0));
		paths.add(path);
		endPoints.add(new ValuePoint(endPos,
				ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), endPos, false)));
		
		// Color pathsColor = new Color(0, 150, 40, 50);
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(0).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(1).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(2).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(3).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(4).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(5).getDrawablePath(10, pathsColor));
		// newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE)
		// .add(paths.get(6).getDrawablePath(10, pathsColor));
		
		for (ValuePoint key : endPoints)
		{
			int r = (int) Math.min(255, Math.max(0, ((1 - key.getValue()) * 255)));
			int g = (int) Math.min(255, Math.max(0, (key.getValue() * 255)));
			Color color = new Color(r, g, 0, 20);
			DrawableCircle dcircle = new DrawableCircle(new Circle(key, 90), color);
			dcircle.setFill(true);
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dcircle);
		}
		action.setDribblePaths(paths);
	}
	
	
	/**
	 * selectPassTarget:
	 * determines which target to pass to.
	 * 
	 * @param botID
	 * @param newTacticalField
	 * @param baseAiFrame
	 * @return null shoud be handled
	 */
	
	private AdvancedPassTarget selectPassTarget(final BotID botID, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		List<AdvancedPassTarget> advancedPassTargets = newTacticalField.getAdvancedPassTargetsRanked();
		for (AdvancedPassTarget key : advancedPassTargets)
		{
			BotID passTarget = key.getBotId();
			double distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(botID).getPos(), key);
			if ((passTarget != botID) && (distance > 1500) && isPassTargetReachable(baseAiFrame, botID, key, passTarget))
			{
				return new AdvancedPassTarget(key, key.value, false, key.getBotId());
			}
		}
		
		for (AdvancedPassTarget key : advancedPassTargets)
		{
			BotID passTarget = key.getBotId();
			double distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(botID).getPos(), key);
			if ((passTarget != botID) && (distance > 2000))
			{
				return new AdvancedPassTarget(key, key.value, true, key.getBotId());
			}
		}
		try
		{
			return advancedPassTargets.get(0);
		} catch (IndexOutOfBoundsException e)
		{
			return null; // No pass will be performed, due to changes architecture of setPassParamsIfPassIsPossible
		}
	}
	
	
	private boolean isLowScoringChanceDirectGoalShootPossible(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (newTacticalField.getBestDirectShootTarget() != null)
		{
			return true;
		}
		return false;
	}
	
	
	private boolean calcKickInsBlaueParams(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame, final OffensiveAction action)
	{
		IVector2 baseTarget = Geometry.getPenaltyMarkTheir().addNew(new Vector2(-1000, 0));
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		
		IVector2 ballToTarget = baseTarget.subtractNew(ballPos);
		IVector2 normal = ballToTarget.getNormalVector().normalizeNew();
		
		IVector2 h1 = ballPos.addNew(ballToTarget.scaleToNew(1600).addNew(normal.multiplyNew(250)));
		IVector2 h2 = ballPos.addNew(ballToTarget.scaleToNew(1600).addNew(normal.multiplyNew(-250)));
		
		DrawableTriangle dt = new DrawableTriangle(ballPos, h1, h2, new Color(125, 30, 255, 10));
		dt.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
		
		IVector2 helperPos = ballPos.addNew(ballToTarget.scaleToNew(1600 + 350));
		
		DrawableCircle dc = new DrawableCircle(new Circle(helperPos, 500), new Color(125, 30, 255, 10));
		dc.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc);
		
		IVector2 a1 = ballPos.addNew(ballToTarget.scaleToNew(500).addNew(normal.multiplyNew(300)));
		IVector2 a2 = ballPos.addNew(ballToTarget.scaleToNew(500).addNew(normal.multiplyNew(-300)));
		DrawableTriangle dt2 = new DrawableTriangle(ballPos, a1, a2, new Color(0, 230, 255, 30));
		dt2.setFill(true);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt2);
		
		boolean freeShootingPos = true;
		boolean isFree = true;
		for (BotID id : baseAiFrame.getWorldFrame().getFoeBots().keySet())
		{
			ITrackedBot bot = baseAiFrame.getWorldFrame().getFoeBot(id);
			IVector2 botPos = bot.getPos();
			if (dc.isPointInShape(botPos) || dt.isPointInShape(botPos))
			{
				isFree = false;
			}
			if (dt2.isPointInShape(botPos))
			{
				freeShootingPos = false;
			}
			if (!freeShootingPos && !isFree)
			{
				break;
			}
		}
		DrawableText dtext = new DrawableText(helperPos, "free of bots: " + isFree, Color.black);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtext);
		DrawableText dtext2 = new DrawableText(helperPos.addNew(new Vector2(100, 0)), "can kick: " + freeShootingPos,
				Color.black);
		newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dtext2);
		
		action.setKickInsBlauePossible(isFree && freeShootingPos);
		action.setKickInsBlaueTarget(helperPos);
		return isFree && freeShootingPos;
	}
	
	
	private DynamicPosition getBestShootTarget(final TacticalField newTacticalField)
	{
		IVector2 target = newTacticalField.getBestDirectShootTarget();
		if (target == null)
		{
			target = Geometry.getGoalTheir().getGoalCenter();
		}
		return new DynamicPosition(target);
	}
}
