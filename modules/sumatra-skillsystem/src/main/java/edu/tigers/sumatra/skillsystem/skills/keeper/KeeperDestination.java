/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Optional;


public record KeeperDestination(ITrackedBot keeper, IVector2 pos, Optional<Double> time)
{
	public static KeeperDestination fromDestination(ITrackedBot keeper, IVector2 destination)
	{
		return new KeeperDestination(keeper, destination, Optional.empty());
	}


	public static KeeperDestination fromTimedDestination(ITrackedBot keeper, IVector2 destination, double time)
	{
		return new KeeperDestination(
				keeper,
				TrajectoryGenerator.generateVirtualPositionToReachPointInTime(keeper, destination, time),
				Optional.of(time)
		);
	}


	public boolean isComeToAStopFaster(MoveConstraints moveConstraints)
	{
		return time.map(
						t -> TrajectoryGenerator.isComeToAStopFasterToReachPointInTime(moveConstraints, keeper.getPos(),
								keeper.getVel(), pos, t))
				.orElseGet(() -> TrajectoryGenerator.isComeToAStopFaster(moveConstraints, keeper.getPos(),
						keeper.getVel(), pos));
	}
}
