/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class CircleObstacle extends Circle implements IObstacle
{
	private Color color = Color.red;
	
	
	protected CircleObstacle()
	{
	}
	
	
	/**
	 * @param circle
	 */
	public CircleObstacle(final ICircle circle)
	{
		super(circle);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = tool.transformToGuiCoordinates(center(), invert);
		final double radius = tool.scaleXLength(radius());
		
		g.setColor(color);
		g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
