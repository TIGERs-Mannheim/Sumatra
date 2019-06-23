/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 12, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ml.model.motor;

import edu.tigers.sumatra.math.IVector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IMotorSampler
{
	/**
	 * This function takes a sample for the optimizer.
	 * 
	 * @param motors Wheel velocities.
	 * @return Actual movement in bot-local reference frame.
	 */
	IVector3 takeSample(final double[] motors);
}
