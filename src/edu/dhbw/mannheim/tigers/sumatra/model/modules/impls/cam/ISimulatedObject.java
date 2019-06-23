/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;


/**
 * An object that can be simulated
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISimulatedObject
{
	/**
	 * Make a simulation step with given dt
	 * 
	 * @param dt
	 */
	void step(float dt);
	
	
	/**
	 * @param vel
	 */
	void setVel(final IVector2 vel);
	
	
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
