/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OffensiveTimeEstimation
{
	private final double	ballContactTime;
	private final double	secondaryTime;
	
	
	private OffensiveTimeEstimation(final Builder builder)
	{
		ballContactTime = builder.ballContactTime;
		secondaryTime = builder.secondaryTime;
	}
	
	
	/**
	 * @return a new builder
	 */
	public static Builder newBuilder()
	{
		return new Builder();
	}
	
	
	/**
	 * @param copy instance to copy
	 * @return a new builder
	 */
	public static Builder newBuilder(final OffensiveTimeEstimation copy)
	{
		Builder builder = new Builder();
		builder.ballContactTime = copy.ballContactTime;
		builder.secondaryTime = copy.secondaryTime;
		return builder;
	}
	
	
	/**
	 * @return ballContact time
	 */
	public double getBallContactTime()
	{
		return ballContactTime;
	}
	
	
	/**
	 * {@code OffensiveTimeEstimation} builder static inner class.
	 */
	public static final class Builder
	{
		private double	ballContactTime;
		private double	secondaryTime;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code ballContactTime} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param ballContactTime the {@code ballContactTime} to set
		 * @return a reference to this Builder
		 */
		public Builder withBallContactTime(final double ballContactTime)
		{
			this.ballContactTime = ballContactTime;
			return this;
		}
		
		
		/**
		 * Sets the {@code secondaryTime} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param secondaryTime the {@code secondaryTime} to set
		 * @return a reference to this Builder
		 */
		public Builder withSecondaryTime(final double secondaryTime)
		{
			this.secondaryTime = secondaryTime;
			return this;
		}
		
		
		/**
		 * Returns a {@code OffensiveTimeEstimation} built from the parameters previously set.
		 *
		 * @return a {@code OffensiveTimeEstimation} built with parameters of this {@code OffensiveTimeEstimation.Builder}
		 */
		public OffensiveTimeEstimation build()
		{
			return new OffensiveTimeEstimation(this);
		}
	}
}
