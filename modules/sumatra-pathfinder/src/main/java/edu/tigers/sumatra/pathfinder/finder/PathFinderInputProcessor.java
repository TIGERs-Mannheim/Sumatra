/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.finder;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputStatic;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PathFinderInputProcessor
{
	private static final boolean INCLUDED = true;
	private static final boolean EXCLUDED = false;


	public PathFinderInput processInput(PathFinderInput input)
	{
		return input.toBuilder()
				.dest(adaptedDestination(input))
				.obstacles(filterObstacles(input))
				.build();
	}


	private List<IObstacle> filterObstacles(PathFinderInput input)
	{
		CollisionInput collisionInput = new CollisionInputStatic(input.getPos(), input.getVel(), Vector2f.ZERO_VECTOR, 0);
		Map<Boolean, List<IObstacle>> obstacleMap = input.getObstacles().stream()
				.collect(Collectors.partitioningBy(o -> o.distanceTo(collisionInput) > 0));
		obstacleMap.get(EXCLUDED).forEach(o -> o.setColor(Color.red));
		obstacleMap.get(INCLUDED).forEach(o -> o.setColor(Color.black));
		return obstacleMap.get(INCLUDED);
	}


	private IVector2 adaptedDestination(PathFinderInput input)
	{
		IVector2 dest = input.getDest();
		for (IObstacle obstacle : input.getObstacles())
		{
			dest = obstacle.adaptDestination(input.getPos(), dest).orElse(dest);
		}
		return dest;
	}
}
