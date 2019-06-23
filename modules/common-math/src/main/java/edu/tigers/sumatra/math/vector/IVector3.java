/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

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
	 * Project 'this' to ground (z==0) using a ray from origin.
	 * 
	 * @param origin
	 * @return 2D vector projected to ground.
	 * @note this.z() must be != origin.z()
	 */
	Vector2 projectToGroundNew(final IVector3 origin);
	
	
	/**
	 * @return a new deep copy
	 */
	IVector3 copy();
}
