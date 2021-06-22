/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.util.BotStateTrajectorySync;


/**
 * Post hook interface for skill executors
 */
public interface ISkillExecutorPostHook
{
	default void onSkillUpdated(final ABot bot, final long timestamp, final ShapeMap shapeMap)
	{
	}


	default void onRobotRemoved(final BotID botID)
	{
	}

	default void onTrajectoryUpdated(BotID botID, BotStateTrajectorySync sync)
	{
	}
}
