/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * The output frame of a vision filter
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FilteredVisionFrame
{
	private final long id;
	private final long timestamp;
	private final long tAssembly;
	private final FilteredVisionBall ball;
	private final List<FilteredVisionBot> bots;
	private final ShapeMap shapeMap;
	private final IKickEvent kickEvent;
	private final FilteredVisionBall kickFitState;
	
	
	private FilteredVisionFrame(final Builder builder)
	{
		id = builder.id;
		timestamp = builder.timestamp;
		ball = builder.ball;
		bots = builder.bots;
		kickEvent = builder.kickEvent;
		kickFitState = builder.kickFitState;
		if (builder.shapeMap != null)
		{
			shapeMap = builder.shapeMap;
		} else
		{
			shapeMap = new ShapeMap();
		}
		
		tAssembly = System.nanoTime();
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
	
	
	public Optional<FilteredVisionBall> getKickFitState()
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
		private Long id;
		private Long timestamp;
		private FilteredVisionBall ball;
		private List<FilteredVisionBot> bots;
		private IKickEvent kickEvent;
		private ShapeMap shapeMap = null;
		private FilteredVisionBall kickFitState;
		
		
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
					.withPos(Vector3f.ZERO_VECTOR)
					.withVel(Vector3f.ZERO_VECTOR)
					.withAcc(Vector3f.ZERO_VECTOR)
					.withIsChipped(false)
					.build();
			
			return create()
					.withId(0)
					.withTimestamp(0)
					.withBots(new ArrayList<>())
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
		 * @param ball
		 * @return
		 */
		public Builder withKickFitState(final FilteredVisionBall ball)
		{
			kickFitState = ball;
			return this;
		}
		
		
		/**
		 * @param map
		 * @return
		 */
		public Builder withShapeMap(final ShapeMap map)
		{
			shapeMap = map;
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
			return new FilteredVisionFrame(this);
		}
	}
}
