/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.wp.ball.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICollisionState extends IState
{
	/**
	 * @return
	 */
	Optional<ICollision> getCollision();
}
