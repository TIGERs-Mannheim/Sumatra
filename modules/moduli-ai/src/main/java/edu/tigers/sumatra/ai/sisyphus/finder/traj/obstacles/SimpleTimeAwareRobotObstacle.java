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
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class SimpleTimeAwareRobotObstacle implements IObstacle
{
	private final ITrackedBot	tBot;
	private final double			radius;
	private final double			maxPredictionTime;
	private final double			margin;
	private final double			extraMargin;
	private Color					color	= Color.red;
	
	
	@SuppressWarnings("unused")
	private SimpleTimeAwareRobotObstacle()
	{
		tBot = null;
		radius = 0;
		margin = 0;
		maxPredictionTime = 0;
		extraMargin = 0;
	}
	
	
	/**
	 * @param tBot
	 * @param radius
	 * @param margin
	 * @param maxPredictionTime
	 */
	public SimpleTimeAwareRobotObstacle(final ITrackedBot tBot, final double radius, final double margin,
			final double maxPredictionTime)
	{
		super();
		this.tBot = tBot;
		this.radius = radius;
		this.margin = margin;
		this.maxPredictionTime = maxPredictionTime;
		extraMargin = ObstacleGenerator.getExtraMargin(tBot.getVel().getLength());
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointCollidingWithObstacle(point, t, 0);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		IVector2 pos = tBot.getPosByTime(Math.min(t, maxPredictionTime));
		ICircle circle = new Circle(pos, radius + margin + extraMargin);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		IVector2 pos = tBot.getPosByTime(Math.min(t, maxPredictionTime));
		ICircle circle = new Circle(pos, radius + margin + extraMargin);
		return circle.nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 pos = tBot.getPos();
		CircleObstacle circle = new CircleObstacle(new Circle(pos, radius));
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final double guiRadius = tool.scaleXLength(radius + extraMargin);
		
		IVector2 lastPos = null;
		for (double t = 0; t <= 0; t += 0.2)
		{
			IVector2 botPos = tBot.getPosByTime(t);
			if ((lastPos != null) && lastPos.equals(botPos, 1))
			{
				break;
			}
			lastPos = botPos;
			final IVector2 center = tool.transformToGuiCoordinates(botPos, invert);
			
			g.setColor(color);
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
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
