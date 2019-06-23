/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ARobotControlServer;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * This class receives strings from the RCC, interprets them and sends commands directly to the {@link ABotManager}!
 * 
 * @author Gero
 * 
 */
public class RobotControlServer extends ARobotControlServer implements IBotManagerObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int					STAN_START_PORT	= 10010;
	

	private final Logger							log					= Logger.getLogger(getClass());
	private final SumatraModel					model;
	
	private final Map<Integer, RCReceiver>	receivers			= new HashMap<Integer, RCReceiver>();
	private final Object							recSync				= new Object();
	private ABotManager							botmanager			= null;
	private final int								startPort;
	private int										count					= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RobotControlServer(SubnodeConfiguration subnodeConfiguration)
	{
		model = SumatraModel.getInstance();
		startPort = Integer.valueOf(subnodeConfiguration.getInt("startPort", STAN_START_PORT));
	}
	

	// --------------------------------------------------------------------------
	// --- lifecycle ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initModule()
	{
		try
		{
			botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
			botmanager.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Botmanager not found!");
			return;
		}
		
		log.info("Initialized.");
	}
	

	@Override
	public void startModule()
	{
		log.info("Started.");
	}
	

	@Override
	public void stopModule()
	{
		if (botmanager != null)
		{
			botmanager.removeObserver(this);
		}
		
		log.info("Stopped.");
	}
	

	@Override
	public void deinitModule()
	{
		count = 0;
		
		// --- close threads ---
		for (RCReceiver rec : receivers.values())
		{
			rec.close();
		}
		
		receivers.clear();

		botmanager = null;

		log.info("Deinitialized.");
	}
	

	private void addBotReceiver(ABot bot)
	{
		RCReceiver receiver = new RCReceiver(startPort + count, bot);
		receiver.startReceiver();
		synchronized (recSync)
		{
			receivers.put(bot.getBotId(), receiver);
		}
		
		log.info("Create a new server at port: " + (startPort + count) + " for robot '" + bot.getName() + "'");
		count++;
	}
	

	@Override
	public void onBotAdded(ABot bot)
	{
		addBotReceiver(bot);
	}
	

	@Override
	public void onBotRemoved(ABot oddBot)
	{
		RCReceiver oddReceiver = null;
		synchronized (recSync)
		{
			oddReceiver = receivers.remove(oddBot.getBotId());
		}
		
		if (oddReceiver != null)
		{
			oddReceiver.close();
		}
	}
	

	@Override
	public void onBotIdChanged(int oldId, int newId)
	{
		// Simply exchange entry in the "receivers"-map as it should not be relevant for the RCReceiver itself
		synchronized (recSync)
		{
			RCReceiver receiver = receivers.remove(oldId);
			
			if (receiver != null)
			{
				receivers.put(newId, receiver);
			}
		}
	}
}
