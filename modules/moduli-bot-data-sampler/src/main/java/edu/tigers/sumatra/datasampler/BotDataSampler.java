/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.datasampler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Capture data using the acquisition mode
 *
 * @author nicolai.ommer
 */
public class BotDataSampler extends AModule
{
	private static final Logger		log			= Logger.getLogger(BotDataSampler.class.getName());
	
	/** */
	public static final String			MODULE_TYPE	= "BotDataSampler";
	/** */
	public static final String			MODULE_ID	= "BotDataSampler";
	
	
	private Map<BotID, BotWatcher>	botWatchers	= new ConcurrentHashMap<>();
	
	
	/**
	 * @param subnodeConfiguration
	 */
	public BotDataSampler(final SubnodeConfiguration subnodeConfiguration)
	{
		
	}
	
	
	/**
	 * Start capturing data, stopping any running process.
	 * acq mode is automatically set by watcher
	 * 
	 * @param botId
	 * @param mode
	 */
	public void startCapture(BotID botId, EDataAcquisitionMode mode)
	{
		stopCapture(botId);
		ABot bot = getBot(botId);
		if (bot == null)
		{
			return;
		}
		bot.getMatchCtrl().setDataAcquisitionMode(mode);
		BotWatcher watcher = new BotWatcher(bot, mode);
		botWatchers.put(botId, watcher);
		watcher.start();
	}
	
	
	/**
	 * Stop capturing data, if capturing is active
	 * and set acq mode to NONE
	 *
	 * @param botID to stop capture for
	 */
	public void stopCapture(BotID botID)
	{
		BotWatcher watcher = botWatchers.remove(botID);
		if (watcher != null)
		{
			watcher.stop();
		}
	}
	
	
	private ABot getBot(BotID botID)
	{
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			return botManager.getBotTable().get(botID);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find botmanager module.", e);
			return null;
		}
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// nothing to do
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		// nothing to do
	}
	
	
	@Override
	public void stopModule()
	{
		botWatchers.forEach((id, bw) -> bw.stop());
		botWatchers.clear();
	}
}
