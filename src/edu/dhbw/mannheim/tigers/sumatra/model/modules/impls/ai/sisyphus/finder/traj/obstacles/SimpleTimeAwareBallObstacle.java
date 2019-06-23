/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SimpleTimeAwareBallObstacle implements IObstacle
{
	private final TrackedBall	ball;
	private final float			radius;
	
	
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
	public SimpleTimeAwareBallObstacle(final TrackedBall ball, final float radius)
	{
		this.ball = ball;
		this.radius = radius;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		IVector2 pos = ball.getPosByTime(t);
		ICircle circle = new Circle(pos, radius);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
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
		Circle circle = new Circle(pos, radius);
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final float guiRadius = fieldPanel.scaleXLength(radius);
		
		float tBallStop = ball.getTimeByVel(0);
		float tStep = Math.max(0.05f, tBallStop / 10);
		for (float t = 0; t <= tBallStop; t += tStep)
		{
			IVector2 ballPos = ball.getPosByTime(t);
			final IVector2 center = fieldPanel.transformToGuiCoordinates(ballPos, invert);
			
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
			if (t > 0)
			{
				g.drawString(String.format("%.1f", t), (center.x() - guiRadius), (center.y() - guiRadius));
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
