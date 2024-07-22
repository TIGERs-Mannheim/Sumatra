/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.checker;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.pathfinder.EPathFinderShapesLayer;
import edu.tigers.sumatra.pathfinder.obstacles.IObstacle;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;


/**
 * Check for collisions for one obstacle, beginning at the front and tracking the front collision duration.
 */
@RequiredArgsConstructor
class ObstacleCollisionChecker
{
	@Getter
	private final IObstacle obstacle;
	private final double maxSpeed;
	private final ShapeMap shapeMap;

	@Getter
	private double firstCollision = Double.POSITIVE_INFINITY;
	@Getter
	private double nextFrontTimeOffset = Double.NEGATIVE_INFINITY;


	public ObstacleCollisionChecker copy()
	{
		var checker = new ObstacleCollisionChecker(obstacle, maxSpeed, shapeMap);
		checker.nextFrontTimeOffset = nextFrontTimeOffset;
		checker.firstCollision = firstCollision;
		return checker;
	}


	public void stepFront(CollisionInput collisionInput)
	{
		if (skipCheck(collisionInput))
		{
			return;
		}

		double distance = distanceToObstacle(collisionInput, obstacle);
		boolean collides = distance <= 0;
		if (collides)
		{
			firstCollision = collisionInput.getTimeOffset();
			nextFrontTimeOffset = Double.POSITIVE_INFINITY;
		} else
		{
			nextFrontTimeOffset = collisionInput.getTimeOffset() + getTimeToNextCheck(distance);
		}

		if (shapeMap != null)
		{
			if (collides)
			{
				shapeMap.get(EPathFinderShapesLayer.obstacleCheckPointsCollision(obstacle.getIdentifier())).add(
						new DrawablePoint(collisionInput.getRobotPos()).withSize(13).setColor(Color.red)
				);
			} else
			{
				shapeMap.get(EPathFinderShapesLayer.obstacleCheckPointsNoCollision(obstacle.getIdentifier())).add(
						new DrawablePoint(collisionInput.getRobotPos()).withSize(15).setColor(Color.green)
				);
			}
			shapeMap.get(EPathFinderShapesLayer.obstacleCheckPoints(obstacle.getIdentifier())).add(
					new DrawableAnnotation(collisionInput.getRobotPos(),
							String.format("%.2f%n%.0f", collisionInput.getTimeOffset(), distance))
							.withCenterHorizontally(true)
							.withFontHeight(3)
			);
		}
	}


	private boolean skipCheck(CollisionInput collisionInput)
	{
		return collisionInput.getTimeOffset() < nextFrontTimeOffset
				|| hasCollision()
				|| !obstacle.canCollide(collisionInput);
	}


	public boolean hasCollision()
	{
		return Double.isFinite(firstCollision);
	}


	private double getTimeToNextCheck(double distance)
	{
		double dist = Math.max(0, distance / 1000);
		double combinedSpeed = maxSpeed + obstacle.getMaxSpeed();
		if (combinedSpeed <= 0)
		{
			return 0;
		}
		return dist / combinedSpeed;
	}


	private double distanceToObstacle(CollisionInput collisionInput, IObstacle obstacle)
	{
		double distance = obstacle.distanceTo(collisionInput);
		if (obstacle.useDynamicMargin())
		{
			return distance - collisionInput.getExtraMargin();
		}
		return distance;
	}
}
