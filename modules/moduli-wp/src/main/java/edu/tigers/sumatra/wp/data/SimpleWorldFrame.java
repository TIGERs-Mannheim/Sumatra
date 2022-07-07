/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.vision.data.IKickEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * This frame contains tracked (filtered) objects from vision in the vision coordinate system.
 */
@Persistent(version = 5)
@Data
@RequiredArgsConstructor
public class SimpleWorldFrame implements IMirrorable<SimpleWorldFrame>
{
	private final long frameNumber;
	private final long timestamp;
	private final Map<BotID, ITrackedBot> bots;
	private final ITrackedBall ball;
	private final IKickEvent kickEvent;
	private final BallKickFitState kickFitState;
	private transient Map<BotID, ITrackedBot> botsReadOnly;


	@SuppressWarnings("unused")
	private SimpleWorldFrame()
	{
		frameNumber = 0;
		timestamp = 0;
		bots = null;
		ball = null;
		kickEvent = null;
		kickFitState = null;
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
		Map<BotID, ITrackedBot> newBots = new HashMap<>();
		for (ITrackedBot bot : bots.values())
		{
			ITrackedBot mBot = bot.mirrored();
			newBots.put(bot.getBotId(), mBot);
		}
		ITrackedBall mBall = getBall().mirrored();
		IKickEvent mKickEvent = Optional.ofNullable(kickEvent).map(IKickEvent::mirrored).orElse(null);
		BallKickFitState mKickFitState = Optional.ofNullable(kickFitState).map(BallKickFitState::mirrored).orElse(null);
		return new SimpleWorldFrame(frameNumber, timestamp, newBots, mBall, mKickEvent, mKickFitState);
	}


	/**
	 * @param botId
	 * @return the bot or null
	 */
	public ITrackedBot getBot(final BotID botId)
	{
		return bots.get(botId);
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


	public Map<BotID, ITrackedBot> getBots()
	{
		if (botsReadOnly == null)
		{
			botsReadOnly = Collections.unmodifiableMap(bots);
		}
		return botsReadOnly;
	}


	/**
	 * @param botID
	 * @return a new map containing all bots except for the given ones
	 */
	public Map<BotID, ITrackedBot> getAllBotsBut(BotID... botID)
	{
		var allBots = new HashMap<>(bots);
		Arrays.asList(botID).forEach(allBots.keySet()::remove);
		return allBots;
	}
}
