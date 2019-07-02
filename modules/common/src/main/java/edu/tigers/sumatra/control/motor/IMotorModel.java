/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IMotorModel
{
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param targetVel
	 * @return
	 */
	IVectorN getWheelSpeed(IVector3 targetVel);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param wheelSpeed
	 * @return
	 */
	IVector3 getXywSpeed(IVectorN wheelSpeed);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param motorNoise
	 */
	void setMotorNoise(final IVectorN motorNoise);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param xywNoise
	 */
	void setXywNoise(final IVector3 xywNoise);
	
	
	/**
	 * Update robot geometry used for motor velocity computation.
	 *
	 * @param frontAngleDeg Angle from robot X axis to front wheels
	 * @param backAngleDeg Angle from robot X axis to back wheels
	 * @param botRadius Bot radius in [m] (wheel center point)
	 * @param wheelRadius Wheel radius in [m]
	 */
	default void updateGeometry(final double frontAngleDeg, final double backAngleDeg, final double botRadius,
			final double wheelRadius)
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	EMotorModel getType();
}
