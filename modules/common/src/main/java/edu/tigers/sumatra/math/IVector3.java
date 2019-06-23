/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.08.2012
 * Author(s): Gero, AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.function.Function;


/**
 * This interface allows transparent access to {@link Vector3} and {@link Vector3f}
 * 
 * @see Vector3
 * @see Vector3f
 * @see Vector3
 * @see Vector3f
 * @author Gero, AndreR
 */
public interface IVector3 extends IVector
{
	@Override
	default int getNumDimensions()
	{
		return 3;
	}
	
	
	@Override
	Vector3 addNew(IVector vector);
	
	
	@Override
	Vector3 subtractNew(IVector vector);
	
	
	@Override
	Vector3 multiplyNew(IVector vector);
	
	
	@Override
	Vector3 multiplyNew(double f);
	
	
	@Override
	Vector3 normalizeNew();
	
	
	@Override
	Vector3 absNew();
	
	
	@Override
	Vector3 applyNew(Function<Double, Double> function);
	
	
	/**
	 * Turn a new vector rotated around Z axis.
	 * 
	 * @param angle Angle in [rad]
	 * @return Rotated vector
	 */
	Vector3 turnAroundZNew(double angle);
}
