/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVectorN extends IVector
{
	/**
	 * Adds 'this' and 'vector' and returns the result in a new object, while 'this' and 'vector' stay unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	VectorN addNew(IVectorN vector);
	
	
	/**
	 * Subtracts the given 'vector' from 'this' and returns the result in a new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	VectorN subtractNew(IVectorN vector);
	
	
	/**
	 * Multiplies each element of the given 'vector' with the corresponding element of 'this' and returns the result in a
	 * new object, while 'this' and 'vector' stay
	 * unaffected.
	 *
	 * @param vector
	 * @return The result as a new object
	 */
	VectorN multiplyNew(IVectorN vector);
	
	
	@Override
	VectorN multiplyNew(double f);
	
	
	@Override
	VectorN normalizeNew();
	
	
	@Override
	VectorN absNew();
	
	
	@Override
	VectorN applyNew(Function<Double, Double> function);
}
