/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.AVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FilteredVisionFrame
{
	private final long							id;
	private final long							timestamp;
	private final FilteredVisionBall			ball;
	private final List<FilteredVisionBot>	bots;
	private final ShapeMap						shapeMap	= new ShapeMap();
	private final IKickEvent					kickEvent;
	
	
	private FilteredVisionFrame(final long id, final long timestamp, final FilteredVisionBall ball,
			final List<FilteredVisionBot> bots, final IKickEvent kickEvent)
	{
		this.id = id;
		this.timestamp = timestamp;
		this.ball = ball;
		this.bots = bots;
		this.kickEvent = kickEvent;
	}
	
	
	public long getId()
	{
		return id;
	}
	
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	public FilteredVisionBall getBall()
	{
		return ball;
	}
	
	
	public List<FilteredVisionBot> getBots()
	{
		return bots;
	}
	
	
	public ShapeMap getShapeMap()
	{
		return shapeMap;
	}
	
	
	public Optional<IKickEvent> getKickEvent()
	{
		return Optional.ofNullable(kickEvent);
	}
	
	
	@Override
	public String toString()
	{
		return "FilteredVisionFrame{" +
				"id=" + id +
				", timestamp=" + timestamp +
				", ball=" + ball +
				", bots=" + bots +
				'}';
	}
	
	/**
	 * Builder for this sub class
	 */
	public static final class Builder
	{
		private Long							id;
		private Long							timestamp;
		private FilteredVisionBall			ball;
		private List<FilteredVisionBot>	bots;
		private IKickEvent					kickEvent;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * @return new builder
		 */
		public static Builder create()
		{
			return new Builder();
		}
		
		
		/**
		 * Create an empty frame, id is zero, ball at (0,0)
		 * 
		 * @return
		 */
		public static FilteredVisionFrame createEmptyFrame()
		{
			FilteredVisionBall b = FilteredVisionBall.Builder.create()
					.withPos(AVector3.ZERO_VECTOR)
					.withVel(AVector3.ZERO_VECTOR)
					.withAcc(AVector3.ZERO_VECTOR)
					.withIsChipped(false)
					.build();
			
			return create()
					.withId(0)
					.withTimestamp(0)
					.withBots(new ArrayList<FilteredVisionBot>())
					.withBall(b)
					.build();
		}
		
		
		/**
		 * @param id frame id
		 * @return this builder
		 */
		public Builder withId(final long id)
		{
			this.id = id;
			return this;
		}
		
		
		/**
		 * @param timestamp in [ns]
		 * @return this builder
		 */
		public Builder withTimestamp(final long timestamp)
		{
			this.timestamp = timestamp;
			return this;
		}
		
		
		/**
		 * @param ball currently detected ball
		 * @return this builder
		 */
		public Builder withBall(final FilteredVisionBall ball)
		{
			this.ball = ball;
			return this;
		}
		
		
		/**
		 * @param bots currently detected balls
		 * @return this builder
		 */
		public Builder withBots(final List<FilteredVisionBot> bots)
		{
			this.bots = bots;
			return this;
		}
		
		
		/**
		 * @param event
		 * @return
		 */
		public Builder withKickEvent(final IKickEvent event)
		{
			kickEvent = event;
			return this;
		}
		
		
		/**
		 * @return new instance
		 */
		public FilteredVisionFrame build()
		{
			Validate.notNull(id);
			Validate.notNull(timestamp);
			Validate.notNull(ball);
			Validate.notNull(bots);
			return new FilteredVisionFrame(id, timestamp, ball, bots, kickEvent);
		}
	}
}
