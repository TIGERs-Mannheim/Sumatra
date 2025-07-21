/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.VisionFilterImpl;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Data provider for raw vision data
 */
public class TimeSeriesVisionFilterDataProvider implements ITimeSeriesDataProvider
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
		SumatraModel.getInstance().getModuleOpt(VisionFilterImpl.class).ifPresent(
				v -> v.getFilteredVisionFrame().subscribe(getClass().getCanonicalName(), this::onNewFilteredVisionFrame)
		);
	}


	@Override
	public void start()
	{
		SumatraModel.getInstance().getModuleOpt(VisionFilterImpl.class)
				.ifPresent(v -> v.getFilteredVisionFrame().unsubscribe(getClass().getCanonicalName()));
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


	private void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		dataBuffers.get("filteredBall").add(filteredVisionFrame.getBall());
		dataBuffers.get("filteredBots").addAll(filteredVisionFrame.getBots());
	}
}
