/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.movingrobot.IMovingRobot;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@RequiredArgsConstructor
public abstract class AMovingRobotObstacle extends AMovingObstacle
{
	private final BotID botID;
	protected final IMovingRobot movingRobot;
	private final double minSpeed;
	protected final double maxHorizon;


	@Override
	public String getIdentifier()
	{
		return super.getIdentifier() + " " + movingRobot.getClass().getSimpleName() + " " + botID.getSaveableString();
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		return pos.subtractNew(movingRobot.getPos()).scaleTo(movingRobot.getSpeed());
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return input.getRobotVel().getLength2() > minSpeed || input.accelerating();
	}


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		// certain, if robot is not moving
		return movingRobot.getSpeed() < 0.3;
	}


	@Override
	public Optional<IVector2> adaptDestinationForRobotPos(IVector2 robotPos)
	{
		return adaptDestinationForRobotPos(getObstacleShape(), robotPos);
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 destination)
	{
		return adaptDestination(getObstacleShape(), destination);
	}


	@Override
	public boolean isCollidingAt(IVector2 pos)
	{
		return getObstacleShape().isPointInShape(pos);
	}


	private ICircle getObstacleShape()
	{
		return movingRobot.getMovingHorizon(0);
	}
}
