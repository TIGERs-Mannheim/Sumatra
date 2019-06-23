/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data.collector;

import java.util.Collection;
import java.util.Map;


/**
 * Interface for time series data providers
 */
public interface ITimeSeriesDataProvider
{
	/**
	 * @return a map of all exportable data buffers
	 */
	Map<String, Collection<IExportable>> getExportableData();
	
	
	/**
	 * Do your stuff when the collectors starts
	 */
	void start();
	
	
	/**
	 * Stop stuff when the collectors stops and before the export starts
	 */
	void stop();
	
	
	/**
	 * Report back to the collectors, if your provider has enough data. Return true, if you do not care
	 * 
	 * @return true, if collected enough data
	 */
	boolean isDone();
	
	
	/**
	 * Add metadata to the data set after collector stopped
	 * 
	 * @param jsonMapping
	 */
	default void onAddMetadata(final Map<String, Object> jsonMapping)
	{
	}
}
