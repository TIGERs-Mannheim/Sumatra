/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Map;

import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IBallWatcherObserver
{
	/**
	 * This is called before the exporter is closed
	 * 
	 * @param jsonMapping
	 */
	default void beforeExport(final Map<String, Object> jsonMapping)
	{
	}
	
	
	/**
	 * Add your custom data
	 * 
	 * @param container
	 * @param frame
	 */
	default void onAddCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
	{
	}
	
	
	/**
	 * This is called after the csv file was saved
	 * 
	 * @param fileName
	 */
	default void postProcessing(final String fileName)
	{
	}
}
