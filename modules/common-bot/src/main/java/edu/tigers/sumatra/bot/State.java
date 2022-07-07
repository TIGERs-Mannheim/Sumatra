/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.filter.IInterpolatable;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Persistent
@Data
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class State implements IMirrorable<State>, IExportable, IInterpolatable<State>
{
	@NonNull
	private final Pose pose;

	/**
	 * [x,y,z] velocity in [m/s,m/s,rad/s]
	 */
	@NonNull
	private final IVector3 vel3;


	protected State()
	{
		pose = Pose.from(Vector3.zero());
		vel3 = Vector3.zero();
	}


	/**
	 * @param pose the pose
	 * @param vel  [m/s,m/s,rad/s]
	 * @return
	 */
	public static State of(final Pose pose, final IVector3 vel)
	{
		return new State(pose, vel);
	}


	/**
	 * @param pose the pose
	 * @return
	 */
	public static State of(final Pose pose)
	{
		return new State(pose, Vector3.zero());
	}


	public static State zero()
	{
		return State.of(Pose.zero(), Vector3f.zero());
	}


	public static State nan()
	{
		return State.of(Pose.nan(), Vector3f.nan());
	}


	@Override
	public State mirrored()
	{
		return State.of(pose.mirrored(), Vector3.from2d(vel3.getXYVector().multiplyNew(-1), vel3.z()));
	}


	@Override
	public State interpolate(final State state, double percentage)
	{
		IVector3 velDiff = state.vel3.subtractNew(vel3).multiply(percentage);
		IVector3 intpVel = vel3.addNew(velDiff);
		return State.of(pose.interpolate(state.pose, percentage), intpVel);
	}


	/**
	 * @return [mm, mm]
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
	public List<String> getHeaders()
	{
		return Arrays.asList("pos_x", "pos_y", "pos_w", "vel_x", "vel_y", "vel_w");
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> list = new ArrayList<>(6);
		list.addAll(pose.getPos().getNumberList());
		list.add(pose.getOrientation());
		list.addAll(vel3.getNumberList());
		return list;
	}
}
