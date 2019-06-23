/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SimpleTimeAwareBallObstacle implements IObstacle
{
	private final TrackedBall	ball;
	private final double			radius;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private SimpleTimeAwareBallObstacle()
	{
		ball = null;
		radius = 0;
	}
	
	
	/**
	 * @param ball
	 * @param radius
	 */
	public SimpleTimeAwareBallObstacle(final TrackedBall ball, final double radius)
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
		IVector2 pos = ball.getPosByTime(t);
		ICircle circle = new Circle(pos, radius + margin);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		IVector2 pos = ball.getPosByTime(t);
		ICircle circle = new Circle(pos, radius + 20);
		return circle.nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 pos = ball.getPos();
		if (!ball.getVel().isZeroVector())
		{
			IVector2 p1 = ball.getPos().subtractNew(ball.getVel().scaleToNew(Geometry.getBotRadius() * 2));
			subPoints.add(p1);
		}
		CircleObstacle circle = new CircleObstacle(new Circle(pos, radius));
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public boolean isSensitiveToTouch()
	{
		return true;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final double guiRadius = tool.scaleXLength(radius);
		
		double tBallStop = ball.getTimeByVel(0);
		double tStep = Math.max(0.05f, tBallStop / 10.0);
		for (double t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 ballPos = ball.getPosByTime(t);
			final IVector2 center = tool.transformToGuiCoordinates(ballPos, invert);
			
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
			if (t > 0)
			{
				g.drawString(String.format("%.1f", t), (float) (center.x() - guiRadius), (float) (center.y() - guiRadius));
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleTimeAwareBallObstacle [ball=");
		builder.append(ball);
		builder.append(", radius=");
		builder.append(radius);
		builder.append("]");
		return builder.toString();
	}
}
