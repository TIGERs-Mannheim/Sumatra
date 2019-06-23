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

import edu.dhbw.mannheim.tigers.robotcontrolutility.model.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


/**
 * This class encapsulates the types capable of interpreting the Command-String from RCC
 * 
 * @author Gero
 * 
 */
public abstract class ARCCommandInterpreter
{
	protected static final float	TWO_PI	= 2 * (float) Math.PI;
	

	protected final EBotType		type;
	
	
	public ARCCommandInterpreter(EBotType type)
	{
		this.type = type;
	}
	

	public abstract void interpret(ActionCommand command);
	

	public EBotType getType()
	{
		return type;
	}
}
