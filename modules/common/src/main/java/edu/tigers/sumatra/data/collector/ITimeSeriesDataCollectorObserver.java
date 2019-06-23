/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data.collector;

import java.util.Map;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITimeSeriesDataCollectorObserver
{
	/**
	 * This is called after the csv file was saved
	 * 
	 * @param fileName
	 */
	default void postProcessing(final String fileName)
	{
	}
	
	
	/**
	 * Add some metadata to the time series data
	 * 
	 * @param jsonMapping
	 */
	default void onAddMetadata(final Map<String, Object> jsonMapping)
	{
	}
}
