/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import java.util.EnumMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;


/**
 * stores one frame of offensive Information (for one robot)
 */
@Persistent()
public class OffensiveBotFrame
{
	private EOffensiveStrategy activeStrategy;
	private EOffensiveAction activeAction;
	private Map<EOffensiveActionMove, EActionViability> moveViabilities = new EnumMap<>(
			EOffensiveActionMove.class);
	private Map<EOffensiveActionMove, Double> moveViabilityScores = new EnumMap<>(
			EOffensiveActionMove.class);
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveActionMove, EActionViability> getMoveViabilities()
	{
		return moveViabilities;
	}
	
	
	/**
	 * @param moveViabilities
	 */
	public void setMoveViabilities(
			final Map<EOffensiveActionMove, EActionViability> moveViabilities)
	{
		this.moveViabilities = moveViabilities;
	}
	
	
	/**
	 * @return
	 */
	public EOffensiveAction getActiveAction()
	{
		return activeAction;
	}
	
	
	/**
	 * @param activeAction
	 */
	public void setActiveAction(final EOffensiveAction activeAction)
	{
		this.activeAction = activeAction;
	}
	
	
	/**
	 * @return
	 */
	public EOffensiveStrategy getActiveStrategy()
	{
		return activeStrategy;
	}
	
	
	/**
	 * @param activeStrategy
	 */
	public void setActiveStrategy(final EOffensiveStrategy activeStrategy)
	{
		this.activeStrategy = activeStrategy;
	}
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveActionMove, Double> getMoveViabilityScores()
	{
		return moveViabilityScores;
	}
	
	
	/**
	 * @param moveViabilityScores
	 */
	public void setMoveViabilityScores(final Map<EOffensiveActionMove, Double> moveViabilityScores)
	{
		this.moveViabilityScores = moveViabilityScores;
	}
}
