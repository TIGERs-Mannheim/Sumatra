/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sampler.velocity;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVelocitySampler implements IInstanceableEnum
{
	/**  */
	RANDOM(new InstanceableClass(VelocityXywRandomSampler.class)),
	/**  */
	CONTINOUS(new InstanceableClass(VelocityXywContinousSampler.class)),
	/**  */
	STATIC(new InstanceableClass(VelocityXywStaticSampler.class));
	
	private final InstanceableClass	impl;
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private EVelocitySampler(final InstanceableClass impl)
	{
		this.impl = impl;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}
}
