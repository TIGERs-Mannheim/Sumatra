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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.robotcontrolutility.model.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.interpreter.CTInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.interpreter.SysoutInterpreter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.interpreter.TigerInterpreter;


/**
 * This class handles the RCC for one bot
 * 
 * @author Gero
 */
public class RCReceiver implements Runnable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger						log					= Logger.getLogger(getClass());
	
	
	// Connection
	private final Thread						thread;
	private Socket								socket				= null;
	private ServerSocket						serverSocket		= null;
	private int									port;
	
	// Input
	private ObjectInputStream				inputStream			= null;
	private ActionCommand					clientCommand		= null;
	
	// Interpretation
	private ABot								bot					= null;
	private final ARCCommandInterpreter	interpreter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RCReceiver(int port, ABot bot)
	{
		this.port = port;
		this.bot = bot;
		
		this.thread = new Thread(this, "RCReceiver_" + bot + "|" + port);
		
		switch (bot.getType())
		{
			case CT:
				interpreter = new CTInterpreter(bot);
				break;
			
			case TIGER:
				interpreter = new TigerInterpreter(bot);
				break;
			
			case SYSOUT:
				interpreter = new SysoutInterpreter(bot);
				log.warn("Some tries to take control over a Sysout-Bot!");
				break;
			
			case UNKNOWN:
			default:
				interpreter = null;
				log.error("UNKNOWN bot!");
				break;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void run()
	{
		while (!thread.isInterrupted())
		{
			try
			{
				// --- start a new server and wait for a client ---
				if (serverSocket != null)
				{
					serverSocket.close();
					if (socket != null)
					{
						socket.close();
					}
					
					log.info("Old RCC disconnected at port '" + port + "'. Waiting for a new one...");
				}
				
				serverSocket = new ServerSocket(port);
				
				socket = serverSocket.accept();
				

				// --- init input-reader ---
				inputStream = new ObjectInputStream(socket.getInputStream());
				
				log.info("RCC connected at port '" + socket.getLocalPort() + "'.");
				

				// --- while client is connected to server -> do your work ---
				while ((clientCommand = (ActionCommand) inputStream.readObject()) != null)
				{
					interpreter.interpret(clientCommand);
				}
				log.info("RCC disconnected at port '" + socket.getLocalPort() + "'.");
				
			} catch (SocketException e)
			{
				Thread.currentThread().interrupt();
			} catch (IOException e)
			{
				log.error(e.getMessage());
				Thread.currentThread().interrupt();
			} catch (ClassNotFoundException e)
			{
				log.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			
		}
	}
	
	
	public void startReceiver()
	{
		thread.start();
	}
	

	public void close()
	{
		thread.interrupt();
		try
		{
			if (serverSocket != null)
			{
				serverSocket.close();
			}
			if (socket != null)
			{
				socket.close();
			}
		} catch (IOException e)
		{
			log.debug("Expected IOException was thrown.");
		}
	}
	
	
	@Override
	public String toString()
	{
		return "RCReceiver " + bot;
	}
}
