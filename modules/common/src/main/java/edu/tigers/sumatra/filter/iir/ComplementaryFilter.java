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
 * x(k+1) = a*(x(k)+z1(k)) + (1-a)*z2(k)
 * x = state
 * z1,z2 = measurements
 * a = weighting factor for state between 0.0 and 1.0
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ComplementaryFilter
{
	private double	alpha;
	private double	state;
	
	
	/**
	 * Create filter.
	 * 
	 * @param alpha
	 */
	public ComplementaryFilter(final double alpha)
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
	public ComplementaryFilter(final double alpha, final double state)
	{
		Validate.isTrue((alpha >= 0.0) && (alpha <= 1.0), "alpha must be in range 0.0 - 1.0");
		this.alpha = alpha;
		this.state = state;
	}
	
	
	/**
	 * Update filter with new measurement.
	 * 
	 * @param model
	 * @param measurement
	 * @return
	 */
	public double update(final double model, final double measurement)
	{
		state = (alpha * (state + model)) + ((1.0 - alpha) * measurement);
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
