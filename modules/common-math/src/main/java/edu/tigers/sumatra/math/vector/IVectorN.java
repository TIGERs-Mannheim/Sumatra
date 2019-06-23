/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.function.Function;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IVectorN extends IVector
{
	@Override
	VectorN addNew(IVector vector);
	
	
	@Override
	VectorN subtractNew(IVector vector);
	
	
	@Override
	VectorN multiplyNew(IVector vector);
	
	
	@Override
	VectorN multiplyNew(double f);
	
	
	@Override
	VectorN normalizeNew();
	
	
	@Override
	VectorN absNew();
	
	
	@Override
	VectorN applyNew(Function<Double, Double> function);
}
