/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.AInputDevice;


/**
 * 
 * @author Manuel
 * 
 */
public class NetworkDevice extends AInputDevice implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log				= Logger.getLogger(NetworkDevice.class.getName());
	
	private final Socket				client;
	
	// Input
	private ObjectInputStream		inputStream		= null;
	private ActionCommand			clientCommand	= null;
	
	// Flag
	private boolean					running			= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * creates a networkDevice for a specific bot and socket
	 * @param bot - the bot, the actionCommands are for
	 * @param client - socket listens on actionCommands from an android machine
	 */
	public NetworkDevice(ABot bot, Socket client)
	{
		super(bot);
		this.client = client;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * sets the currentConfig for an actionCommand
	 */
	@Override
	public void setCurrentConfig(Map<String, String> currentConfig)
	{
		
	}
	
	
	/**
	 * listens on the client socket to get actionCommands from an android machine
	 * and to execute them
	 */
	@Override
	public void run()
	{
		while (running)
		{
			try
			{
				// --- init input-reader ---
				inputStream = new ObjectInputStream(client.getInputStream());
				
				while ((running) && ((clientCommand = (ActionCommand) inputStream.readObject()) != null))
				{
					execute(clientCommand);
				}
			} catch (IOException e)
			{
				stopSending();
				log.error("IOException: " + e.getMessage());
			} catch (ClassNotFoundException e)
			{
				stopSending();
				log.error("ClassNotFoundException: " + e.getMessage());
			}
		}
	}
	
	
	/**
	 * starts listening on the client socket to get actionCommands from an android machine
	 */
	public void startSending()
	{
		running = true;
		Thread t = new Thread(this, "AndroidNwDevice");
		t.start();
	}
	
	
	/**
	 * stops listening on the client socket an closes all connections
	 */
	@Override
	public void stopSending()
	{
		running = false;
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
			} catch (IOException e)
			{
				log.error("IOException: " + e.getMessage());
			}
		}
		if (client != null)
		{
			try
			{
				client.close();
			} catch (IOException e)
			{
				log.error("IOException: " + e.getMessage());
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
