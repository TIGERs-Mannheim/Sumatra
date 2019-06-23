/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollisionObject
{
	/**
	 * @return
	 */
	IVector2 getVel();
	
	
	/**
	 * @param prePos
	 * @param postPos
	 * @return
	 */
	Optional<ICollision> getCollision(IVector3 prePos, IVector3 postPos);
}
