/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.filter.iir;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * This is a very simple, yet effective, Infinite Impulse Response filter.
 * Discrete time transfer function:
 * x(k+1) = a*x(k) + (1-a)*z(k)
 * x = state
 * z = measurement
 * a = weighting factor for state between 0.0 and 1.0
 * This filter finds the long-term average value of noisy data.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ExponentialMovingAverageFilter2D extends ExponentialMovingAverageFilterVector
{
	/**
	 * Create filter.
	 * 
	 * @param alpha
	 */
	public ExponentialMovingAverageFilter2D(final double alpha)
	{
		super(alpha, 2);
	}
	
	
	/**
	 * Create filter.
	 * 
	 * @param alpha
	 * @param state
	 */
	public ExponentialMovingAverageFilter2D(final double alpha, final IVector2 state)
	{
		super(alpha, VectorN.copy(state));
	}
	
	
	/**
	 * Update filter with new measurement.
	 * 
	 * @param measurement
	 * @return
	 */
	public IVector2 update(final IVector2 measurement)
	{
		return super.update(VectorN.copy(measurement)).getXYVector();
	}
	
	
	/**
	 * @return the state
	 */
	@Override
	public IVectorN getState()
	{
		return state;
	}
	
	
	/**
	 * @param state the state to set
	 */
	public void setState(final IVector2 state)
	{
		this.state = VectorN.copy(state);
	}
}
