/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2016
 * Author(s): AndreR <andre@ryll.cc>
 * *********************************************************
 */
package edu.tigers.sumatra.filter.iir;

import edu.tigers.sumatra.math.vector.IVector2;


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
		super(alpha, state);
	}
	
	
	/**
	 * Update filter with new measurement.
	 * 
	 * @param measurement
	 * @return
	 */
	public IVector2 update(final IVector2 measurement)
	{
		return super.update(measurement).getXYVector();
	}
	
	
	/**
	 * @return the state
	 */
	@Override
	public IVector2 getState()
	{
		return state.getXYVector();
	}
	
	
	/**
	 * @param state the state to set
	 */
	public void setState(final IVector2 state)
	{
		this.state = state;
	}
}
