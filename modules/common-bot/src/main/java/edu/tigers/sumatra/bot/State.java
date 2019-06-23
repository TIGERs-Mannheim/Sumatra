/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
	private final IVector3 vel3;
	
	
	protected State()
	{
		pose = Pose.from(Vector3.zero());
		vel3 = Vector3.zero();
	}
	
	
	protected State(final Pose pose, final IVector3 vel3)
	{
		this.pose = pose;
		this.vel3 = vel3;
	}
	
	
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
	
	
	public IVector2 getPos()
	{
		return pose.getPos();
	}
	
	
	public double getOrientation()
	{
		return pose.getOrientation();
	}
	
	
	public IVector3 getVel3()
	{
		return vel3;
	}
	
	
	public IVector2 getVel2()
	{
		return vel3.getXYVector();
	}
	
	
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
		return new ToStringBuilder(this)
				.append("pose", pose)
				.append("vel3", vel3)
				.toString();
	}
}
