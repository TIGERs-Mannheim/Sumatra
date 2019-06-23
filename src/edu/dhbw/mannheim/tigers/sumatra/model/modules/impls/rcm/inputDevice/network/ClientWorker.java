/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.shared.AndroidMessage;


/**
 * 
 * - Sends/receives data to/from android device for robot controlling
 * 
 * @author Sven Frank
 * 
 */
public final class ClientWorker implements Runnable
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log						= Logger.getLogger(ClientWorker.class.getName());
	
	private static final int				SLEEP_TIME				= 50;
	
	private List<CommunicationSocket>	communicationSockets	= null;
	
	// Flag
	private boolean							running					= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * creates a new clientWorker on a specific socket
	 */
	public ClientWorker()
	{
		communicationSockets = new ArrayList<CommunicationSocket>();
		final Thread thread = new Thread(this, "ClientWorker");
		thread.start();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * first sends a list of all bots to an android machine
	 * then listens on a bot to run actionCommands on and starts listening on actionCommands for this bot
	 */
	@Override
	public void run()
	{
		while (running)
		{
			synchronized (communicationSockets)
			{
				
				for (final CommunicationSocket communicationSocket : communicationSockets)
				{
					try
					{
						interpretCommand(communicationSocket.getInputStream().readObject());
						
						communicationSocket.getOutputStream().writeObject(getAndroidMessage());
						communicationSocket.getOutputStream().flush();
					} catch (final IOException e)
					{
						communicationSocket.closeSocketConnection();
						communicationSockets.remove(communicationSocket);
						log.error("IOException: " + e.getMessage(), e);
					} catch (final ClassNotFoundException e)
					{
						communicationSocket.closeSocketConnection();
						communicationSockets.remove(communicationSocket);
						log.error("ClassNotFoundException: " + e.getMessage());
					}
				}
				
			}
			
			try
			{
				Thread.sleep(SLEEP_TIME);
			} catch (final InterruptedException e)
			{
				log.error("Interrupted Exception: " + e.getMessage());
			}
		}
	}
	
	
	/**
	 * stops getting data from the server and closes all connections
	 */
	public void closeWorker()
	{
		running = false;
		synchronized (communicationSockets)
		{
			for (final CommunicationSocket communicationSocket : communicationSockets)
			{
				communicationSocket.closeSocketConnection();
			}
			communicationSockets.clear();
		}
	}
	
	
	/**
	 * @param communicationSocket
	 */
	public void addCommunicationSocket(CommunicationSocket communicationSocket)
	{
		synchronized (communicationSockets)
		{
			communicationSockets.add(communicationSocket);
		}
	}
	
	
	private void interpretCommand(Object command)
	{
		if (command instanceof AndroidMessage)
		{
			final AndroidMessage message = (AndroidMessage) command;
			CommandInterpreter.getInstance().interpretAndroidMessage(message);
		} else
		{
			log.info("No cast to AndroidMessage possible...");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	private AndroidMessage getAndroidMessage()
	{
		return CommandInterpreter.getInstance().createAndroidMessage();
	}
}
