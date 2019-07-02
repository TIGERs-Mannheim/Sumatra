/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * MarkG
 */
public class KeepActiveStatusFeature extends AOffensiveStrategyFeature
{
	private long initialTime = 0;

	@Configurable(comment = "This time is subtracted from the time that is still left for the freekick action", defValue = "1.5")
	private static double timeUntilKickOffset = 1.5;


	@Override
	public void doCalc(final TacticalField newTacticalField,
			final OffensiveStrategy strategy)
	{
		GameState gameState = newTacticalField.getGameState();
		if (gameState.isStandardSituationForThem())
		{
			doIndirect(strategy);
		} else if (gameState.isStandardSituationForUs())
		{
			doDirect(strategy, newTacticalField);
		} else if (gameState.isBallPlacementForThem())
		{
			doBallPlacement(strategy);
		} else if (gameState.isStoppedGame())
		{
			doStop(strategy);
		} else if (isOffenseIdle(gameState))
		{
			doNothing(strategy);
		} else
		{
			initialTime = 0;
		}
	}


	private boolean isOffenseIdle(final GameState state)
	{
		return (state.getState() == EGameState.TIMEOUT)
				|| (state.getState() == EGameState.HALT)
				|| (state.getState() == EGameState.PREPARE_PENALTY)
				|| state.isPrepareKickoffForThem();

	}


	private void doNothing(final OffensiveStrategy strategy)
	{
		strategy.clearDesiredBots();
		initialTime = 0;
	}


	private void doBallPlacement(final OffensiveStrategy strategy)
	{
		strategy.clearDesiredBots();
		strategy.clearPlayConfiguration();
	}


	private void doDirect(final OffensiveStrategy strategy, final TacticalField newTacticalField)
	{
		if (initialTime == 0)
		{
			initialTime = getWFrame().getTimestamp();
		}

		double delayTime = OffensiveConstants.getDelayWaitTime();

		// maximize delay time by time until kick
		double timeLeft = Math.max(0, Math.min(delayTime,
				getAiFrame().getRefereeMsg().getCurrentActionTimeRemaining() - timeUntilKickOffset));

		if (strategy.getAttackerBot().isPresent()
				&& strategy.getCurrentOffensivePlayConfiguration().containsKey(strategy.getAttackerBot().get()))
		{
			BotID botID = strategy.getAttackerBot().get();
			ITrackedBot bot = getWFrame().getBot(botID);
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER)
					.add(new DrawableAnnotation(bot.getPos(), String.format("time left: %.2f", timeLeft),
							Vector2f.fromY(-200)));
			if (endDelayEarly(botID, newTacticalField) || timeLeft <= 0)
			{
				strategy.putPlayConfiguration(botID, EOffensiveStrategy.KICK);
			} else
			{
				strategy.putPlayConfiguration(botID, EOffensiveStrategy.DELAY);
			}
		}
	}


	private boolean endDelayEarly(final BotID botID, final TacticalField tacticalField)
	{
		ITrackedBot attacker = getWFrame().getBot(botID);
		return getAiFrame().getGamestate().isDirectFreeForUs()
				&& attacker.getPos().distanceTo(getWFrame().getBall().getPos()) < 400
				&& tacticalField.getBestGoalKickTarget().map(IRatedTarget::getScore).orElse(0.0) > OffensiveConstants
						.getMinBotShouldDoGoalShotScore();
	}


	private void doStop(final OffensiveStrategy strategy)
	{
		for (BotID key : strategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			strategy.putPlayConfiguration(key, EOffensiveStrategy.STOP);
		}
		initialTime = 0;
	}


	private void doIndirect(final OffensiveStrategy strategy)
	{
		for (BotID key : strategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			strategy.putPlayConfiguration(key, EOffensiveStrategy.INTERCEPT);
		}

		if (!OffensiveConstants.isInterceptorEnabled())
		{
			strategy.clearDesiredBots();
		}
		initialTime = 0;
	}
}
