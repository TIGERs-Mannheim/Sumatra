/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;


/**
 * Default stub implementation of CommandInterpreters
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CommandInterpreterStub implements ICommandInterpreter
{
	private final ABot bot = new DummyBot();
	
	
	@Override
	public ABotSkill interpret(final BotActionCommand command)
	{
		return new BotSkillMotorsOff();
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
