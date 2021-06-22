/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * This is a asynchronous data collector that writes multiple timeseries data streams into a common folder
 */
public class TimeSeriesDataCollector implements Runnable
{
	private static final Logger log = LogManager.getLogger(TimeSeriesDataCollector.class.getName());
	private final String baseFolder;

	private final List<ITimeSeriesDataCollectorObserver> observers = new CopyOnWriteArrayList<>();
	private final List<ITimeSeriesDataProvider> dataProviders = new ArrayList<>();
	private ScheduledExecutorService executorService;

	private long startTime;
	private long time2Stop = 0;
	private double timeout = 30;
	private boolean processing = false;

	private boolean stopAutomatically = true;


	private TimeSeriesDataCollector(String baseFolder)
	{
		Validate.notEmpty(baseFolder, "The file name must not be empty");
		this.baseFolder = baseFolder;
	}


	/**
	 * Create a collector with all known data providers
	 *
	 * @param baseFolder
	 * @return
	 */
	public static TimeSeriesDataCollector allProviders(final String baseFolder)
	{
		TimeSeriesDataCollector collector = noProviders(baseFolder);
		ServiceLoader<ITimeSeriesDataProvider> serviceLoader = ServiceLoader.load(ITimeSeriesDataProvider.class);
		serviceLoader.forEach(collector::addDataProvider);
		return collector;
	}


	/**
	 * Create a collector without any initial data providers. Add your desired ones yourself.
	 *
	 * @param baseFolder
	 * @return
	 */
	public static TimeSeriesDataCollector noProviders(final String baseFolder)
	{
		return new TimeSeriesDataCollector(baseFolder);
	}


	/**
	 * @param observer
	 */
	public final void addObserver(final ITimeSeriesDataCollectorObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public final void removeObserver(final ITimeSeriesDataCollectorObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * @param dataProvider a data provider
	 */
	public final void addDataProvider(ITimeSeriesDataProvider dataProvider)
	{
		dataProviders.add(dataProvider);
	}


	private void notifyPostProcessing(final String filename)
	{
		for (ITimeSeriesDataCollectorObserver observer : observers)
		{
			observer.postProcessing(filename);
		}
	}


	private void startExportData()
	{
		stop();
		if (!processing)
		{
			processing = true;
			new Thread(this, getClass().getSimpleName()).start();
		}
	}


	/**
	 * Stop collection of data before starting to export the data.<br>
	 * This must not be called outside the collector!
	 */
	protected void stop()
	{
		dataProviders.forEach(ITimeSeriesDataProvider::stop);
		executorService.shutdown();
		log.debug("Stopped");
	}


	/**
	 * Starting collecting data
	 *
	 * @return true if successfully started
	 */
	public boolean start()
	{
		startTime = System.nanoTime();
		dataProviders.forEach(ITimeSeriesDataProvider::start);
		executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(getClass().getSimpleName()));
		executorService.scheduleAtFixedRate(this::stopIfDone, 1000, 500, TimeUnit.MILLISECONDS);
		log.debug("Started.");
		return true;
	}


	@SuppressWarnings("squid:S1181") // Catch Throwable here intentionally
	private void stopIfDone()
	{
		try
		{
			if (isDone())
			{
				startExportData();
			}
		} catch (Throwable err)
		{
			log.error("An exception occurred in done check.", err);
			startExportData();
		}
	}


	protected boolean isDone()
	{
		if ((time2Stop != 0) && ((System.nanoTime() - time2Stop) > 0))
		{
			log.debug("requested timeout reached. Done.");
			return true;
		}
		if (((System.nanoTime() - startTime) / 1e9) > timeout)
		{
			log.debug("Ball watcher timed out");
			return true;
		}
		return stopAutomatically && dataProviders.stream().allMatch(ITimeSeriesDataProvider::isDone);
	}


	/**
	 * Stop after given delay
	 *
	 * @param milliseconds
	 */
	public final void stopDelayed(final long milliseconds)
	{
		time2Stop = System.nanoTime() + (milliseconds * (long) 1e6);
	}


	/**
	 * Stop export
	 */
	public final void stopExport()
	{
		stopDelayed(0);
	}


	@Override
	public final void run()
	{
		exportCsvFiles(baseFolder);
		String fullFileName = exportMetadata();
		notifyPostProcessing(fullFileName);
	}


	private String exportMetadata()
	{
		Map<String, Object> jsonMapping = new HashMap<>();
		jsonMapping.put("timestamp", System.currentTimeMillis());
		jsonMapping.put("description", "no description available");
		dataProviders.forEach(provider -> provider.onAddMetadata(jsonMapping));
		observers.forEach(provider -> provider.onAddMetadata(jsonMapping));

		JSONObject jsonObj = new JSONObject(jsonMapping);
		String fullFileName = baseFolder + "/info.json";
		try
		{
			Files.write(Paths.get(fullFileName), jsonObj.toJSONString().getBytes());
		} catch (IOException err)
		{
			log.error("Could not write file!", err);
		}
		return fullFileName;
	}


	private void exportCsvFiles(final String folder)
	{
		File dir = new File(folder);
		if (dir.exists())
		{
			log.error("Target folder already exists: {}", folder);
			return;
		}
		if (!dir.mkdirs())
		{
			log.error("Can not create target folder: {}", folder);
			return;
		}

		dataProviders.forEach(
				provider -> provider.getExportableData()
						.forEach(
								(name, dataBuffer) -> CSVExporter.exportCollection(folder, name, new ArrayList<>(dataBuffer))));
	}


	/**
	 * @param stopAutomatically the stopAutomatically to set
	 */
	public final void setStopAutomatically(final boolean stopAutomatically)
	{
		this.stopAutomatically = stopAutomatically;
	}


	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(final double timeout)
	{
		this.timeout = timeout;
	}
}
