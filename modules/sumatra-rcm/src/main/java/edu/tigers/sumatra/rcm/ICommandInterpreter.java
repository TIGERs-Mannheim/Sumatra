/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICommandInterpreter
{
	
	/**
	 * @param command
	 */
	ABotSkill interpret(BotActionCommand command);
	
	
	/**
	 * @return
	 */
	ABot getBot();
	
	
	/**
	 * @return the highSpeedMode
	 */
	boolean isHighSpeedMode();
	
	
	/**
	 * @param highSpeedMode the highSpeedMode to set
	 */
	void setHighSpeedMode(boolean highSpeedMode);


	/**
	 * @param paused the paused to set
	 */
	void setPaused(boolean paused);
	
	
	/**
	 * @return
	 */
	double getCompassThreshold();
	
}