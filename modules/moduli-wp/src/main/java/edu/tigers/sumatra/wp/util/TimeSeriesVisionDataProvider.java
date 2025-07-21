/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Data provider for raw vision data
 */
public class TimeSeriesVisionDataProvider implements ITimeSeriesDataProvider, IWorldFrameObserver
{
	private static final Logger log = LogManager.getLogger(TimeSeriesVisionDataProvider.class.getName());

	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();


	/**
	 * Default constructor
	 */
	public TimeSeriesVisionDataProvider()
	{
		dataBuffers.put("frameInfo", new ConcurrentLinkedQueue<>());
		dataBuffers.put("rawBall", new ConcurrentLinkedQueue<>());
		dataBuffers.put("rawBalls", new ConcurrentLinkedQueue<>());
		dataBuffers.put("rawBots", new ConcurrentLinkedQueue<>());
	}


	@Override
	public void stop()
	{
		try
		{
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}


	@Override
	public void start()
	{
		try
		{
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("WP module not found.", err);
		}
	}


	@Override
	public boolean isDone()
	{
		return true;
	}


	@Override
	public Map<String, Collection<IExportable>> getExportableData()
	{
		return dataBuffers;
	}


	@Override
	public void onNewCamDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		processCamFrame(frame);
	}


	private void processCamFrame(final ExtendedCamDetectionFrame frame)
	{
		dataBuffers.get("frameInfo").add(new ExportableFrameInfo(
				frame.getGlobalFrameId(), frame.getCameraId(),
				frame.getTimestamp(), frame.getOriginalTimestamps().getTSent(), (long) (System.currentTimeMillis() * 1e6),
				frame.getCamFrameNumber()));
		dataBuffers.get("rawBall").add(frame.getBall());
		dataBuffers.get("rawBalls").addAll(frame.getBalls());
		dataBuffers.get("rawBots").addAll(frame.getRobots());
	}
}
