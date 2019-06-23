/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
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
	/**
	 * @return The Z part of the vector
	 */
	double z();
	
	
	@Override
	default int getNumDimensions()
	{
		return 3;
	}
	
	
	/**
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector3 addNew(IVector3 vector);
	
	
	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector3 subtractNew(IVector3 vector);
	
	
	/**
	 * Multiplies each element of the given 'vector' with the corresponding element of 'this' and returns the result in a
	 * new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	Vector3 multiplyNew(IVector3 vector);
	
	
	@Override
	Vector3 multiplyNew(double f);
	
	
	@Override
	Vector3 normalizeNew();
	
	
	@Override
	Vector3 absNew();
	
	
	@Override
	Vector3 applyNew(Function<Double, Double> function);
	
	
	/**
	 * Dot product.
	 * 
	 * @param vector
	 * @return
	 */
	double dotNew(IVector3 vector);
	
	
	/**
	 * Cross product.
	 * 
	 * @param vector
	 * @return
	 */
	IVector3 crossNew(final IVector3 vector);
	
	
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
