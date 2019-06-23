/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 12, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LimitedVelocityCommand extends ACommand
{
	private final float	maxVelocity;
	
	
	/**
	 * 
	 */
	public LimitedVelocityCommand()
	{
		this(-1);
	}
	
	
	/**
	 * @param maxVelocity
	 */
	public LimitedVelocityCommand(final float maxVelocity)
	{
		super(ECommand.CMD_SYSTEM_LIMITED_VEL);
		this.maxVelocity = maxVelocity;
	}
	
	
	/**
	 * @return the maxVelocity
	 */
	public final float getMaxVelocity()
	{
		return maxVelocity;
	}
}
