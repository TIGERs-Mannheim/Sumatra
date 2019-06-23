/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;

import java.util.HashMap;
import java.util.Map;


/**
 * @author MarkG
 */
@Persistent()
public class OffensiveStatisticsFrame
{
	private int										minNumBots				= 0;
	private int										maxNumBots				= 0;
	private int										desiredNumBots			= 0;
	private BotID									primaryOffensiveBot	= null;
	
	private Map<BotID, OffensiveBotFrame>	botFrames				= new HashMap<>();
	
	
	/**
	 * @return
	 */
	public Map<BotID, OffensiveBotFrame> getBotFrames()
	{
		return botFrames;
	}
	
	
	/**
	 * @param botFrames
	 */
	public void setBotFrames(final Map<BotID, OffensiveBotFrame> botFrames)
	{
		this.botFrames = botFrames;
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
	
	
	/**
	 * @return
	 */
	public int getMaxNumBots()
	{
		return maxNumBots;
	}
	
	
	/**
	 * @param maxNumBots
	 */
	public void setMaxNumBots(final int maxNumBots)
	{
		this.maxNumBots = maxNumBots;
	}
	
	
	/**
	 * @return
	 */
	public int getMinNumBots()
	{
		return minNumBots;
	}
	
	
	/**
	 * @param minNumBots
	 */
	public void setMinNumBots(final int minNumBots)
	{
		this.minNumBots = minNumBots;
	}
	
}
