/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Data structure for a filtered vision ball.
 * <br>
 * <b>WARNING: Units of this class are [mm], [mm/s], [mm/s^2] !!!</b>
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author AndreR <andre@ryll.cc>
 */
public class FilteredVisionBall
{
	private final IVector3	pos;
	private final IVector3	vel;
	private final IVector3	acc;
	private final boolean	chipped;
	private final double		vSwitch;
	private final long		lastVisibleTimestamp;
	
	
	private FilteredVisionBall(final Builder builder)
	{
		pos = builder.pos;
		vel = builder.vel;
		acc = builder.acc;
		chipped = builder.chipped;
		if (builder.vSwitch < 0)
		{
			vSwitch = vel.getLength2();
		} else
		{
			vSwitch = builder.vSwitch;
		}
		lastVisibleTimestamp = builder.lastVisibleTimestamp;
	}
	
	
	/**
	 * Position in [mm]
	 * 
	 * @return
	 */
	public IVector3 getPos()
	{
		return pos;
	}
	
	
	/**
	 * Velocity in [mm/s]
	 * 
	 * @return
	 */
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	/**
	 * Acceleration in [mm/s^2]
	 * 
	 * @return
	 */
	public IVector3 getAcc()
	{
		return acc;
	}
	
	
	public boolean isChipped()
	{
		return chipped;
	}
	
	
	public long getLastVisibleTimestamp()
	{
		return lastVisibleTimestamp;
	}
	
	
	public double getVSwitch()
	{
		return vSwitch;
	}
	
	
	@Override
	public String toString()
	{
		return "FilteredVisionBall{" +
				", pos=" + pos +
				", vel=" + vel +
				", acc=" + acc +
				'}';
	}
	
	/**
	 * Builder
	 */
	public static final class Builder
	{
		private IVector3	pos;
		private IVector3	vel;
		private IVector3	acc;
		private Boolean	chipped;
		private double		vSwitch					= -1;
		private long		lastVisibleTimestamp	= 0;
		
		
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
		 * @param pos of the ball in [mm]
		 * @return this builder
		 */
		public Builder withPos(final IVector3 pos)
		{
			this.pos = pos;
			return this;
		}
		
		
		/**
		 * @param vel of the ball in [mm/2]
		 * @return this builder
		 */
		public Builder withVel(final IVector3 vel)
		{
			this.vel = vel;
			return this;
		}
		
		
		/**
		 * @param acc of the ball in [mm/s^2]
		 * @return this builder
		 */
		public Builder withAcc(final IVector3 acc)
		{
			this.acc = acc;
			return this;
		}
		
		
		/**
		 * @param chipped
		 * @return this builder
		 */
		public Builder withIsChipped(final boolean chipped)
		{
			this.chipped = chipped;
			return this;
		}
		
		
		/**
		 * @param vSwitch velocity where the ball switches from slide to roll
		 * @return this builder
		 */
		public Builder withvSwitch(final double vSwitch)
		{
			this.vSwitch = vSwitch;
			return this;
		}
		
		
		/**
		 * @param visibleTimestamp timestamp when the ball was really last seen by a camera/barrier
		 * @return this builder
		 */
		public Builder withLastVisibleTimestamp(final long visibleTimestamp)
		{
			lastVisibleTimestamp = visibleTimestamp;
			return this;
		}
		
		
		/**
		 * @return new instance
		 */
		public FilteredVisionBall build()
		{
			Validate.notNull(pos);
			Validate.notNull(vel);
			Validate.notNull(acc);
			Validate.notNull(chipped);
			Validate.isTrue(Double.isFinite(vSwitch));
			return new FilteredVisionBall(this);
		}
	}
}
