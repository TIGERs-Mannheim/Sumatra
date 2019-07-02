/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
@Persistent
public class SimpleTimeAwarePassObstacle extends AObstacle
{
	private final IVector2 start;
	private final IVector2 end;
	private final double radius;

	private final transient ABallTrajectory trajectory;
	private final transient double tEnd;


	@SuppressWarnings("unused")
	private SimpleTimeAwarePassObstacle()
	{
		this(Vector2f.ZERO_VECTOR, 0.0, Vector2f.ZERO_VECTOR, 0);
	}


	public SimpleTimeAwarePassObstacle(final IVector2 start, final double kickSpeed, final IVector2 end,
			final double radius)
	{
		this.start = start;
		this.end = end;
		this.radius = radius;

		trajectory = BallFactory.createTrajectoryFromStraightKick(start,
				end.subtractNew(start).scaleTo(kickSpeed * 1000));
		tEnd = trajectory.getTimeByPos(end);
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		IVector2 pos = trajectory.getPosByTime(Math.min(t, tEnd)).getXYVector();
		ICircle circle = Circle.createCircle(pos, radius + margin);
		return circle.isPointInShape(point);
	}


	@Override
	protected void initializeShapes()
	{
		shapes.add(new DrawableTube(Tube.create(start, end, radius)));
	}


	@Override
	public String toString()
	{
		return String.format("SimpleTimeAwareBallObstacle [ball=%s, radius=%s]", trajectory, radius);
	}
}
