/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SimpleTimeAwareBallObstacle implements IObstacle
{
	private final ITrackedBall	ball;
	private final double			radius;
	
	
	@SuppressWarnings("unused")
	private SimpleTimeAwareBallObstacle()
	{
		ball = TrackedBall.createStub();
		radius = 0;
	}
	
	
	/**
	 * @param ball
	 * @param radius
	 */
	public SimpleTimeAwareBallObstacle(final ITrackedBall ball, final double radius)
	{
		this.ball = ball;
		this.radius = radius;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointCollidingWithObstacle(point, t, 0);
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
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final double guiRadius = tool.scaleXLength(radius);
		
		double tBallStop = ball.getTrajectory().getTimeByVel(0);
		double tStep = Math.max(0.05f, tBallStop / 10.0);
		for (double t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 ballPos = ball.getTrajectory().getPosByTime(t).getXYVector();
			if (isPointCollidingWithObstacle(ballPos, t))
			{
				final IVector2 center = tool.transformToGuiCoordinates(ballPos, invert);
				
				g.setColor(Color.RED);
				g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
						(int) guiRadius * 2);
				if (t > 0)
				{
					g.drawString(String.format("%.1f", t), (float) (center.x() - guiRadius),
							(float) (center.y() - guiRadius));
				}
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		return String.format("SimpleTimeAwareBallObstacle [ball=%s, radius=%s]", ball, radius);
	}
}
