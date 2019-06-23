/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class RectangleObstacle extends Rectangle implements IObstacle
{
	private transient DrawableRectangle drawableRectangle;
	
	
	@SuppressWarnings("unused")
	private RectangleObstacle()
	{
		super();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param rect
	 */
	public RectangleObstacle(final IRectangle rect)
	{
		super(rect);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (drawableRectangle == null)
		{
			drawableRectangle = new DrawableRectangle(this);
		}
		drawableRectangle.paintShape(g, tool, invert);
	}
}
