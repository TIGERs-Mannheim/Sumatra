/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.boundary.IShapeBoundary;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILineSegment;
import org.apache.commons.lang.NotImplementedException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class DrawableShapeBoundary extends ADrawableWithStroke
{
	private final List<IDrawableShape> drawables;


	public DrawableShapeBoundary(I2DShape shape, Color color)
	{
		drawables = shape.getPerimeterPath().stream().map(this::pathToDrawable)
				.collect(Collectors.toCollection(ArrayList::new));
		setColor(color);
	}


	public DrawableShapeBoundary(I2DShape shape)
	{
		this(shape, Color.black);
	}


	public DrawableShapeBoundary(IShapeBoundary shapeBoundary)
	{
		this(shapeBoundary.getShape(), Color.black);
	}


	public DrawableShapeBoundary(IShapeBoundary shapeBoundary, Color color)
	{
		this(shapeBoundary.getShape(), color);
	}


	private IDrawableShape pathToDrawable(IBoundedPath path)
	{
		return switch (path)
		{
			case ILineSegment segment -> new DrawableLine(segment);
			case IArc arc -> new DrawableArc(arc);
			case ICircle circle -> new DrawableCircle(circle);
			case null, default -> throw new NotImplementedException();
		};
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		for (var drawable : drawables)
		{
			drawable.paintShape(g, tool, invert);
		}
	}


	@Override
	public ADrawable setColor(Color color)
	{
		drawables.forEach(d -> d.setColor(color));
		return this;
	}


	@Override
	public ADrawableWithStroke setStrokeWidth(double strokeWidth)
	{
		drawables.forEach(d -> d.setStrokeWidth(strokeWidth));
		return this;
	}


	public DrawableShapeBoundary setArcType(int arcType)
	{
		drawables.forEach(d -> {
			if (d instanceof DrawableArc arc)
			{
				arc.setArcType(arcType);
			}
		});
		return this;
	}
}
