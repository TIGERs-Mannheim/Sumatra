/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.11.2011
 * Author(s): Sven Frank
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.network;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.log4j.Logger;


/**
 * - Creates server for android connection
 * 
 * @author Sven Frank
 * 
 */
public final class AndroidRCMServer implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(AndroidRCMServer.class.getName());
	
	private static final int		SERVERPORT	= 20010;
	
	private boolean					running		= false;
	
	// Connection
	private Thread						thread		= null;
	private ServerSocket				serverSocket;
	private ClientWorker				clientWorker;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * starts a new android server
	 */
	public AndroidRCMServer()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * closes the server socket and all running clientWorkers
	 */
	public synchronized void stopServer()
	{
		running = false;
		try
		{
			clientWorker.closeWorker();
			serverSocket.close();
			log.info("Connection closed for port " + SERVERPORT + ".");
		} catch (IOException e)
		{
			log.error("IOException on stop: " + e.getMessage());
		}
	}
	
	
	/**
	 * starts a new server socket to listen on new clientWorkers
	 */
	public synchronized void startServer()
	{
		if (!running)
		{
			running = true;
			thread = new Thread(this, "Android Server");
			thread.start();
			log.info("Android Server started...");
		}
	}
	
	
	/**
	 * listen on server socket on SERVERPORT for new input to create new clientWorkers
	 */
	@Override
	public void run()
	{
		try
		{
			// --- start a new server and wait for a client ---
			serverSocket = new ServerSocket(SERVERPORT);
		} catch (IOException e)
		{
			log.error("Could not open server socket");
			return;
		}
		
		log.info("Server connected at port '" + serverSocket.getLocalPort() + "'.");
		
		clientWorker = new ClientWorker();
		
		while (running)
		{
			try
			{
				clientWorker.addCommunicationSocket(new CommunicationSocket(serverSocket.accept()));
			} catch (IOException e)
			{
				log.info("Server Socket closed");
			}
		}
	}
	
	
	/**
	 * @return the running
	 */
	public boolean isRunning()
	{
		return running;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
