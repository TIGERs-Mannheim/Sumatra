/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.acceptor;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.pathfinder.finder.PathFinderResult;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MovingObstacleResultAcceptor implements PathFinderResultAcceptor
{
	@Configurable(defValue = "0.5", comment = "Brake time [s] to require in addition to time to collision")
	private static double brakeTimeOffset = 0.5;

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
		double currentSpeed = result.getTrajectory().getVelocity(0).getLength2();
		double brakeTime = currentSpeed / moveConstraints.getAccMax() + brakeTimeOffset;
		return result.getFirstCollisionTime() > brakeTime;
	}
}
