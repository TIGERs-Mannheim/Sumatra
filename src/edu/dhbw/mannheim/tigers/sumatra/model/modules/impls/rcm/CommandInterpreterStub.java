/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;


/**
 * Default stub implementation of CommandInterpreters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CommandInterpreterStub implements ICommandInterpreter
{
	
	@Override
	public void interpret(final BotActionCommand command)
	{
	}
	
	
	@Override
	public void stopAll()
	{
	}
	
	
	@Override
	public ABot getBot()
	{
		return new DummyBot();
	}
	
	
	@Override
	public boolean isHighSpeedMode()
	{
		return false;
	}
	
	
	@Override
	public void setHighSpeedMode(final boolean highSpeedMode)
	{
	}
	
	
	@Override
	public boolean isPaused()
	{
		return false;
	}
	
	
	@Override
	public void setPaused(final boolean paused)
	{
	}
	
	
	@Override
	public float getCompassThreshold()
	{
		return 0.4f;
	}
}
