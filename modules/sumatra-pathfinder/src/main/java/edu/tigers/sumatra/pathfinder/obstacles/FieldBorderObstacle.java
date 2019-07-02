/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * An obstacle based on the field border (inverted rectangle obstacle)
 */
@Persistent
public class FieldBorderObstacle extends AObstacle
{
	private final IRectangle field;


	@SuppressWarnings("unused")
	private FieldBorderObstacle()
	{
		field = Rectangle.fromCenter(Vector2f.ZERO_VECTOR, 0, 0);
	}


	public FieldBorderObstacle(final IRectangle field)
	{
		this.field = field;
	}


	@Override
	protected void initializeShapes()
	{
		shapes.add(new DrawableRectangle(field, Color.black));
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return !field.isPointInShape(point, margin);
	}
}
