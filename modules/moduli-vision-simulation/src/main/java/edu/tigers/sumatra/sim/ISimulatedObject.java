/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * An object that can be simulated
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISimulatedObject
{
	/**
	 * Update object. This is called less frequently then step.
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param dt
	 * @param timestamp
	 */
	default void update(double dt, long timestamp)
	{
	}
	
	
	/**
	 * Make a simulation step with given dt
	 * 
	 * @param dt
	 * @param context
	 */
	void step(double dt, MotionContext context);
	
	/**
	 * @param vector3
	 */
	void addVel(final IVector3 vector3);
	
	
	/**
	 * @return
	 */
	IVector3 getVel();
	
	
	/**
	 * @param vel
	 */
	void setVel(final IVector3 vel);
	
	/**
	 * @return
	 */
	IVector3 getPos();
	
	
	/**
	 * @param pos
	 */
	void setPos(final IVector3 pos);
}
