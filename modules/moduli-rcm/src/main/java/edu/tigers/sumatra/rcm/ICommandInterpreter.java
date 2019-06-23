/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.tigers.sumatra.botmanager.bots.ABot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICommandInterpreter
{
	
	/**
	 * @param command
	 */
	void interpret(BotActionCommand command);
	
	
	/**
	 * send a move, dribble and kick cmd to stop everything
	 */
	void stopAll();
	
	
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
	 * @return the paused
	 */
	boolean isPaused();
	
	
	/**
	 * @param paused the paused to set
	 */
	void setPaused(boolean paused);
	
	
	/**
	 * @return
	 */
	double getCompassThreshold();
	
}