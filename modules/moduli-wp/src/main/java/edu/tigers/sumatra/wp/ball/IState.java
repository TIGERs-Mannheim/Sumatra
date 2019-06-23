/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IState
{
	
	/**
	 * @return the pos
	 */
	IVector3 getPos();
	
	
	/**
	 * @return the vel
	 */
	IVector3 getVel();
	
	
	/**
	 * @return the acc
	 */
	IVector3 getAcc();
	
	
	/**
	 * @return
	 */
	IVector3 getAccFromTorque();
	
}