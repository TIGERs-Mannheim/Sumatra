/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Wraps information related to a bot position at a certain point in time
 * 
 * @author "Lukas Magel"
 */
public class BotPosition
{
	private long				ts;
	private final BotID		id;
	private final IVector2	pos;
	
	
	/**
	 * 
	 */
	public BotPosition()
	{
		ts = 0;
		id = BotID.get();
		pos = AVector2.ZERO_VECTOR;
	}
	
	
	/**
	 * @param ts
	 * @param id
	 * @param pos
	 */
	public BotPosition(final long ts, final BotID id, final IVector2 pos)
	{
		setTs(ts);
		this.id = id;
		this.pos = pos;
	}
	
	
	/**
	 * @param ts
	 * @param bot
	 */
	public BotPosition(final long ts, final ITrackedBot bot)
	{
		setTs(ts);
		id = bot.getBotId();
		pos = bot.getPos();
	}
	
	
	/**
	 * @return the ts
	 */
	public long getTs()
	{
		return ts;
	}
	
	
	/**
	 * @param ts the ts to set
	 */
	public void setTs(final long ts)
	{
		this.ts = ts;
	}
	
	
	/**
	 * @return the id
	 */
	public BotID getId()
	{
		return id;
	}
	
	
	/**
	 * @return the pos
	 */
	public IVector2 getPos()
	{
		return pos;
	}
}
