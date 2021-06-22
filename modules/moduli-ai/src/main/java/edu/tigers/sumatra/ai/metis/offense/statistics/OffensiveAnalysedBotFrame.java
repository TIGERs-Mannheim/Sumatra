/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import lombok.Data;

import java.util.EnumMap;
import java.util.Map;


/**
 * @author MarkG
 */
@Persistent
@Data
public class OffensiveAnalysedBotFrame
{
	private Map<EOffensiveActionMove, Map<EActionViability, Double>> moveViabilitiesAvg = new EnumMap<>(
			EOffensiveActionMove.class);
	private Map<EOffensiveActionMove, Double> moveViabilitiyScoreAvg = new EnumMap<>(EOffensiveActionMove.class);
}
