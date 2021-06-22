/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ISkillExecutorPostHook;
import edu.tigers.sumatra.wp.util.BotStateTrajectorySync;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Data provider for skill data
 */
@Log4j2
public class TimeSeriesBotSkillDataProvider implements ITimeSeriesDataProvider, ISkillExecutorPostHook
{
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();
	private final Collection<IExportable> botSkillData = new ConcurrentLinkedQueue<>();


	/**
	 * Default constructor
	 */
	public TimeSeriesBotSkillDataProvider()
	{
		dataBuffers.put("botSkillData", botSkillData);
	}


	@Override
	public void start()
	{
		SumatraModel.getInstance().getModule(ASkillSystem.class).addSkillExecutorPostHook(this);
	}


	@Override
	public void stop()
	{
		SumatraModel.getInstance().getModule(ASkillSystem.class).removeSkillExecutorPostHook(this);
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
	public void onTrajectoryUpdated(BotID botID, BotStateTrajectorySync sync)
	{
		var skillDataBuilder = ExportableBotSkillData.builder()
				.botID(botID)
				.trackingQuality(sync.getTrajTrackingQuality());
		sync.getLatestState().ifPresent(skillDataBuilder::bufferedTrajState);
		sync.getLatestTimestamp().ifPresent(skillDataBuilder::timestamp);
		botSkillData.add(skillDataBuilder.build());
	}
}
