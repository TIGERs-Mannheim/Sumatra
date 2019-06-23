/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import edu.tigers.sumatra.math.IVector3;
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
	 * @param dt TODO
	 */
	default void update(double dt)
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
	 * @param vel
	 */
	void setVel(final IVector3 vel);
	
	
	/**
	 * @param vector3
	 */
	void addVel(final IVector3 vector3);
	
	
	/**
	 * @param pos
	 */
	void setPos(final IVector3 pos);
	
	
	/**
	 * @return
	 */
	IVector3 getVel();
	
	
	/**
	 * @return
	 */
	IVector3 getPos();
}
