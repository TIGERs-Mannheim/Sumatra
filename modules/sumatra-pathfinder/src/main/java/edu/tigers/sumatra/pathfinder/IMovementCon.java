/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;

import java.util.List;
import java.util.Set;


/**
 * Read-only view to movementCon
 */
public interface IMovementCon
{
	PathFinderPrioMap getPrioMap();


	boolean isPenaltyAreaOurObstacle();


	boolean isPenaltyAreaTheirObstacle();


	boolean isBallObstacle();


	boolean isGoalPostsObstacle();


	boolean isTheirBotsObstacle();


	boolean isOurBotsObstacle();


	boolean isGameStateObstacle();


	Set<BotID> getIgnoredBots();


	List<IObstacle> getCustomObstacles();


	boolean isFieldBorderObstacle();


	Double getDistanceToBall();


	EObstacleAvoidanceMode getObstacleAvoidanceMode();
}
