/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.VisionFilterImpl;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;


/**
 * Data provider for raw vision data
 */
public class TimeSeriesVisionFilterDataProvider implements ITimeSeriesDataProvider, IVisionFilterObserver
{
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();


	/**
	 * Default constructor
	 */
	public TimeSeriesVisionFilterDataProvider()
	{
		dataBuffers.put("filteredBall", new ConcurrentLinkedQueue<>());
		dataBuffers.put("filteredBots", new ConcurrentLinkedQueue<>());
	}


	@Override
	public void stop()
	{
		SumatraModel.getInstance().getModuleOpt(VisionFilterImpl.class).ifPresent(v -> v.removeObserver(this));
	}


	@Override
	public void start()
	{
		SumatraModel.getInstance().getModuleOpt(VisionFilterImpl.class).ifPresent(v -> v.addObserver(this));
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
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		dataBuffers.get("filteredBall").add(filteredVisionFrame.getBall());
		dataBuffers.get("filteredBots").addAll(filteredVisionFrame.getBots());
	}
}
