/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IMirrorable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


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
	private final KickedBall kickedBall;
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private transient Map<BotID, ITrackedBot> botsReadOnly;


	@SuppressWarnings("unused")
	private SimpleWorldFrame()
	{
		frameNumber = 0;
		timestamp = 0;
		bots = null;
		ball = null;
		kickedBall = null;
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
		kickedBall = swf.kickedBall;
	}


	/**
	 * Create a new instance of this SimpleWorldFrame and mirror bots and ball
	 *
	 * @return
	 */
	@Override
	public SimpleWorldFrame mirrored()
	{
		var mBots = bots.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().mirrored()));
		var mBall = getBall().mirrored();
		var mKickedBall = kickedBall == null ? null : kickedBall.mirrored();
		return new SimpleWorldFrame(frameNumber, timestamp, mBots, mBall, mKickedBall);
	}


	/**
	 * @param botId
	 * @return the bot or null
	 */
	public ITrackedBot getBot(final BotID botId)
	{
		return bots.get(botId);
	}


	public Optional<KickedBall> getKickedBall()
	{
		return Optional.ofNullable(kickedBall);
	}


	public Map<BotID, ITrackedBot> getBots()
	{
		if (botsReadOnly == null)
		{
			botsReadOnly = Collections.unmodifiableMap(bots);
		}
		return botsReadOnly;
	}
}
