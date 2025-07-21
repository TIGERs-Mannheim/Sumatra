/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Outline of a bot with orientation
 */
public class DrawableBot implements IDrawableShape
{
	private final DrawableCircle circle;
	private final DrawableLine line;


	public DrawableBot(final IVector2 pos, final double orientation, final Color color, final double radius,
			final double center2DribbleDist)
	{
		circle = new DrawableCircle(Circle.createCircle(pos, radius), color);
		line = new DrawableLine(
				Lines.segmentFromOffset(pos, Vector2.fromAngle(orientation).scaleTo(center2DribbleDist)),
				color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		circle.paintShape(g, tool, invert);
		line.paintShape(g, tool, invert);
	}
}
