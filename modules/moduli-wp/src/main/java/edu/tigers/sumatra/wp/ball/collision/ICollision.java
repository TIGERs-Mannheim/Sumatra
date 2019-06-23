/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollision
{
	
	/**
	 * @return the pos
	 */
	IVector2 getPos();
	
	
	/**
	 * @return
	 */
	IVector2 getObjectVel();
	
	
	/**
	 * @return the normal
	 */
	IVector2 getNormal();
}
