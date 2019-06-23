/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2016
 * Author(s): AndreR <andre@ryll.cc>
 * *********************************************************
 */
package edu.tigers.sumatra.filter.iir;

import org.apache.commons.lang.Validate;


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
public class ExponentialMovingAverageFilter
{
	private double	alpha;
	private double	state;
	
	
	/**
	 * Create filter.
	 * 
	 * @param alpha
	 */
	public ExponentialMovingAverageFilter(final double alpha)
	{
		Validate.isTrue((alpha >= 0.0) && (alpha <= 1.0), "alpha must be in range 0.0 - 1.0");
		this.alpha = alpha;
	}
	
	
	/**
	 * Create filter.
	 * 
	 * @param alpha
	 * @param state
	 */
	public ExponentialMovingAverageFilter(final double alpha, final double state)
	{
		Validate.isTrue((alpha >= 0.0) && (alpha <= 1.0), "alpha must be in range 0.0 - 1.0");
		this.alpha = alpha;
		this.state = state;
	}
	
	
	/**
	 * Update filter with new measurement.
	 * 
	 * @param measurement
	 * @return
	 */
	public double update(final double measurement)
	{
		state = (alpha * state) + ((1.0 - alpha) * measurement);
		return state;
	}
	
	
	/**
	 * @return the alpha
	 */
	public double getAlpha()
	{
		return alpha;
	}
	
	
	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(final double alpha)
	{
		this.alpha = alpha;
	}
	
	
	/**
	 * @return the state
	 */
	public double getState()
	{
		return state;
	}
	
	
	/**
	 * @param state the state to set
	 */
	public void setState(final double state)
	{
		this.state = state;
	}
}
