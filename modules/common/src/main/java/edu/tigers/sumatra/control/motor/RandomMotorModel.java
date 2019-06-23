/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RandomMotorModel extends AMotorModel
{
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 targetVel)
	{
		return new VectorN(4);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.RANDOM;
	}
}
