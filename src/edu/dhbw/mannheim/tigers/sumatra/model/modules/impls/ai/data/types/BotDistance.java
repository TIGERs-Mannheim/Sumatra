/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.07.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import java.util.Comparator;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;


/**
 * Simple data holder which associates a {@link TrackedBot} with a float-value
 * 
 * @author Gero
 */
public class BotDistance
{
	public static final Comparator<BotDistance>	ASCENDING			= new Comparator<BotDistance>()
	{
		@Override
		public int compare(BotDistance d1, BotDistance d2)
		{
			return Float.compare(d1.dist, d2.dist);
		}
	};
	
	public static final Comparator<BotDistance>	DESCENDING			= new Comparator<BotDistance>()
	{
		@Override
		public int compare(BotDistance d1, BotDistance d2)
		{
			return -Float.compare(d1.dist, d2.dist);
		}
	};
	
	/**
	 * Used to provide a not-null {@link BotDistance} even if no distances have been calculated! All its members are
	 * <code>null</code>!
	 */
	public static final BotDistance					NULL_BOT_DISTANCE	= new BotDistance(null, Float.MAX_VALUE);
	

	public final TrackedBot								bot;
	public final float									dist;
	
	
	public BotDistance(TrackedBot bot, float dist)
	{
		this.bot = bot;
		this.dist = dist;
	}
}
