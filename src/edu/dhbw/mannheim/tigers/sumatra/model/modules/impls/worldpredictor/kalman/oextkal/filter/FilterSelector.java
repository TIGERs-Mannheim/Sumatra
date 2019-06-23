/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.01.2011
 * Author(s): Administrator
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.WPConfig;


/**
 */
public final class FilterSelector
{
	// Logger
	private static final Logger	log	= Logger.getLogger(FilterSelector.class.getName());
	
	
	private FilterSelector()
	{
		
	}
	
	
	private static IFilter getFilter(int i)
	{
		switch (i)
		{
			case 0:
				return new ExtKalmanFilter();
			case 1:
				return new ParticleFilter();
			default:
			{
				log.error("Wrong Filter Config, only 0 and 1 allowed, returning extKalFilter");
				return new ExtKalmanFilter();
			}
		}
	}
	
	
	/**
	 * @return
	 */
	public static IFilter getBallFilter()
	{
		return getFilter(WPConfig.BALL_MODULE);
	}
	
	
	/**
	 * @return
	 */
	public static IFilter getFoodFilter()
	{
		return getFilter(WPConfig.BLUE_MODULE);
	}
	
	
	/**
	 * @return
	 */
	public static IFilter getTigerFilter()
	{
		
		return getFilter(WPConfig.YELLOW_MODULE);
	}
	
}
