/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.checker;

import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.EPathFinderShapesLayer;
import edu.tigers.sumatra.pathfinder.finder.PathFinderCollision;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import edu.tigers.sumatra.pathfinder.finder.TrajPath;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInputLazy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.Color;
import java.util.List;


/**
 * Check for collisions on a specific path for multiple obstacles.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PathCollisionChecker
{
	@Getter
	private final TrajPath path;
	private final List<ObstacleCollisionChecker> obstacleCollisionCheckers;
	private final ShapeMap shapeMap;

	private double timeOffset;


	public static PathCollisionChecker ofPath(
			TrajPath path,
			List<IObstacle> obstacles,
			ShapeMap shapeMap,
			double initialTimeOffset
	)
	{
		double maxSpeed = path.getMaxSpeed();
		return new PathCollisionChecker(
				path,
				obstacles.stream().map(o -> new ObstacleCollisionChecker(o, maxSpeed, shapeMap)).toList(),
				shapeMap,
				initialTimeOffset
		);
	}


	public PathCollisionChecker append(
			IMoveConstraints mc,
			double connectionTime,
			IVector2 dest
	)
	{
		var checkers = obstacleCollisionCheckers.stream()
				.map(ObstacleCollisionChecker::copy)
				.toList();
		return new PathCollisionChecker(
				path.append(mc, connectionTime, dest),
				checkers,
				shapeMap,
				timeOffset
		);
	}


	private void stepFront()
	{
		CollisionInput collisionInput = new CollisionInputLazy(path, timeOffset);
		obstacleCollisionCheckers.forEach(c -> c.stepFront(collisionInput));

		if (shapeMap != null)
		{
			shapeMap.get(EPathFinderShapesLayer.COLLISION_CHECK_POINTS).add(
					new DrawablePoint(path.getPositionMM(timeOffset)).withSize(13).setColor(Color.orange)
			);
			shapeMap.get(EPathFinderShapesLayer.COLLISION_CHECK_POINTS).add(
					new DrawableAnnotation(path.getPositionMM(timeOffset), String.format("%.1f", timeOffset))
							.withCenterHorizontally(true)
							.withFontHeight(3)
			);
		}
	}


	public void checkUntil(double endTime)
	{
		while (timeOffset < endTime)
		{
			stepFront();
			if (obstacleCollisionCheckers.stream().anyMatch(ObstacleCollisionChecker::hasCollision))
			{
				return;
			}
			timeOffset += 0.1;
		}
	}


	public PathFinderResult getPathFinderResult()
	{
		List<PathFinderCollision> collisions = obstacleCollisionCheckers.stream()
				.filter(ObstacleCollisionChecker::hasCollision)
				.map(c -> new PathFinderCollision(c.getObstacle(), c.getFirstCollision()))
				.toList();
		return PathFinderResult.withCollision(path, collisions);
	}
}
