/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Outline of a bot with orientation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableBot implements IDrawableShape
{
	private final DrawableCircle	circle;
	private final DrawableLine		line;
	
	
	@SuppressWarnings("unused")
	private DrawableBot()
	{
		this(Vector2f.ZERO_VECTOR, 0, Color.RED, 90, 75);
	}
	
	
	/**
	 * @param pos
	 * @param orientation
	 * @param color
	 * @param radius
	 * @param center2DribbleDist
	 */
	public DrawableBot(final IVector2 pos, final double orientation, final Color color, final double radius,
			final double center2DribbleDist)
	{
		circle = new DrawableCircle(Circle.createCircle(pos, radius), color);
		line = new DrawableLine(
				Line.fromDirection(pos, Vector2.fromAngle(orientation).scaleTo(center2DribbleDist)),
				color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		circle.paintShape(g, tool, invert);
		line.paintShape(g, tool, invert);
	}
}
