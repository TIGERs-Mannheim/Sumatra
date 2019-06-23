/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * A kick event from simulation
 */
@Persistent
public class SimKickEvent implements IKickEvent
{
	private final IVector2 pos;
	private final BotID kickingBot;
	private final long timestamp;
	
	
	@SuppressWarnings("unused")
	private SimKickEvent()
	{
		pos = Vector2.zero();
		kickingBot = BotID.noBot();
		timestamp = 0;
	}
	
	
	public SimKickEvent(final IVector2 pos, final BotID kickingBot, final long timestamp)
	{
		this.pos = pos;
		this.kickingBot = kickingBot;
		this.timestamp = timestamp;
	}
	
	
	@Override
	public IVector2 getPosition()
	{
		return pos;
	}
	
	
	@Override
	public BotID getKickingBot()
	{
		return kickingBot;
	}
	
	
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	@Override
	public IKickEvent mirrored()
	{
		return new SimKickEvent(pos.multiplyNew(-1), kickingBot, timestamp);
	}
}
