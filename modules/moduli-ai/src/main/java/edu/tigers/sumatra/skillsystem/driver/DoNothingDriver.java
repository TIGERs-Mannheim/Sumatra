/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 13, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DoNothingDriver extends ABaseDriver implements IKickPathDriver
{
	
	/**
	 * 
	 */
	public DoNothingDriver()
	{
		addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.DO_NOTHING;
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	@Override
	public boolean isEnableDribbler()
	{
		return false;
	}
	
	
	@Override
	public boolean armKicker()
	{
		return false;
	}
}
