/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.botdistance;

import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * Simple data holder which associates a {@link TrackedBot} with a double-value
 * 
 * @author Gero
 */
public class BotDistance
{
	/**
	 * Used to provide a not-null {@link BotDistance} even if no distances have been calculated! All its members are
	 * <code>null</code>!
	 */
	public static final BotDistance NULL_BOT_DISTANCE = new BotDistance(null, Double.MAX_VALUE);
	
	
	private ITrackedBot bot;
	private double dist;
	
	
	/**
	 * @param bot
	 * @param dist
	 */
	public BotDistance(final ITrackedBot bot, final double dist)
	{
		this.bot = bot;
		this.dist = dist;
	}
	
	
	/**
	 * @return the bot
	 */
	public ITrackedBot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @return the dist
	 */
	public double getDist()
	{
		return dist;
	}
}
