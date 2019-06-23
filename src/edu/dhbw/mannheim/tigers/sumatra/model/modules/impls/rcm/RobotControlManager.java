/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ARobotControlManager;
import edu.moduli.exceptions.InitModuleException;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.exceptions.StartModuleException;


/**
 * 
 * This is a module within Sumatra which can directly handle user inputs for
 * robot controlling.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class RobotControlManager extends ARobotControlManager
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log					= Logger.getLogger(RobotControlManager.class.getName());
	
	private static final int		STAN_START_PORT	= 10010;
	
	private final int					startPort;
	
	private final SumatraModel		model;
	private ABotManager				botmanager			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param subnodeConfiguration
	 */
	public RobotControlManager(SubnodeConfiguration subnodeConfiguration)
	{
		model = SumatraModel.getInstance();
		startPort = Integer.valueOf(subnodeConfiguration.getInt("startPort", STAN_START_PORT));
		log.debug("rcm instatiated!");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void initModule() throws InitModuleException
	{
		try
		{
			botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
			botmanager.addObserver(this);
		} catch (final ModuleNotFoundException e)
		{
			log.error("Botmanager not found!");
			return;
		}
		
		log.debug("Initialized.");
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		log.debug("Started.");
	}
	
	
	@Override
	public void stopModule()
	{
		log.debug("Stopped.");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public int getStartPort()
	{
		return startPort;
	}
	
	
	@Override
	public void onBotAdded(ABot bot)
	{
	}
	
	
	@Override
	public void onBotRemoved(ABot bot)
	{
	}
	
	
	@Override
	public void onBotIdChanged(BotID oldId, BotID newId)
	{
	}
	
	
	/**
	 * gets a list of all active bots known by the botmanager
	 * @return list of all active bots known by the botmanager
	 */
	public Collection<ABot> getAllBots()
	{
		return Collections.unmodifiableCollection(botmanager.getAllBots().values());
	}
	
	
	/**
	 * @param botID - id of a special bot that should be returned
	 * @return - bot with the id botID
	 */
	public ABot getBotByBotID(int botID)
	{
		final Collection<ABot> bots = getAllBots();
		for (final ABot bot : bots)
		{
			if (botID == bot.getBotID().getNumber())
			{
				return bot;
			}
		}
		return null;
	}
	
	
	/**
	 * @param botID - id of a special bot that should be returned
	 * @return - bot with the id botID
	 */
	public ABot getBotByBotID(BotID botID)
	{
		final Collection<ABot> bots = getAllBots();
		for (final ABot bot : bots)
		{
			if (botID.equals(bot.getBotID()))
			{
				return bot;
			}
		}
		return null;
	}
	
	
	@Override
	public void onBotConnectionChanged(ABot bot)
	{
	}
}
