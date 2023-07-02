/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.acceptor;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;


public class MotionLessObstacleResultAcceptor implements PathFinderResultAcceptor
{
	@Override
	public boolean accept(PathFinderResult result)
	{
		if (result.isCollisionFree())
		{
			return true;
		}

		double distThreshold = SumatraMath.relative(result.getFirstCollisionTime(), 0, 2);

		IVector2 startPos = result.getTrajectory().getPositionMM(0);
		IVector2 endPos = result.getTrajectory().getFinalDestination();
		IVector2 collisionPos = result.getTrajectory().getPositionMM(result.getFirstCollisionTime());
		double startToEndDist = startPos.distanceTo(endPos);
		double collisionToEndDist = collisionPos.distanceTo(endPos);
		double relativeDist = collisionToEndDist / startToEndDist;
		return relativeDist < distThreshold;
	}
}
