/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.finder;

import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import lombok.Value;


@Value
public class PathFinderCollision
{
	IObstacle obstacle;
	double firstCollisionTime;
}
