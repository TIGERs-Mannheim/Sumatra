/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.generic;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TimedPosition;


/**
 * Wraps information related to a bot position at a certain point in time
 */
public class BotPosition extends TimedPosition
{
	private final BotID botId;
	
	
	public BotPosition(final long timestamp, final IVector2 position, final BotID id)
	{
		super(timestamp, Vector3.from2d(position, 0));
		botId = id;
	}
	
	
	public BotPosition(final long ts, final ITrackedBot bot)
	{
		super(ts, Vector3.from2d(bot.getPos(), 0));
		botId = bot.getBotId();
	}
	
	
	public BotID getBotID()
	{
		return botId;
	}
}
