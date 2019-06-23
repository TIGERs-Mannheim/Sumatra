/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


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
	private final IVector pos;
	private final IVector vel;
	private final IVector acc;
	private final double vSwitchToRoll;
	private final boolean chipped;
	private final double spin;
	
	
	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	protected BallTrajectoryState()
	{
		pos = Vector3f.ZERO_VECTOR;
		vel = Vector3f.ZERO_VECTOR;
		acc = Vector3f.ZERO_VECTOR;
		vSwitchToRoll = 0;
		chipped = false;
		spin = 0;
	}
	
	
	/**
	 * @param pos
	 * @param vel
	 * @param acc
	 * @param vSwitchToRoll
	 * @param chipped
	 * @param spin
	 */
	private BallTrajectoryState(final IVector pos, final IVector vel, final IVector acc, final double vSwitchToRoll,
			final boolean chipped, final double spin)
	{
		this.pos = pos;
		this.vel = vel;
		this.acc = acc;
		this.vSwitchToRoll = vSwitchToRoll;
		this.chipped = chipped;
		this.spin = spin;
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
		spin = builder.spin;
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
		builder.spin = copy.spin;
		return builder;
	}
	
	
	/**
	 * Position in [mm]
	 * 
	 * @return
	 */
	public IVector getPos()
	{
		return pos;
	}
	
	
	/**
	 * Velocity in [mm/s]
	 * 
	 * @return
	 */
	public IVector getVel()
	{
		return vel;
	}
	
	
	/**
	 * Acceleration in [mm/s^2]
	 * 
	 * @return
	 */
	public IVector getAcc()
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
	 * @return the spin
	 */
	public double getSpin()
	{
		return spin;
	}
	
	
	/**
	 * @return mirrored copy
	 */
	public BallTrajectoryState mirrored()
	{
		IVector3 newPos = Vector3.from2d(pos.getXYVector().multiplyNew(-1), pos.getXYZVector().z());
		IVector3 newVel = Vector3.from2d(vel.getXYVector().multiplyNew(-1), vel.getXYZVector().z());
		IVector3 newAcc = Vector3.from2d(acc.getXYVector().multiplyNew(-1), acc.getXYZVector().z());
		
		return new BallTrajectoryState(newPos, newVel, newAcc, vSwitchToRoll, chipped, spin);
	}
	
	
	/**
	 * {@code BallTrajectoryState} builder static inner class.
	 */
	public static final class Builder
	{
		private IVector pos;
		private IVector vel;
		private IVector acc = Vector3f.ZERO_VECTOR;
		private double vSwitchToRoll = 0;
		private boolean chipped = false;
		private double spin = 0;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code pos} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param pos the {@code pos} to set
		 * @return a reference to this Builder
		 */
		public Builder withPos(final IVector pos)
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
		public Builder withVel(final IVector vel)
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
		public Builder withAcc(final IVector acc)
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
		 * Sets the {@code spin} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param spin the {@code spin} to set
		 * @return a reference to this Builder
		 */
		public Builder withSpin(final double spin)
		{
			this.spin = spin;
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
