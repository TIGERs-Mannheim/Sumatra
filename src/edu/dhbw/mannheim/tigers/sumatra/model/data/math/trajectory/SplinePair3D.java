/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Storage class for a position and rotation spline.
 * 
 * @author AndreR
 */
@Persistent(version = 2)
public class SplinePair3D implements ISpline, ITrajectory2D
{
	private static final Random			rnd				= new Random(SumatraClock.nanoTime());
	private static final int				MAX_RANDOM_ID	= 999999999;
	private final int							randomId;
	/** */
	private HermiteSplineTrajectory2D	position;
	/** */
	private HermiteSplineTrajectory1D	rotation;
	
	private long								startTime		= SumatraClock.nanoTime();
	
	
	/**
	  * 
	  */
	public SplinePair3D()
	{
		randomId = rnd.nextInt(MAX_RANDOM_ID);
	}
	
	
	/**
	 * @param traj
	 */
	public SplinePair3D(final SplinePair3D traj)
	{
		this();
		position = new HermiteSplineTrajectory2D(traj.position);
		rotation = new HermiteSplineTrajectory1D(traj.rotation);
		startTime = traj.startTime;
	}
	
	
	/**
	 */
	public void mirror()
	{
		position.mirror();
		rotation.mirror();
	}
	
	
	/**
	 * @return the position
	 */
	public HermiteSplineTrajectory2D getPositionTrajectory()
	{
		return position;
	}
	
	
	/**
	 * @param position the position to set
	 */
	public void setPositionTrajectory(final HermiteSplineTrajectory2D position)
	{
		this.position = position;
	}
	
	
	/**
	 * @return the rotation
	 */
	public HermiteSplineTrajectory1D getRotationTrajectory()
	{
		return rotation;
	}
	
	
	/**
	 * @param rotation the rotation to set
	 */
	public void setRotationTrajectory(final HermiteSplineTrajectory1D rotation)
	{
		this.rotation = rotation;
	}
	
	
	/**
	 * Append spline pair to <b>this</b> pair.<br>
	 * The function does not check the appended trajectories for any jumps in position, velocity or acceleration
	 * profiles. It is the responsibility of the user to make sure that the trajectories is still smooth after appending.
	 * 
	 * @param pair Trajectory to append.
	 */
	public void append(final SplinePair3D pair)
	{
		position.append(pair.position);
		rotation.append(pair.rotation);
	}
	
	
	/**
	 * @return the randomId
	 */
	public final int getRandomId()
	{
		return randomId;
	}
	
	
	/**
	 * @return the startTime [ns]
	 */
	public final long getStartTime()
	{
		return startTime;
	}
	
	
	/**
	 * @return [s]
	 */
	public final float getTrajectoryTime()
	{
		return ((SumatraClock.nanoTime() - startTime) / (1e9f));
	}
	
	
	@Override
	public float getCurrentTime()
	{
		return getTrajectoryTime();
	}
	
	
	/**
	 * @param startTime [ns]
	 */
	public final void setStartTime(final long startTime)
	{
		this.startTime = startTime;
	}
	
	
	/**
	 * Get the maximum total time from both position and rotation splines
	 * 
	 * @return [s]
	 */
	@Override
	public final float getTotalTime()
	{
		return Math.max(position.getTotalTime(), rotation.getTotalTime());
	}
	
	
	@Override
	public IVector3 getPositionByTime(final float t)
	{
		IVector2 pos = DistanceUnit.METERS.toMillimeters(getPositionTrajectory().getPosition(t));
		float orientation = getRotationTrajectory().getPosition(t);
		return new Vector3(pos, orientation);
	}
	
	
	@Override
	public IVector3 getVelocityByTime(final float t)
	{
		IVector2 pos = getPositionTrajectory().getVelocity(t);
		float orientation = getRotationTrajectory().getVelocity(t);
		return new Vector3(pos, orientation);
	}
	
	
	@Override
	public IVector3 getAccelerationByTime(final float t)
	{
		IVector2 pos = getPositionTrajectory().getAcceleration(t);
		float orientation = getRotationTrajectory().getAcceleration(t);
		return new Vector3(pos, orientation);
	}
	
	
	@Override
	public Vector2 getPosition(final float t)
	{
		return getPositionByTime(t).getXYVector();
	}
	
	
	@Override
	public Vector2 getVelocity(final float t)
	{
		return getVelocityByTime(t).getXYVector();
	}
	
	
	@Override
	public Vector2 getAcceleration(final float t)
	{
		return getAccelerationByTime(t).getXYVector();
	}
}