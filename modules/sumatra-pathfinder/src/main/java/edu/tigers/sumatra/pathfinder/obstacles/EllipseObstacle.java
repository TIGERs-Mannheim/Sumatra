/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class EllipseObstacle implements IObstacle
{
	private final IEllipse	ellipse;
	private Color				color	= Color.red;
	
	
	/**
	 * 
	 */
	protected EllipseObstacle()
	{
		ellipse = Ellipse.createEllipse(Vector2.ZERO_VECTOR, 0, 0);
	}
	
	
	/**
	 * @param ellipse
	 */
	public EllipseObstacle(final IEllipse ellipse)
	{
		this.ellipse = ellipse;
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	public EllipseObstacle(final IVector2 center, final double radiusX, final double radiusY, final double turnAngle)
	{
		ellipse = Ellipse.createTurned(center, radiusX, radiusY, turnAngle);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 */
	public EllipseObstacle(final IVector2 center, final double radiusX, final double radiusY)
	{
		ellipse = Ellipse.createEllipse(center, radiusX, radiusY);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		DrawableEllipse de = new DrawableEllipse(ellipse);
		de.setFill(true);
		de.setColor(color);
		de.paintShape(g, tool, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return ellipse.isPointInShape(point);
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
