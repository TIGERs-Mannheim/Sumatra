/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An obstacle based on the field border (inverted rectangle obstacle)
 */
public class FieldBorderObstacle extends AObstacle
{
	private final IRectangle field;


	public FieldBorderObstacle(final IRectangle field)
	{
		this.field = field;
		setEmergencyBrakeFor(true);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableRectangle(field, Color.black));
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return !field.isPointInShape(point, margin);
	}
}
