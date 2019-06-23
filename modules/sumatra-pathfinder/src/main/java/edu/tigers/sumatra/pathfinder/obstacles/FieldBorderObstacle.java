/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.ADrawable;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class FieldBorderObstacle extends ADrawable implements IObstacle
{
	private final IRectangle				field;
	private transient DrawableRectangle	drawableRectangle;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private FieldBorderObstacle()
	{
		field = Rectangle.fromCenter(Vector2f.ZERO_VECTOR, 0, 0);
	}
	
	
	/**
	 * @param field
	 */
	public FieldBorderObstacle(final IRectangle field)
	{
		this.field = field;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (drawableRectangle == null)
		{
			drawableRectangle = new DrawableRectangle(field, getColor());
		}
		drawableRectangle.paintShape(g, tool, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return !field.isPointInShape(point);
	}
	
}
