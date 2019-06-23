/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control.motor;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EMotorModel implements IInstanceableEnum
{
	/**  */
	MATRIX(new InstanceableClass(MatrixMotorModel.class)),
	/**  */
	COMBINATION(new InstanceableClass(CombinationMotorModel.class)),
	/**  */
	RANDOM(new InstanceableClass(RandomMotorModel.class)),
	/**  */
	GP_MATLAB(new InstanceableClass(GpMatlabMotorModel.class)),
	/**  */
	INTERPOLATION(new InstanceableClass(InterpolationMotorModel.class, new InstanceableParameter(String.class, "file",
			"gp2.interpol")));
	
	private final InstanceableClass	impl;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private EMotorModel(final InstanceableClass impl)
	{
		this.impl = impl;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}
}
