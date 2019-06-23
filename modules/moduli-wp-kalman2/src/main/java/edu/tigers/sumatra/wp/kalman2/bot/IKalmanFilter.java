/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman2.bot;

import org.apache.commons.math3.linear.RealVector;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IKalmanFilter
{
	/**
	 * @param meas
	 * @param timestamp
	 */
	void correct(RealVector meas, long timestamp);
	
	
	/**
	 * @param timestamp
	 */
	void predict(long timestamp);
	
	
	/**
	 * @return the state
	 */
	RealVector getState();
	
}