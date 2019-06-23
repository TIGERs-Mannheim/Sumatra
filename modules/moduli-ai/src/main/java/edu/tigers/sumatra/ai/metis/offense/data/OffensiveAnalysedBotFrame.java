/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;

import java.util.EnumMap;
import java.util.Map;

import static edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction.EOffensiveAction;


/**
 * @author MarkG
 */
@Persistent()
public class OffensiveAnalysedBotFrame
{
	private Map<EOffensiveActionMove, Map<EActionViability, Double>>	moveViabilitiesAvg		= new EnumMap<>(
			EOffensiveActionMove.class);
	private Map<EOffensiveActionMove, Double>								moveViabilitiyScoreAvg	= new EnumMap<>(
			EOffensiveActionMove.class);
	private Map<EOffensiveAction, Double>										activeActionMoveAvg		= new EnumMap<>(
			EOffensiveAction.class);
	private Map<EOffensiveStrategy, Double>									activeFeatureAvg			= new EnumMap<>(
			EOffensiveStrategy.class);
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveActionMove, Map<EActionViability, Double>> getMoveViabilitiesAvg()
	{
		return moveViabilitiesAvg;
	}
	
	
	/**
	 * @param moveViabilitiesAvg
	 */
	public void setMoveViabilitiesAvg(final Map<EOffensiveActionMove, Map<EActionViability, Double>> moveViabilitiesAvg)
	{
		this.moveViabilitiesAvg = moveViabilitiesAvg;
	}
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveActionMove, Double> getMoveViabilitiyScoreAvg()
	{
		return moveViabilitiyScoreAvg;
	}
	
	
	/**
	 * @param moveViabilitiyScoreAvg
	 */
	public void setMoveViabilitiyScoreAvg(final Map<EOffensiveActionMove, Double> moveViabilitiyScoreAvg)
	{
		this.moveViabilitiyScoreAvg = moveViabilitiyScoreAvg;
	}
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveAction, Double> getActiveActionMoveAvg()
	{
		return activeActionMoveAvg;
	}
	
	
	/**
	 * @param activeActionMoveAvg
	 */
	public void setActiveActionMoveAvg(final Map<EOffensiveAction, Double> activeActionMoveAvg)
	{
		this.activeActionMoveAvg = activeActionMoveAvg;
	}
	
	
	/**
	 * @return
	 */
	public Map<EOffensiveStrategy, Double> getActiveFeatureAvg()
	{
		return activeFeatureAvg;
	}
	
	
	/**
	 * @param activeFeatureAvg
	 */
	public void setActiveFeatureAvg(final Map<EOffensiveStrategy, Double> activeFeatureAvg)
	{
		this.activeFeatureAvg = activeFeatureAvg;
	}
}
