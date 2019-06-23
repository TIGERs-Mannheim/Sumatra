/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import java.util.Optional;

import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


public interface IBotProviderBot
{
	
	RobotInfo getRobotInfo();
	
	
	Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory();
	
	
	ETeamColor getColor();
	
	
	ERobotMode getRobotMode();
	
	
	BotID getBotId();
}
