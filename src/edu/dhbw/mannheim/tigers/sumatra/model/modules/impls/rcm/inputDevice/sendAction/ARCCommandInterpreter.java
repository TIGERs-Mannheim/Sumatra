/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;


/**
 * Interpreter class for RDM Commands skeleton.
 * 
 * @author Manuel
 * 
 */
public abstract class ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected static final float	TWO_PI	= 2 * (float) Math.PI;
	
	protected final EBotType		type;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param type
	 */
	public ARCCommandInterpreter(EBotType type)
	{
		this.type = type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param command
	 */
	public abstract void interpret(ActionCommand command);
	
	
	/**
	 *
	 */
	public abstract void stopAll();
	
	
	/**
	 * 
	 * @return
	 */
	public EBotType getType()
	{
		return type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public abstract ABot getBot();
}
