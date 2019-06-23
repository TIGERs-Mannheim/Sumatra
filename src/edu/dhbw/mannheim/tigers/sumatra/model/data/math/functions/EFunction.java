/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 10, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions;

import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * Implementations of instanceable functions
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EFunction implements IInstanceableEnum
{
	/**  */
	POLY_1D("poly", new InstanceableClass(Function1dPoly.class, new InstanceableParameter(float[].class, "", ""))),
	
	/**  */
	POLY_2D("poly2", new InstanceableClass(Function2dPoly.class, new InstanceableParameter(float[].class, "", ""))), ;
	
	private final String					id;
	private final InstanceableClass	instClass;
	
	
	private EFunction(final String id, final InstanceableClass instClass)
	{
		this.id = id;
		this.instClass = instClass;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return instClass;
	}
	
	
	/**
	 * @return the id
	 */
	public final String getId()
	{
		return id;
	}
}
