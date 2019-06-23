/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.ball.IState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IImpulseObject
{
	/**
	 * @param pos
	 * @return
	 */
	IVector3 getImpulse(IVector3 pos);
	
	
	/**
	 * @param state
	 * @return
	 */
	IVector3 getTorqueAcc(IState state);
}
