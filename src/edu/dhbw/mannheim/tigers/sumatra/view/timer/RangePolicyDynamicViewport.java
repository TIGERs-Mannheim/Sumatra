/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 13, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import info.monitorenter.gui.chart.rangepolicies.ARangePolicy;
import info.monitorenter.util.Range;


/**
 * This {@link ARangePolicy} has an dynamic viewport
 * with a fixed range width, but incrementing range
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RangePolicyDynamicViewport extends ARangePolicy
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 8636803160639955055L;
	private final double			width;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param width
	 */
	public RangePolicyDynamicViewport(double width)
	{
		this.width = width;
		setRange(new Range(0, width));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public double getMax(double chartMin, double chartMax)
	{
		return Math.max(chartMax, width);
	}
	
	
	@Override
	public double getMin(double chartMin, double chartMax)
	{
		return Math.max(chartMax - width, 0.0);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
