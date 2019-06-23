/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Data structure for the ball state at a certain time.
 * <br>
 * <b>WARNING: Units of this class are [mm], [mm/s], [mm/s^2] !!!</b>
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class BallTrajectoryState
{
	private final IVector3	pos;
	private final IVector3	vel;
	private final IVector3	acc;
	private final double		vSwitchToRoll;
	private final boolean	chipped;
	
	
	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	protected BallTrajectoryState()
	{
		pos = AVector3.ZERO_VECTOR;
		vel = AVector3.ZERO_VECTOR;
		acc = AVector3.ZERO_VECTOR;
		vSwitchToRoll = 0;
		chipped = false;
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param vSwitchToRoll
	 * @param chipped
	 */
	private BallTrajectoryState(final IVector3 pos, final IVector3 vel, final IVector3 acc, final double vSwitchToRoll,
			final boolean chipped)
	{
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.vSwitchToRoll = vSwitchToRoll;
		this.chipped = chipped;
	}
	
	
	private BallTrajectoryState(final Builder builder)
	{
		Validate.notNull(builder.pos);
		Validate.notNull(builder.vel);
		Validate.notNull(builder.acc);
		pos = builder.pos;
		vel = builder.vel;
		acc = builder.acc;
		vSwitchToRoll = builder.vSwitchToRoll;
		chipped = builder.chipped;
	}
	
	
	/**
	 * @return a new builder
	 */
	public static Builder aBallState()
	{
		return new Builder();
	}
	
	
	/**
	 * @param copy the instance to copy
	 * @return a new builder
	 */
	public static Builder aBallStateCopyOf(final BallTrajectoryState copy)
	{
		Builder builder = new Builder();
		builder.pos = copy.pos;
		builder.vel = copy.vel;
		builder.acc = copy.acc;
		builder.vSwitchToRoll = copy.vSwitchToRoll;
		builder.chipped = copy.chipped;
		return builder;
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
	
	
	@Override
	public String toString()
	{
		return "BallTrajectoryState {" +
				", pos=" + pos +
				", vel=" + vel +
				", acc=" + acc +
				'}';
	}
	
	
	/**
	 * @return the velocity where the ball turns to roll
	 */
	public double getvSwitchToRoll()
	{
		return vSwitchToRoll;
	}
	
	
	/**
	 * @return
	 */
	public boolean isChipped()
	{
		return chipped;
	}
	
	
	/**
	 * @return mirrored copy
	 */
	public BallTrajectoryState mirrored()
	{
		IVector3 newPos = Vector3.from2d(pos.getXYVector().multiplyNew(-1), pos.z());
		IVector3 newVel = Vector3.from2d(vel.getXYVector().multiplyNew(-1), vel.z());
		IVector3 newAcc = Vector3.from2d(acc.getXYVector().multiplyNew(-1), acc.z());
		
		return new BallTrajectoryState(newPos, newVel, newAcc, vSwitchToRoll, chipped);
	}
	
	
	/**
	 * {@code BallTrajectoryState} builder static inner class.
	 */
	public static final class Builder
	{
		private IVector3	pos;
		private IVector3	vel;
		private IVector3	acc				= Vector3.ZERO_VECTOR;
		private double		vSwitchToRoll	= 0;
		private boolean	chipped			= false;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code pos} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param pos the {@code pos} to set
		 * @return a reference to this Builder
		 */
		public Builder withPos(final IVector3 pos)
		{
			this.pos = pos;
			return this;
		}
		
		
		/**
		 * Sets the {@code vel} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param vel the {@code vel} to set in [mm/s]
		 * @return a reference to this Builder
		 */
		public Builder withVel(final IVector3 vel)
		{
			this.vel = vel;
			return this;
		}
		
		
		/**
		 * Sets the {@code acc} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param acc the {@code acc} to set in [mm/s^2]
		 * @return a reference to this Builder
		 */
		public Builder withAcc(final IVector3 acc)
		{
			this.acc = acc;
			return this;
		}
		
		
		/**
		 * Sets the {@code vSwitchToRoll} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param vSwitchToRoll the {@code vSwitchToRoll} to set
		 * @return a reference to this Builder
		 */
		public Builder withVSwitchToRoll(final double vSwitchToRoll)
		{
			this.vSwitchToRoll = vSwitchToRoll;
			return this;
		}
		
		
		/**
		 * Sets the {@code chipped} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param chipped the {@code chipped} to set
		 * @return a reference to this Builder
		 */
		public Builder withChipped(final boolean chipped)
		{
			this.chipped = chipped;
			return this;
		}
		
		
		/**
		 * Returns a {@code BallTrajectoryState} built from the parameters previously set.
		 *
		 * @return a {@code BallTrajectoryState} built with parameters of this {@code BallTrajectoryState.Builder}
		 */
		public BallTrajectoryState build()
		{
			return new BallTrajectoryState(this);
		}
	}
}
