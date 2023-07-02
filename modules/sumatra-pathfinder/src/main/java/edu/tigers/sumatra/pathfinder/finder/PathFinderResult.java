/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.finder;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Value
public class PathFinderResult
{
	TrajPath trajectory;
	List<PathFinderCollision> collisions;


	public static PathFinderResult success(TrajPath trajectory)
	{
		return new PathFinderResult(trajectory, Collections.emptyList());
	}


	public static PathFinderResult withCollision(TrajPath trajectory, List<PathFinderCollision> collisions)
	{
		return new PathFinderResult(trajectory, Collections.unmodifiableList(collisions));
	}


	public PathFinderResult merge(PathFinderResult result)
	{
		List<PathFinderCollision> allCollisions = new ArrayList<>(collisions);
		allCollisions.addAll(result.getCollisions());
		return new PathFinderResult(trajectory, Collections.unmodifiableList(allCollisions));
	}


	public boolean isCollisionFree()
	{
		return collisions.isEmpty();
	}


	public double getFirstCollisionTime()
	{
		return collisions.stream()
				.mapToDouble(PathFinderCollision::getFirstCollisionTime)
				.min()
				.orElse(Double.POSITIVE_INFINITY);
	}
}
