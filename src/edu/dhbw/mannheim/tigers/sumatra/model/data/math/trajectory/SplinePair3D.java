/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import java.util.Random;

import com.sleepycat.persist.model.Persistent;


/**
 * Storage class for a position and rotation spline.
 * 
 * @author AndreR
 */
@Persistent(version = 2)
public class SplinePair3D
{
	private static final Random			rnd				= new Random(System.nanoTime());
	private static final int				MAX_RANDOM_ID	= 999999999;
	private final int							randomId;
	/** */
	private HermiteSplineTrajectory2D	position;
	/** */
	private HermiteSplineTrajectory1D	rotation;
	
	private long								startTime		= 0;
	
	
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
	public SplinePair3D(SplinePair3D traj)
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
	public void setPositionTrajectory(HermiteSplineTrajectory2D position)
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
	public void setRotationTrajectory(HermiteSplineTrajectory1D rotation)
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
	public void append(SplinePair3D pair)
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
	 * 
	 * @return [s]
	 */
	public final float getTrajectoryTime()
	{
		return ((System.nanoTime() - startTime) / (1e9f));
	}
	
	
	/**
	 * @param startTime [ns]
	 */
	public final void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}
	
	
	/**
	 * Get the maximum total time from both position and rotation splines
	 * 
	 * @return [s]
	 */
	public final float getTotalTime()
	{
		return Math.max(position.getTotalTime(), rotation.getTotalTime());
	}
	
}