/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
@Persistent
public class SimpleTimeAwareBallObstacle extends AObstacle
{
	private final ITrackedBall ball;
	private final double radius;


	@SuppressWarnings("unused")
	private SimpleTimeAwareBallObstacle()
	{
		ball = TrackedBall.createStub();
		radius = 0;
	}


	public SimpleTimeAwareBallObstacle(final ITrackedBall ball, final double radius)
	{
		this.ball = ball;
		this.radius = radius;
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		if (!ball.getTrajectory().isInterceptableByTime(t))
		{
			return false;
		}
		IVector2 pos = ball.getTrajectory().getPosByTime(t).getXYVector();
		ICircle circle = Circle.createCircle(pos, radius + margin);
		return circle.isPointInShape(point);
	}


	@Override
	protected void initializeShapes()
	{
		shapes.add(new DrawableCircle(Circle.createCircle(ball.getPos(), radius)));
	}


	@Override
	public String toString()
	{
		return String.format("SimpleTimeAwareBallObstacle [ball=%s, radius=%s]", ball, radius);
	}
}
