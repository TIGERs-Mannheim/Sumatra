/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.basestation.BotCommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.model.SumatraModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Data provider for feedback from a (real) bot
 */
public class TimeSeriesBotOutputDataProvider implements ITimeSeriesDataProvider
{
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();
	private final Collection<IExportable> botOutputs = new ConcurrentLinkedQueue<>();


	public TimeSeriesBotOutputDataProvider()
	{
		dataBuffers.put("botOutput", botOutputs);
	}


	@Override
	public void start()
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.ifPresent(mgr ->
						mgr.getOnIncomingBotCommand().subscribe(getClass().getCanonicalName(), this::onIncomingBotCommand)
				);
	}


	@Override
	public void stop()
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.ifPresent(mgr -> mgr.getOnIncomingBotCommand().unsubscribe(getClass().getCanonicalName()));
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


	private void onIncomingBotCommand(BotCommand botCommand)
	{
		if (botCommand.command().getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) botCommand.command();
			long tReceive = (long) (System.currentTimeMillis() * 1e6);
			var output = new ExportableBotOutput(
					botCommand.botId().getNumber(), botCommand.botId().getTeamColor(), tReceive, feedback);
			botOutputs.add(output);
		}
	}
}
