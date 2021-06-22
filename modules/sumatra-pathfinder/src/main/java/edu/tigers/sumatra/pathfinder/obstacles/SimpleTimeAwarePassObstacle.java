/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * A time-aware obstacle for the ball that is based on a tracked ball and its trajectory
 */
public class SimpleTimeAwarePassObstacle extends AObstacle
{
	private final IVector2 start;
	private final IVector2 end;
	private final double radius;

	private final IBallTrajectory trajectory;
	private final double tEnd;


	public SimpleTimeAwarePassObstacle(IVector2 start, double kickSpeed, IVector2 end, double radius)
	{
		this.start = start;
		this.end = end;
		this.radius = radius;

		IVector2 kickVel = end.subtractNew(start).scaleTo(kickSpeed * 1000);
		trajectory = Geometry.getBallFactory()
				.createTrajectoryFromKickedBallWithoutSpin(start, kickVel.getXYZVector());
		tEnd = trajectory.getTimeByPos(end);
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		IVector2 pos = trajectory.getPosByTime(Math.min(t, tEnd)).getXYVector();
		return pos.distanceToSqr(point) <= SumatraMath.square(radius + margin);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableTube(Tube.create(start, end, radius)));
	}


	@Override
	public String toString()
	{
		return String.format("SimpleTimeAwarePassObstacle [ball=%s, radius=%s]", trajectory, radius);
	}
}
