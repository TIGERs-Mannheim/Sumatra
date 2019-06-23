/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * MarkG
 */
public class KeepActiveStatusFeature extends AOffensiveStrategyFeature
{
	private boolean hasResponded = false;
	
	
	/**
	 * Default
	 */
	public KeepActiveStatusFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		GameState gameState = newTacticalField.getGameState();
		if (gameState.isStandardSituationForThem())
		{
			doIndirect(strategy, tempInfo);
		} else if (gameState.isStandardSituationForUs())
		{
			doDirect(baseAiFrame, tempInfo, strategy);
		} else if (gameState.isBallPlacementForThem())
		{
			doBallPlacement(strategy);
		} else if (gameState.getState() == EGameState.STOP)
		{
			doStop(strategy);
		} else if (isOffenseIdle(gameState))
		{
			doNothing(strategy);
		} else
		{
			hasResponded = false;
		}
	}
	
	
	private boolean isOffenseIdle(final GameState state)
	{
		if ((state.getState() == EGameState.TIMEOUT) || (state.getState() == EGameState.HALT)
				|| (state.getState() == EGameState.PREPARE_PENALTY))
		{
			return true;
		}
		
		return state.isPrepareKickoffForThem();
	}
	
	
	private void doNothing(final OffensiveStrategy strategy)
	{
		strategy.getDesiredBots().clear();
		strategy.setMaxNumberOfBots(0);
		strategy.setMinNumberOfBots(0);
		hasResponded = false;
	}
	
	
	private void doBallPlacement(final OffensiveStrategy strategy)
	{
		strategy.setMaxNumberOfBots(0);
		strategy.setMinNumberOfBots(0);
		strategy.getDesiredBots().clear();
		strategy.getCurrentOffensivePlayConfiguration().clear();
	}
	
	
	private void doDirect(final BaseAiFrame baseAiFrame, final TemporaryOffensiveInformation tempInfo,
			final OffensiveStrategy strategy)
	{
		for (BotID key : strategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			if (key == tempInfo.getPrimaryBot().getBotId())
			{
				if (baseAiFrame.getPrevFrame().getAICom().hasResponded() || hasResponded)
				{
					hasResponded = true;
					strategy.getCurrentOffensivePlayConfiguration().put(key, OffensiveStrategy.EOffensiveStrategy.KICK);
				} else
				{
					strategy.getCurrentOffensivePlayConfiguration().put(key, OffensiveStrategy.EOffensiveStrategy.DELAY);
				}
			}
		}
	}
	
	
	private void doStop(final OffensiveStrategy strategy)
	{
		for (BotID key : strategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			strategy.getCurrentOffensivePlayConfiguration().put(key, OffensiveStrategy.EOffensiveStrategy.STOP);
		}
		strategy.setMaxNumberOfBots(1);
		strategy.setMinNumberOfBots(0);
		hasResponded = false;
	}
	
	
	private void doIndirect(final OffensiveStrategy strategy, final TemporaryOffensiveInformation tempInfo)
	{
		for (BotID key : strategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			strategy.getCurrentOffensivePlayConfiguration().put(key, OffensiveStrategy.EOffensiveStrategy.INTERCEPT);
		}
		strategy.getDesiredBots().clear();
		
		if (OffensiveConstants.isInterceptorEnabled())
		{
			strategy.getDesiredBots().add(tempInfo.getPrimaryBot().getBotId());
			strategy.setMaxNumberOfBots(1);
		} else
		{
			strategy.setMaxNumberOfBots(0);
		}
		hasResponded = false;
	}
}
