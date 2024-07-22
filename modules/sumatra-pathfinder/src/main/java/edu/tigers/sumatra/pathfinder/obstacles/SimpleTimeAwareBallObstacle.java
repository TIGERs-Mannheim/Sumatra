/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
@RequiredArgsConstructor
public class SimpleTimeAwareBallObstacle extends AMovingObstacle
{
	private final IBallTrajectory ballTrajectory;
	private final double radius;
	private final double tStart;
	private final double tEnd;


	public SimpleTimeAwareBallObstacle(IBallTrajectory ballTrajectory, double radius)
	{
		this(ballTrajectory, radius, Double.POSITIVE_INFINITY);
	}


	public SimpleTimeAwareBallObstacle(IBallTrajectory ballTrajectory, double radius, double tEnd)
	{
		this(ballTrajectory, radius, 0, tEnd);
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return input.getTimeOffset() >= tStart && ballTrajectory.isInterceptableByTime(input.getTimeOffset() - tStart);
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		if (input.getTimeOffset() < tStart)
		{
			return Double.POSITIVE_INFINITY;
		}
		IVector2 pos = ballTrajectory.getPosByTime(Math.min(input.getTimeOffset() - tStart, tEnd)).getXYVector();
		return pos.distanceTo(input.getRobotPos()) - radius;
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(new DrawableTube(Tube.create(
				ballTrajectory.getPosByTime(0).getXYVector(),
				ballTrajectory.getPosByTime(tEnd - tStart).getXYVector(),
				radius
		)));
	}


	@Override
	public double getMaxSpeed()
	{
		return ballTrajectory.getVelByTime(0).getLength2();
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


	private ITube getObstacleShape()
	{
		return Tube.create(
				ballTrajectory.getPosByTime(0).getXYVector(),
				ballTrajectory.getPosByTime(tEnd - tStart).getXYVector(),
				radius
		);
	}


	@Override
	public boolean collisionLikely(double t, IVector2 pos)
	{
		return ballTrajectory.getVelByTime(0).getLength2() < 0.3;
	}


	@Override
	public IVector2 velocity(IVector2 pos, double t)
	{
		if (t < tStart)
		{
			return Vector2.zero();
		}
		return ballTrajectory.getVelByTime(t - tStart).getXYVector();
	}
}
