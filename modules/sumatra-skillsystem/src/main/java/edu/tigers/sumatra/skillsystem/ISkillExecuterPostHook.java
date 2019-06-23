/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ISkillExecuterPostHook
{
	/**
	 * @param bot
	 * @param timestamp
	 * @param shapeMap
	 */
	default void onSkillUpdated(final ABot bot, final long timestamp, final ShapeMap shapeMap)
	{
	}
}
