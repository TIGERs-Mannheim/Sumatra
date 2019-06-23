/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;


/**
 * This class opens SendingThreads that connect to Sumatra.
 * 
 * @author Lukas
 * 
 */

public class ActionSender
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private SendThread	sendingThread	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ActionSender()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param cmdInterpreter
	 */
	public void startSending(ARCCommandInterpreter cmdInterpreter)
	{
		sendingThread = new SendThread(cmdInterpreter);
		sendingThread.setName("SendingLocalThread on bot: " + cmdInterpreter.getBot().getBotID().getNumber() + ", Name: "
				+ cmdInterpreter.getBot().getName());
		sendingThread.startRunning();
	}
	
	
	/**
	 */
	public void stopSending()
	{
		sendingThread.stopRunning();
	}
	
	
	/**
	 * @param newCmd
	 */
	public void setNewCmd(ActionCommand newCmd)
	{
		sendingThread.setNewCmd(newCmd);
	}
}
