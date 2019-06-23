/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Data provider for feedback from a (real) bot
 */
public class TimeSeriesBotOutputDataProvider implements ITimeSeriesDataProvider, IBotManagerObserver
{
	private static final Logger log = Logger.getLogger(TimeSeriesBotOutputDataProvider.class.getName());
	
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
	public void stop()
	{
		try
		{
			ABotManager botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			botManager.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("botManager module not found.", err);
		}
	}
	
	
	@Override
	public void start()
	{
		try
		{
			ABotManager botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			botManager.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("botManager module not found.", err);
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
	public void onIncomingBotCommand(final ABot bot, final ACommand command)
	{
		if (command.getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) command;
			long tReceive = System.nanoTime();
			ExportableBotOutput output = new ExportableBotOutput(bot.getBotId().getNumber(), bot.getBotId().getTeamColor(),
					tReceive, feedback);
			botOutputs.add(output);
		}
	}
}
