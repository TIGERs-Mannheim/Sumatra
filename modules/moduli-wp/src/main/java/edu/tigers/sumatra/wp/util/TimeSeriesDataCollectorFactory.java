/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.io.File;

import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Factory for creating {@link TimeSeriesDataCollector}s
 */
public class TimeSeriesDataCollectorFactory
{
	public static final String DATA_DIR = "data/timeSeries/";
	
	
	private TimeSeriesDataCollectorFactory()
	{
	}
	
	
	private static void ensureBaseFolderExists()
	{
		File baseFolder = new File(DATA_DIR);
		// noinspection ResultOfMethodCallIgnored - we do not care about the change, just about the existence afterwards
		baseFolder.mkdirs();
		if (!baseFolder.exists())
		{
			throw new IllegalStateException("Could not create base folder: " + baseFolder);
		}
	}
	
	
	private static String getBaseFolder(String folderName)
	{
		ensureBaseFolderExists();
		String moduli = SumatraModel.getInstance().getCurrentModuliConfig().split("\\.")[0];
		return DATA_DIR + moduli + "/" + folderName;
	}
	
	
	/**
	 * @param folderName
	 * @return
	 */
	public static TimeSeriesDataCollector createFullCollector(String folderName)
	{
		return TimeSeriesDataCollector.allProviders(getBaseFolder(folderName));
	}
}
