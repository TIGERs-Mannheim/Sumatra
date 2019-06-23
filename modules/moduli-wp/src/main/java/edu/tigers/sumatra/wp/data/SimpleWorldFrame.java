/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
@Persistent(version = 4)
public class SimpleWorldFrame implements IMirrorable<SimpleWorldFrame>
{
	private final long frameNumber;
	private final long timestamp;
	private final long tAssembly;
	private final IBotIDMap<ITrackedBot> bots;
	private final ITrackedBall ball;
	private final IKickEvent kickEvent;
	private final BallKickFitState kickFitState;
	
	
	@SuppressWarnings("unused")
	private SimpleWorldFrame()
	{
		frameNumber = 0;
		bots = null;
		ball = null;
		kickEvent = null;
		kickFitState = null;
		timestamp = 0;
		tAssembly = 0;
	}
	
	
	/**
	 * @param bots
	 * @param ball
	 * @param kickEvent
	 * @param kickFitState
	 * @param frameNumber
	 * @param timestamp
	 */
	public SimpleWorldFrame(final IBotIDMap<ITrackedBot> bots, final ITrackedBall ball, final IKickEvent kickEvent,
			final BallKickFitState kickFitState,
			final long frameNumber,
			final long timestamp)
	{
		this.ball = ball;
		this.timestamp = timestamp;
		this.frameNumber = frameNumber;
		this.bots = BotIDMapConst.unmodifiableBotIDMap(bots);
		this.kickEvent = kickEvent;
		this.kickFitState = kickFitState;
		
		tAssembly = System.nanoTime();
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
		tAssembly = swf.tAssembly;
		frameNumber = swf.frameNumber;
		bots = swf.bots;
		kickEvent = swf.kickEvent;
		kickFitState = swf.kickFitState;
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
		IKickEvent mKickEvent = Optional.ofNullable(kickEvent).map(IKickEvent::mirrored).orElse(null);
		return new SimpleWorldFrame(newBots, mBall, mKickEvent, kickFitState, frameNumber, timestamp);
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
		return new SimpleWorldFrame(bots, TrackedBall.createStub(), kickEvent, null, frameNumber, timestamp);
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
	
	
	/**
	 * @return the last kick fit state
	 */
	public Optional<BallKickFitState> getKickFitState()
	{
		return Optional.ofNullable(kickFitState);
	}
	
	
	/**
	 * @return the assembly timestamp in [ns]
	 */
	public long gettAssembly()
	{
		return tAssembly;
	}
}
