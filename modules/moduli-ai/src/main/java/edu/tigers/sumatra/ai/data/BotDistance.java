/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.07.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.io.Serializable;
import java.util.Comparator;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * Simple data holder which associates a {@link TrackedBot} with a double-value
 * 
 * @author Gero
 */
@Persistent
public class BotDistance implements Serializable
{
	/**
	 */
	public static final Comparator<BotDistance>	ASCENDING			= new Comparator<BotDistance>()
																						{
																							@Override
																							public int compare(final BotDistance d1,
																									final BotDistance d2)
																							{
																								return Double
																										.compare(d1.getDist(), d2.getDist());
																							}
																						};
	/**
	 */
	public static final Comparator<BotDistance>	DESCENDING			= new Comparator<BotDistance>()
																						{
																							@Override
																							public int compare(final BotDistance d1,
																									final BotDistance d2)
																							{
																								return Double
																										.compare(d2.getDist(), d1.getDist());
																							}
																						};
	
	
	/**  */
	private static final long							serialVersionUID	= 1131465265437655081L;
	
	/**
	 * Used to provide a not-null {@link BotDistance} even if no distances have been calculated! All its members are
	 * <code>null</code>!
	 */
	public static final BotDistance					NULL_BOT_DISTANCE	= new BotDistance(null, Double.MAX_VALUE);
	
	
	/** */
	private ITrackedBot									bot;
	/** */
	private double											dist;
	
	
	@SuppressWarnings("unused")
	private BotDistance()
	{
	}
	
	
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
