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

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RandomMotorModel extends AMotorModel
{
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 targetVel)
	{
		return VectorN.zero(4);
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
