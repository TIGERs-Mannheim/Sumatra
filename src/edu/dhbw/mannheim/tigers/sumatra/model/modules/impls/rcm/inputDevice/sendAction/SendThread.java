/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;


/**
 * Send cmds to a bot
 * 
 * @author Manuel
 * 
 */
class SendThread extends Thread
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger			log		= Logger.getLogger(SendThread.class.getName());
	private static final int				SLEEP		= 50;
	private final ARCCommandInterpreter	cmdInterpreter;
	private ActionCommand					cmd		= null;
	private boolean							running	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create new send thread to send cmds to bot
	 * 
	 * @param cmdInterpreter
	 */
	SendThread(ARCCommandInterpreter cmdInterpreter)
	{
		this.cmdInterpreter = cmdInterpreter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void run()
	{
		while (running)
		{
			if (cmd != null)
			{
				synchronized (cmdInterpreter)
				{
					cmdInterpreter.interpret(cmd);
				}
			}
			try
			{
				Thread.sleep(SLEEP);
			} catch (InterruptedException e)
			{
				log.error("SendThread interrupted!", e);
				break;
			}
		}
	}
	
	
	/**
	 * Let the thread begin
	 */
	void startRunning()
	{
		running = true;
		start();
	}
	
	
	/**
	 * Stop the thread and send bot a stop cmd
	 */
	void stopRunning()
	{
		running = false;
		cmd = null;
		synchronized (cmdInterpreter)
		{
			cmdInterpreter.stopAll();
		}
	}
	
	
	/**
	 * Set a new cmd to be executed
	 * 
	 * @param newCmd
	 */
	public void setNewCmd(ActionCommand newCmd)
	{
		cmd = newCmd;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
