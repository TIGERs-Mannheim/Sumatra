/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory;



/**
 * Trajectory base class to get velocity at any time
 * 
 * @author AndreR
 * 
 */
public interface ITrajectory1D
{
	/**
	 * Get position at time t.
	 * 
	 * @param t time
	 * @return position
	 */
	public float getPosition(float t);
	
	
	/**
	 * Get velocity at a certain time.
	 * 
	 * @param t time
	 * @return velocity
	 */
	public float getVelocity(float t);
	
	
	/**
	 * Get acceleration at a certain time.
	 * 
	 * @param t time
	 * @return acceleration
	 */
	public float getAcceleration(float t);
	
	
	/**
	 * Get total runtime.
	 * 
	 * @return total time for trajectory
	 */
	public float getTotalTime();
}
