/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


public interface IKeeperDestinationCalculator
{
	KeeperDestination calcDestination(WorldFrame worldFrame, ShapeMap shapeMap, ITrackedBot tBot, IVector2 posToCover);
}
