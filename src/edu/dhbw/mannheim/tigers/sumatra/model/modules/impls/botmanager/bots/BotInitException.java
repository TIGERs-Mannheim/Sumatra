/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.03.2011
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import org.apache.commons.configuration.SubnodeConfiguration;

/**
 * Indicates an error while instantiating a bot
 * 
 * @author Gero
 */
public class BotInitException extends Exception
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -4495790203031090476L;


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public BotInitException()
	{
		super();
	}
	
	
	/**
	  * @param botConfig
	  * @param throwable
	  */
	public BotInitException(SubnodeConfiguration botConfig, Throwable throwable)
	{
		super("Bot: " + botConfig.getInt("[@id]", -1) + " | " + botConfig.getString("name", "[unknown]"), throwable);
	}
	
	
	/**
	  * @param botConfig
	  */
	public BotInitException(SubnodeConfiguration botConfig)
	{
		this(botConfig, null);
	}
	
	
	/**
	  * @param msg
	  */
	public BotInitException(String msg)
	{
		super(msg);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
