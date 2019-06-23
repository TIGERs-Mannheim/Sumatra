/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class SimpleTimeAwareRobotObstacle implements IObstacle
{
	private final ITrackedBot tBot;
	private final double radius;
	private final double extraMargin;
	private Color color = Color.red;
	
	
	@SuppressWarnings("unused")
	private SimpleTimeAwareRobotObstacle()
	{
		tBot = TrackedBot.stub(BotID.noBot(), 0);
		radius = 0;
		extraMargin = 0;
	}
	
	
	/**
	 * @param tBot
	 * @param radius
	 */
	public SimpleTimeAwareRobotObstacle(final ITrackedBot tBot, final double radius)
	{
		super();
		this.tBot = tBot;
		this.radius = radius;
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
		IVector2 pos = tBot.getPosByTime(t);
		ICircle circle = Circle.createCircle(pos, radius + margin + extraMargin);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final double guiRadius = tool.scaleXLength(radius + extraMargin);
		
		IVector2 lastPos = null;
		for (double t = 0; t <= 0; t += 0.2)
		{
			IVector2 botPos = tBot.getPosByTime(t);
			if ((lastPos != null) && lastPos.isCloseTo(botPos, 1))
			{
				break;
			}
			lastPos = botPos;
			final IVector2 center = tool.transformToGuiCoordinates(botPos, invert);
			
			g.setColor(color);
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
	
	
	@Override
	public boolean isWorthBrakingFor()
	{
		return true;
	}
}
