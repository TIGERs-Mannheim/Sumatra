/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 16, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.csvexporter;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;


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
	void beforeExport(Map<String, Object> jsonMapping);
	
	
	/**
	 * Add your custom data
	 * 
	 * @param container
	 * @param frame
	 */
	void onAddCustomData(ExportDataContainer container, MergedCamDetectionFrame frame);
	
	
	/**
	 * This is called after the csv file was saved
	 * 
	 * @param fileName
	 */
	void postProcessing(final String fileName);
}
