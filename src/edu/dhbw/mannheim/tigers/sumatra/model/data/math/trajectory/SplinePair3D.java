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

import javax.persistence.Embeddable;


/**
 * Storage class for a position and rotation spline.
 * 
 * @author AndreR
 */
@Embeddable
public class SplinePair3D
{
	/** */
	private HermiteSplineTrajectory2D	position;
	/** */
	private HermiteSplineTrajectory1D	rotation;
	
	
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
	
}