/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.util.List;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
public class SimpleTimeAwareBallObstacle extends AObstacle
{
	private final ITrackedBall ball;
	private final double radius;


	public SimpleTimeAwareBallObstacle(final ITrackedBall ball, final double radius)
	{
		this.ball = ball;
		this.radius = radius;
		setActivelyEvade(true);
		setEmergencyBrakeFor(true);
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		if (!ball.getTrajectory().isInterceptableByTime(t))
		{
			return false;
		}
		IVector2 pos = ball.getTrajectory().getPosByTime(t).getXYVector();
		return pos.distanceToSqr(point) <= SumatraMath.square(radius + margin);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableCircle(Circle.createCircle(ball.getPos(), radius)));
	}


	@Override
	public String toString()
	{
		return String.format("SimpleTimeAwareBallObstacle [ball=%s, radius=%s]", ball, radius);
	}
}
