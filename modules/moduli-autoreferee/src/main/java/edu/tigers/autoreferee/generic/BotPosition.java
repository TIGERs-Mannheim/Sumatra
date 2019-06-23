/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.generic;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Wraps information related to a bot position at a certain point in time
 * 
 * @author "Lukas Magel"
 */
public class BotPosition extends TimedPosition
{
	private final BotID botId;
	
	
	/**
	 * 
	 */
	public BotPosition()
	{
		botId = BotID.noBot();
	}
	
	
	/**
	 * @param timestamp
	 * @param position
	 * @param id
	 */
	public BotPosition(final long timestamp, final IVector2 position, final BotID id)
	{
		super(timestamp, position);
		botId = id;
	}
	
	
	/**
	 * @param ts
	 * @param bot
	 */
	public BotPosition(final long ts, final ITrackedBot bot)
	{
		super(ts, bot.getPos());
		botId = bot.getBotId();
	}
	
	
	/**
	 * @return the id
	 */
	public BotID getBotID()
	{
		return botId;
	}
}
