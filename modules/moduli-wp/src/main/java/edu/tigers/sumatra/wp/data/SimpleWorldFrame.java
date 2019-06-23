/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * This frame contains tracked (filtered) objects from vision
 * 
 * @author Gero
 */
@Persistent(version = 3)
public class SimpleWorldFrame implements IMirrorable<SimpleWorldFrame>
{
	private final long							frameNumber;
	private final long							timestamp;
	private final IBotIDMap<ITrackedBot>	bots;
	private final ITrackedBall					ball;
	private final IKickEvent					kickEvent;
	
	
	@SuppressWarnings("unused")
	private SimpleWorldFrame()
	{
		frameNumber = 0;
		bots = null;
		ball = null;
		kickEvent = null;
		timestamp = 0;
	}
	
	
	/**
	 * @param bots
	 * @param ball
	 * @param kickEvent
	 * @param frameNumber
	 * @param timestamp
	 */
	public SimpleWorldFrame(final IBotIDMap<ITrackedBot> bots, final ITrackedBall ball, final IKickEvent kickEvent,
			final long frameNumber,
			final long timestamp)
	{
		this.ball = ball;
		this.timestamp = timestamp;
		this.frameNumber = frameNumber;
		this.bots = BotIDMapConst.unmodifiableBotIDMap(bots);
		this.kickEvent = kickEvent;
	}
	
	
	/**
	 * Soft copy
	 * 
	 * @param swf
	 */
	public SimpleWorldFrame(final SimpleWorldFrame swf)
	{
		ball = swf.getBall();
		timestamp = swf.timestamp;
		frameNumber = swf.frameNumber;
		bots = swf.bots;
		kickEvent = swf.kickEvent;
	}
	
	
	/**
	 * Create a new instance of this SimpleWorldFrame and mirror bots and ball
	 * 
	 * @return
	 */
	@Override
	public SimpleWorldFrame mirrored()
	{
		IBotIDMap<ITrackedBot> newBots = new BotIDMap<>();
		for (ITrackedBot bot : bots.values())
		{
			ITrackedBot mBot = bot.mirrored();
			newBots.put(bot.getBotId(), mBot);
		}
		ITrackedBall mBall = getBall().mirrored();
		return new SimpleWorldFrame(newBots, mBall, kickEvent, frameNumber, timestamp);
	}
	
	
	/**
	 * Creates a new WorldFrame without bots
	 * 
	 * @param frameNumber the id of this frame
	 * @param timestamp
	 * @return
	 */
	public static SimpleWorldFrame createEmptyWorldFrame(final long frameNumber, final long timestamp)
	{
		final IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		final IKickEvent kickEvent = null;
		return new SimpleWorldFrame(bots, TrackedBall.createStub(), kickEvent, frameNumber, timestamp);
	}
	
	
	/**
	 * @return all bots
	 */
	public IBotIDMap<ITrackedBot> getBots()
	{
		return bots;
	}
	
	
	/**
	 * @param botId
	 * @return the bot or null
	 */
	public ITrackedBot getBot(final BotID botId)
	{
		return bots.getWithNull(botId);
	}
	
	
	/**
	 * @return the ball
	 */
	public final ITrackedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @return the id
	 */
	public final long getId()
	{
		return frameNumber;
	}
	
	
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the last kick event
	 */
	public Optional<IKickEvent> getKickEvent()
	{
		return Optional.ofNullable(kickEvent);
	}
}
