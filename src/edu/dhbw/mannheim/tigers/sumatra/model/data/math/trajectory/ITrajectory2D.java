/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;


/**
 * 2 dimensional trajectory.
 * 
 * @author AndreR
 */
public interface ITrajectory2D
{
	/**
	 * Get position at time t.
	 * 
	 * @param t time
	 * @return position [mm]
	 */
	Vector2 getPosition(float t);
	
	
	/**
	 * Get velocity at a certain time.
	 * 
	 * @param t time
	 * @return velocity
	 */
	Vector2 getVelocity(float t);
	
	
	/**
	 * Get acceleration at a certain time.
	 * 
	 * @param t time
	 * @return acceleration
	 */
	Vector2 getAcceleration(float t);
	
	
	/**
	 * Get total runtime.
	 * 
	 * @return total time for trajectory
	 */
	float getTotalTime();
}
