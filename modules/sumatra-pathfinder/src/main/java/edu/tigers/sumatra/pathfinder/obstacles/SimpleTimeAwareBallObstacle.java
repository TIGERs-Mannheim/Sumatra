/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.obstacles.input.CollisionInput;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
@RequiredArgsConstructor
public class SimpleTimeAwareBallObstacle extends AObstacle
{
	private final IBallTrajectory ballTrajectory;
	private final double radius;
	private final double tEnd;


	public SimpleTimeAwareBallObstacle(IBallTrajectory ballTrajectory, double radius)
	{
		this(ballTrajectory, radius, Double.POSITIVE_INFINITY);
	}


	@Override
	protected void configure()
	{
		setMotionLess(false);
	}


	@Override
	public boolean canCollide(CollisionInput input)
	{
		return ballTrajectory.isInterceptableByTime(input.getTimeOffset());
	}


	@Override
	public double distanceTo(CollisionInput input)
	{
		IVector2 pos = ballTrajectory.getPosByTime(Math.min(input.getTimeOffset(), tEnd)).getXYVector();
		return pos.distanceTo(input.getRobotPos()) - radius;
	}


	@Override
	protected List<IDrawableShape> initializeShapes()
	{
		return List.of(new DrawableTube(Tube.create(
				ballTrajectory.getPosByTime(0).getXYVector(),
				ballTrajectory.getPosByTime(tEnd).getXYVector(),
				radius
		)));
	}


	@Override
	public double getMaxSpeed()
	{
		return ballTrajectory.getVelByTime(0).getLength2();
	}


	@Override
	public Optional<IVector2> adaptDestination(IVector2 robotPos, IVector2 destination)
	{
		ICircle circle = Circle.createCircle(ballTrajectory.getPosByTime(0).getXYVector(), radius);
		return adaptDestination(circle, robotPos, destination);
	}
}
