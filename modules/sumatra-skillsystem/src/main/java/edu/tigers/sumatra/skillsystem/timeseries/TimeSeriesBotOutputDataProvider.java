/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.bots.ITigerBotObserver;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
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
public class TimeSeriesBotOutputDataProvider implements ITimeSeriesDataProvider, ITigerBotObserver
{
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();
	private final Collection<IExportable> botOutputs = new ConcurrentLinkedQueue<>();


	/**
	 * Default constructor
	 */
	public TimeSeriesBotOutputDataProvider()
	{
		dataBuffers.put("botOutput", botOutputs);
	}


	@Override
	public void start()
	{
		if (SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			SumatraModel.getInstance().getModule(TigersBotManager.class).addBotObserver(this);
		}
	}


	@Override
	public void stop()
	{
		if (SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			SumatraModel.getInstance().getModule(TigersBotManager.class).removeBotObserver(this);
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
	public void onIncomingBotCommand(final TigerBot bot, final ACommand command)
	{
		if (command.getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) command;
			long tReceive = (long) (System.currentTimeMillis() * 1e6);
			ExportableBotOutput output = new ExportableBotOutput(bot.getBotId().getNumber(), bot.getBotId().getTeamColor(),
					tReceive, feedback);
			botOutputs.add(output);
		}
	}
}
