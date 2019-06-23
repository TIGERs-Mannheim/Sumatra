/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 2, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISkillExecuterPostHook
{
	/**
	 * @param bot
	 * @param timestamp
	 */
	default void onCommandSent(final ABot bot, final long timestamp)
	{
	}
}
