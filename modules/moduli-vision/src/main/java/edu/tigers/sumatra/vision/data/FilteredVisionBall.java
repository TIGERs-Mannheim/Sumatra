/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
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
	private final IVector3 pos;
	private final IVector3 vel;
	private final IVector3 acc;
	private final boolean chipped;
	private final double vSwitch;
	private final long lastVisibleTimestamp;
	private final double spin;
	
	
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
		spin = builder.spin;
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
	
	
	public double getSpin()
	{
		return spin;
	}
	
	
	/**
	 * Extrapolate ball by using trajectory.
	 * 
	 * @param timestampNow
	 * @param timestampFuture
	 * @return
	 */
	public FilteredVisionBall extrapolate(final long timestampNow, final long timestampFuture)
	{
		if (timestampFuture < timestampNow)
		{
			return this;
		}
		
		return getTrajectory(timestampNow).getStateAtTimestamp(timestampFuture);
	}
	
	
	/**
	 * Get ball trajectory.
	 * 
	 * @param timestampNow
	 * @return
	 */
	public ABallTrajectory getTrajectory(final long timestampNow)
	{
		ABallTrajectory trajectory;
		if (chipped)
		{
			trajectory = new ChipBallTrajectory(timestampNow, this);
		} else
		{
			long switchTimestamp = timestampNow;
			if (acc.getLength2() > 1e-6)
			{
				switchTimestamp = timestampNow + (long) (((vel.getLength2() - vSwitch) / acc.getLength2()) * 1e9);
			}
			
			trajectory = new StraightBallTrajectory(timestampNow, pos, vel, switchTimestamp);
		}
		
		return trajectory;
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
		private IVector3 pos;
		private IVector3 vel;
		private IVector3 acc;
		private Boolean chipped;
		private double vSwitch = -1;
		private long lastVisibleTimestamp = 0;
		private double spin = 0;
		
		
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
		 * @param base
		 * @return new builder based on given base
		 */
		public static Builder create(final FilteredVisionBall base)
		{
			return new Builder()
					.withPos(base.pos)
					.withVel(base.vel)
					.withAcc(base.acc)
					.withIsChipped(base.chipped)
					.withvSwitch(base.vSwitch)
					.withSpin(base.spin)
					.withLastVisibleTimestamp(base.lastVisibleTimestamp);
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
		 * @param spin forward/topspin of the ball, backspin is negative
		 * @return this builder
		 */
		public Builder withSpin(final double spin)
		{
			this.spin = spin;
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
			Validate.isTrue(Double.isFinite(spin));
			return new FilteredVisionBall(this);
		}
	}
}
