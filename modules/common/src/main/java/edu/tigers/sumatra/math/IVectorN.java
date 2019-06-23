/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.math;

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
