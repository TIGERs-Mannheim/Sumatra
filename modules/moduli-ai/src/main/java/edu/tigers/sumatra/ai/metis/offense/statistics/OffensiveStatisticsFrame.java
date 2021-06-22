/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


/**
 * @author MarkG
 */
@Persistent(version = 1)
@Data
public class OffensiveStatisticsFrame
{
	private int desiredNumBots = 0;
	private BotID primaryOffensiveBot = null;
	private Map<BotID, OffensiveBotFrame> botFrames = new HashMap<>();
}
