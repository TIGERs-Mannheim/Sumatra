/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * Describes a Kick Event.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class KickEvent implements IKickEvent
{
	private final BotID								kickingBot;
	private final IVector2							position;
	private final long								timestamp;
	private final transient List<MergedBall>	recordsSinceKick	= new ArrayList<>();
	private final boolean							isEarlyDetection;
	
	
	@SuppressWarnings("unused")
	private KickEvent()
	{
		this(AVector2.ZERO_VECTOR, FilteredVisionBot.Builder.emptyBot(), 0, Collections.emptyList(), false);
	}
	
	
	/**
	 * @param position
	 * @param kickingBot
	 * @param timestamp
	 * @param recordsSinceKick
	 * @param earlyDetection
	 */
	public KickEvent(final IVector2 position, final FilteredVisionBot kickingBot, final long timestamp,
			final List<MergedBall> recordsSinceKick, final boolean earlyDetection)
	{
		this.position = position;
		this.kickingBot = kickingBot.getBotID();
		this.timestamp = timestamp;
		this.recordsSinceKick.addAll(recordsSinceKick);
		isEarlyDetection = earlyDetection;
	}
	
	
	/**
	 * @return the position
	 */
	@Override
	public IVector2 getPosition()
	{
		return position;
	}
	
	
	/**
	 * @return the kickingBot
	 */
	@Override
	public BotID getKickingBot()
	{
		return kickingBot;
	}
	
	
	/**
	 * @return the timestamp
	 */
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the recordsSinceKick
	 */
	public List<MergedBall> getRecordsSinceKick()
	{
		return Collections.unmodifiableList(recordsSinceKick);
	}
	
	
	public MergedBall getLatestRecordSinceKick()
	{
		return recordsSinceKick.get(recordsSinceKick.size() - 1);
	}
	
	
	public boolean isEarlyDetection()
	{
		return isEarlyDetection;
	}
	
	
	public Optional<IVector2> getKickDirection()
	{
		List<IVector2> points = recordsSinceKick.stream()
				.map(MergedBall::getCamPos)
				.collect(Collectors.toList());
		
		Optional<Line> line = Line.fromPointsList(points);
		if (line.isPresent())
		{
			return Optional.of(line.get().directionVector());
		}
		
		return Optional.empty();
	}
}