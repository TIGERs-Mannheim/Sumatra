/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;


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
		this(AVector2.ZERO_VECTOR, 0, Color.RED, 90, 75);
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
		circle = new DrawableCircle(new Circle(pos, radius), color);
		line = new DrawableLine(
				new Line(pos, new Vector2(orientation).scaleTo(center2DribbleDist)),
				color, false);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setStroke(new BasicStroke(1));
		circle.paintShape(g, tool, invert);
		line.paintShape(g, tool, invert);
	}
}
