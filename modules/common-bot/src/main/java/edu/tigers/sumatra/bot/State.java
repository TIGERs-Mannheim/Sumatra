/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;


@Persistent
public class State implements IMirrorable<State>
{
	private final Pose pose;
	/** [x,y,z] velocity in [m/s,m/s,rad/s] */
	private final IVector3 vel3;
	
	
	protected State()
	{
		pose = Pose.from(Vector3.zero());
		vel3 = Vector3.zero();
	}
	
	
	/**
	 * @param pose the pose
	 * @param vel3 [m/s,m/s,rad/s]
	 */
	protected State(final Pose pose, final IVector3 vel3)
	{
		this.pose = pose;
		this.vel3 = vel3;
	}
	
	
	/**
	 * @param pose the pose
	 * @param vel [m/s,m/s,rad/s]
	 * @return
	 */
	public static State of(final Pose pose, final IVector3 vel)
	{
		return new State(pose, vel);
	}
	
	
	public static State zero()
	{
		return State.of(Pose.zero(), Vector3f.zero());
	}
	
	
	@Override
	public State mirrored()
	{
		return State.of(pose.mirrored(), Vector3.from2d(vel3.getXYVector().multiplyNew(-1), vel3.z()));
	}
	
	
	public State interpolate(final State state, double percentage)
	{
		IVector3 velDiff = state.vel3.subtractNew(vel3).multiply(percentage);
		IVector3 intpVel = vel3.addNew(velDiff);
		return State.of(pose.interpolate(state.pose, percentage), intpVel);
	}
	
	
	public Pose getPose()
	{
		return pose;
	}
	
	
	/**
	 * @return [mm,mm]
	 */
	public IVector2 getPos()
	{
		return pose.getPos();
	}
	
	
	/**
	 * @return [rad]
	 */
	public double getOrientation()
	{
		return pose.getOrientation();
	}
	
	
	/**
	 * @return [m/s,m/s,rad/s]
	 */
	public IVector3 getVel3()
	{
		return vel3;
	}
	
	
	/**
	 * @return [m/s,m/s]
	 */
	public IVector2 getVel2()
	{
		return vel3.getXYVector();
	}
	
	
	/**
	 * @return [rad/s]
	 */
	public double getAngularVel()
	{
		return vel3.z();
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final State state = (State) o;
		
		return new EqualsBuilder()
				.append(pose, state.pose)
				.append(vel3, state.vel3)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(pose)
				.append(vel3)
				.toHashCode();
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pose", pose)
				.append("vel3", vel3)
				.toString();
	}
}
