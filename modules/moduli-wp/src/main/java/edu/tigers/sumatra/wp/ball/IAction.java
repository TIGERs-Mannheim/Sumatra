/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IAction
{
	/**
	 * @return
	 */
	IVector3 getAccTorque();
}
