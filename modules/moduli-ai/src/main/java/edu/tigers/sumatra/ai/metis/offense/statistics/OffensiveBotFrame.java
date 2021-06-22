/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import lombok.Data;

import java.util.EnumMap;
import java.util.Map;


/**
 * stores one frame of offensive Information (for one robot)
 */
@Persistent
@Data
public class OffensiveBotFrame
{
	private EOffensiveStrategy activeStrategy;
	private EOffensiveAction activeAction;
	private Map<EOffensiveActionMove, OffensiveActionViability> moveViabilityMap = new EnumMap<>(
			EOffensiveActionMove.class);
}
