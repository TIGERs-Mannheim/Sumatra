/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.04.2015
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction.EOffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


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
		newTacticalField.setOffensiveActions(offensiveActions);
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
		if ((OffensiveConstants.isForcePassWhenIndirectIsCalled()
				&& ((newTacticalField.getGameState() == EGameState.THROW_IN_WE)
				|| (newTacticalField.getGameState() == EGameState.CORNER_KICK_WE)
				|| (newTacticalField.getGameState() == EGameState.CORNER_KICK_WE))) || !bothTouched)
		{
			action.setType(EOffensiveAction.PASS);
			setPassParamsIfPassIsPossible(botID, newTacticalField,
					baseAiFrame, action);
		}
		else if (newTacticalField.getGameState() == EGameState.PREPARE_KICKOFF_WE) // Kickoff
		{
			action.setType(EOffensiveAction.GOAL_SHOT);
			action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
		} else if (isDirectGoalShootPossible(newTacticalField,
				baseAiFrame) /* && !(/* forcePass && OffensiveConstants.forcePassWhenIndirectIsCalled */)
		{
			action.setType(EOffensiveAction.GOAL_SHOT);
			action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
		} else if (setPassParamsIfPassIsPossible(botID, newTacticalField,
				baseAiFrame, action) || OffensiveConstants.isForcePassWhenIndirectIsCalled())
		{
			action.setType(EOffensiveAction.PASS);
		} else if (isLowScoringChanceDirectGoalShootPossible(newTacticalField,
				baseAiFrame))
		{
			if (isBotInGoodBlockingPosition(botID, newTacticalField, baseAiFrame)
					&& setPassParamsIfPassIsPossible(botID, newTacticalField, baseAiFrame, action))
			{
				action.setType(EOffensiveAction.PASS);
			}
			else
			{
				action.setType(EOffensiveAction.GOAL_SHOT);
				action.setDirectShotAndClearingTarget(getBestShootTarget(newTacticalField));
			}
		}
		return action;
	}
	
	
	/*
	 * isBotInGoodBlockingPosition:
	 * this function checks whether current bot is in good position to prevent foe from goal_kick
	 * (maybe then current tiger should not move away for goal_kick if chance is rather low)
	 */
	private boolean isBotInGoodBlockingPosition(final BotID botID, final TacticalField currentTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		BotID ballPossessorsID = currentTacticalField.getBallPossession().getOpponentsId();
		
		// Create List of all Tigers, except botID
		List<BotID> botstoignore = new ArrayList<BotID>();
		for (Entry<BotID, TrackedTigerBot> tiger : baseAiFrame.getWorldFrame().tigerBotsVisible)
		{
			if (tiger.getKey() == botID)
			{
				continue;
			}
			botstoignore.add(tiger.getKey());
		}
		float raysize = AIConfig.getGeometry().getBotRadius() * 2f;
		
		IVector2 ourGoalPosition = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		TrackedTigerBot bot = baseAiFrame.getWorldFrame().getBot(ballPossessorsID);
		if (bot == null)
		{
			return false;
		}
		IVector2 ballPossessorsPos = bot.getPos();
		return !GeoMath.p2pVisibility(baseAiFrame.getWorldFrame(), ballPossessorsPos, ourGoalPosition, raysize,
				botstoignore);
		
		
	}
	
	
	private boolean isPassTargetReachable(final BaseAiFrame baseAiFrame, final BotID thisbot,
			final AdvancedPassTarget target, final BotID targetbot)
	{
		IVector2 startPos = baseAiFrame.getWorldFrame().getBot(thisbot).getPos();
		List<BotID> ignoredBots = new ArrayList<BotID>(2);
		ignoredBots.add(thisbot);
		ignoredBots.add(targetbot);
		return GeoMath.p2pVisibility(baseAiFrame.getWorldFrame(), startPos, target, ignoredBots);
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
			float distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(id).getPos(), key);
			if ((passTarget != id) && (distance > 1500f) && (isPassTargetReachable(baseAiFrame, id, key, passTarget)))
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
			float distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(id).getPos(), key);
			if ((passTarget != id) && (distance > 2000f))
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
		return true;
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
			float distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(botID).getPos(), key);
			if ((passTarget != botID) && (distance > 1500f) && isPassTargetReachable(baseAiFrame, botID, key, passTarget))
			{
				
				return new AdvancedPassTarget(key, key.value, false, key.getBotId());
			}
		}
		
		for (AdvancedPassTarget key : advancedPassTargets)
		{
			BotID passTarget = key.getBotId();
			float distance = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBot(botID).getPos(), key);
			if ((passTarget != botID) && (distance > 2000f))
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
	
	
	private DynamicPosition getBestShootTarget(final TacticalField newTacticalField)
	{
		IVector2 target = newTacticalField.getBestDirectShootTarget();
		if (target == null)
		{
			target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		}
		return new DynamicPosition(target);
	}
}
