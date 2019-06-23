/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.interpreter;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.robotcontrolutility.model.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SysoutBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.ARCCommandInterpreter;

/**
 * @see ARCCommandInterpreter
 * 
 * @author Gero
 * 
 */
public class SysoutInterpreter extends ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Logger log = Logger.getLogger(getClass());
	
	private final SysoutBot bot;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public SysoutInterpreter(ABot bot)
	{
		super(EBotType.SYSOUT);
		
		this.bot = (SysoutBot) bot;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void interpret(ActionCommand command)
	{
		log.debug("Someone tries to control a SysoutBot(" + bot + ")! How stupid...");
	}
}
