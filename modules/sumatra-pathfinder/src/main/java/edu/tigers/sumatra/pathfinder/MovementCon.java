/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Conditions on path planning.
 */
@Data
public final class MovementCon implements IMovementCon
{
	private PathFinderPrioMap prioMap;

	private boolean penaltyAreaOurObstacle = true;
	private boolean penaltyAreaTheirObstacle = true;
	private boolean fieldBorderObstacle = true;
	private boolean ballObstacle = true;
	private boolean theirBotsObstacle = true;
	private boolean ourBotsObstacle = true;
	private boolean goalPostsObstacle = false;
	private boolean gameStateObstacle = true;

	private Set<BotID> ignoredBots = new HashSet<>();
	private List<IObstacle> customObstacles = Collections.emptyList();


	/**
	 * Update dynamic targets/positions
	 *
	 * @param bot that is associated with this moveCon
	 */
	public final void update(final ITrackedBot bot)
	{
		if (prioMap == null)
		{
			prioMap = PathFinderPrioMap.byBotId(bot.getTeamColor());
		}
	}


	public void setBotsObstacle(final boolean obstacle)
	{
		theirBotsObstacle = obstacle;
		ourBotsObstacle = obstacle;
	}


	public void setIgnoredBots(final Set<BotID> ignoredBots)
	{
		this.ignoredBots = Collections.unmodifiableSet(ignoredBots);
	}


	public void setCustomObstacles(final List<IObstacle> customObstacles)
	{
		this.customObstacles = Collections.unmodifiableList(customObstacles);
	}


	/**
	 * Set only physical objects as obstacles (no penArea, game state obstacles)
	 */
	public void physicalObstaclesOnly()
	{
		setFieldBorderObstacle(true);
		setPenaltyAreaTheirObstacle(false);
		setPenaltyAreaOurObstacle(false);
		setGoalPostsObstacle(true);
		setGameStateObstacle(false);
		setBotsObstacle(true);
		setBallObstacle(true);
	}
}
