/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.IBotIDMap;


/**
 * This frame contains tracked (filtered) objects from vision
 * 
 * @author Gero
 */
@Persistent(version = 3)
public class SimpleWorldFrame
{
	private final long									frameNumber;
	private final IBotIDMap<ITrackedBot>			bots;
	private final TrackedBall							ball;
	private final long									timestamp;
	private transient ExtendedCamDetectionFrame	frame	= null;
	
	
	@SuppressWarnings("unused")
	private SimpleWorldFrame()
	{
		frameNumber = 0;
		bots = null;
		ball = null;
		timestamp = 0;
	}
	
	
	/**
	 * @param bots
	 * @param ball
	 * @param frameNumber
	 * @param timestamp
	 */
	public SimpleWorldFrame(final IBotIDMap<ITrackedBot> bots, final TrackedBall ball, final long frameNumber,
			final long timestamp)
	{
		this.ball = ball;
		this.timestamp = timestamp;
		this.frameNumber = frameNumber;
		this.bots = BotIDMapConst.unmodifiableBotIDMap(bots);
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
		frame = swf.frame;
	}
	
	
	/**
	 * Create a new instance of this SimpleWorldFrame and mirror bots and ball
	 * 
	 * @return
	 */
	public SimpleWorldFrame mirrorNew()
	{
		IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		for (ITrackedBot bot : this.bots.values())
		{
			ITrackedBot mBot = bot.mirrorNew();
			bots.put(bot.getBotId(), mBot);
		}
		TrackedBall mBall = new TrackedBall(getBall()).mirrorNew();
		SimpleWorldFrame frame = new SimpleWorldFrame(bots, mBall, frameNumber, timestamp);
		frame.setCamFrame(getCamFrame());
		return frame;
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
		final TrackedBall ball = TrackedBall.defaultInstance();
		
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, frameNumber, timestamp);
		return swf;
	}
	
	
	/**
	 * @return all bots
	 */
	public final IBotIDMap<ITrackedBot> getBots()
	{
		return bots;
	}
	
	
	/**
	 * @param botId
	 * @return the bot or null
	 */
	public final ITrackedBot getBot(final BotID botId)
	{
		return bots.getWithNull(botId);
	}
	
	
	/**
	 * @return the ball
	 */
	public final TrackedBall getBall()
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
	 * @param frame
	 */
	public void setCamFrame(final ExtendedCamDetectionFrame frame)
	{
		this.frame = frame;
	}
	
	
	/**
	 * @return the frame
	 */
	public ExtendedCamDetectionFrame getCamFrame()
	{
		return frame;
	}
}
