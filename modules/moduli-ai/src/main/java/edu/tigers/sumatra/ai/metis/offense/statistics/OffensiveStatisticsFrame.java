/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;


/**
 * @author MarkG
 */
@Persistent(version = 1)
public class OffensiveStatisticsFrame
{
	private int desiredNumBots = 0;
	private BotID primaryOffensiveBot = null;
	
	private Map<BotID, OffensiveBotFrame> botFrames = new HashMap<>();
	
	
	/**
	 * @return
	 */
	public Map<BotID, OffensiveBotFrame> getBotFrames()
	{
		return botFrames;
	}
	
	
	/**
	 * @return
	 */
	public BotID getPrimaryOffensiveBot()
	{
		return primaryOffensiveBot;
	}
	
	
	/**
	 * @param primaryOffensiveBot
	 */
	public void setPrimaryOffensiveBot(final BotID primaryOffensiveBot)
	{
		this.primaryOffensiveBot = primaryOffensiveBot;
	}
	
	
	/**
	 * @return
	 */
	public int getDesiredNumBots()
	{
		return desiredNumBots;
	}
	
	
	/**
	 * @param desiredNumBots
	 */
	public void setDesiredNumBots(final int desiredNumBots)
	{
		this.desiredNumBots = desiredNumBots;
	}
}
