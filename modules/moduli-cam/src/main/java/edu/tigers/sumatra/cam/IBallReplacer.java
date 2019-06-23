/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.math.IVector3;


/**
 * Replace the ball
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBallReplacer
{
	/**
	 * @param pos
	 * @param vel
	 */
	void replaceBall(IVector3 pos, IVector3 vel);
}
