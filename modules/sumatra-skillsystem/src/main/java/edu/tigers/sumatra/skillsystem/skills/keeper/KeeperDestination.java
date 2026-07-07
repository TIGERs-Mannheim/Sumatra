package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.math.vector.IVector2;


public record KeeperDestination(IVector2 pos, Double time)
{
	public static KeeperDestination fromDestination(IVector2 destination)
	{
		return new KeeperDestination(destination, null);
	}


	public static KeeperDestination fromTimedDestination(IVector2 destination, double time)
	{
		return new KeeperDestination(destination, time);
	}
}
