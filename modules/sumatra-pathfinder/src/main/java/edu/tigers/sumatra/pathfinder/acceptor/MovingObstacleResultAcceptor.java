/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.acceptor;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import lombok.RequiredArgsConstructor;

import static edu.tigers.sumatra.math.SumatraMath.square;


@RequiredArgsConstructor
public class MovingObstacleResultAcceptor implements PathFinderResultAcceptor
{
	@Configurable(defValue = "0.0", comment = "Brake time [s] to require in addition to time to collision")
	private static double brakeTimeOffset = 0.0;

	@Configurable(defValue = "300.0", comment = "Threshold [mm] for distance from dest to collision to reject path")
	private static double distDestToCollisionThreshold = 300.0;

	@Configurable(defValue = "1.5", comment = "Acceptable threshold [m/s] for potential robot speed at collision")
	private static double collisionSpeedThreshold = 1.5;

	static
	{
		ConfigRegistration.registerClass("sisyphus", MovingObstacleResultAcceptor.class);
	}

	private final IMoveConstraints moveConstraints;


	@Override
	public boolean accept(PathFinderResult result)
	{
		if (result.isCollisionFree())
		{
			return true;
		}

		var currentPos = result.getTrajectory().getPositionMM(0);
		var finalDestination = result.getTrajectory().getFinalDestination();
		var distToDestSqr = currentPos.distanceToSqr(finalDestination);
		if (distToDestSqr < square(Geometry.getBotRadius() * 2))
		{
			// we are currently already almost at the destination.
			// Collision would be imminent anyway, even if we overshoot.
			// Avoiding the collision could even make it worth.
			return true;
		}

		var firstCollision = result.getFirstCollision().orElseThrow();
		if (firstCollision.getObstacle().hasPriority())
		{
			// The other obstacle has priority (e.g. own robots)
			return false;
		}

		var collisionTime = firstCollision.getFirstCollisionTime();
		var collisionPos = result.getTrajectory().getPositionMM(collisionTime);
		var distDestToCollisionSqr = finalDestination.distanceToSqr(collisionPos);
		if (distDestToCollisionSqr > square(distDestToCollisionThreshold))
		{
			// Collision is not near destination, so no need to aggressively accept the collision
			return false;
		}

		boolean collisionLikely = result.getCollisions().stream()
				.anyMatch(c -> c.getObstacle().collisionLikely(
						c.getFirstCollisionTime(),
						result.getTrajectory().getPositionMM(c.getFirstCollisionTime())
				));
		if (collisionLikely)
		{
			// collision is very likely (maybe a standing obstacle)
			return false;
		}

		double speedOnCollision = result.getTrajectory().getVelocity(collisionTime).getLength2();
		if (speedOnCollision < collisionSpeedThreshold)
		{
			// be a bit aggressive to non-priority obstacles
			return true;
		}

		double currentSpeed = result.getTrajectory().getVelocity(0).getLength2();
		double brakeTime = currentSpeed / moveConstraints.getAccMax();
		return result.getFirstCollisionTime() > brakeTime + brakeTimeOffset;
	}
}
