/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;


/**
 * Default stub implementation of CommandInterpreters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CommandInterpreterStub implements ICommandInterpreter
{
	private final ABot bot = new DummyBot();
	
	
	@Override
	public void interpret(final BotActionCommand command)
	{
		// this is a stub only
	}
	

	
	
	@Override
	public ABot getBot()
	{
		return bot;
	}
	
	
	@Override
	public boolean isHighSpeedMode()
	{
		return false;
	}
	
	
	@Override
	public void setHighSpeedMode(final boolean highSpeedMode)
	{
		// ignore this
	}
	
	
	@Override
	public boolean isPaused()
	{
		return false;
	}
	
	
	@Override
	public void setPaused(final boolean paused)
	{
		// ignore this
	}
	
	
	@Override
	public double getCompassThreshold()
	{
		return 0.4;
	}
}
