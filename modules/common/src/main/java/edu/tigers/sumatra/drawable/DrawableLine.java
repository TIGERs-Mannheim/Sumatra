/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * This is a Line connected to a color
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableLine extends ADrawableWithStroke
{
	private final ILine line;
	
	
	@SuppressWarnings("unused")
	private DrawableLine()
	{
		line = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
	}
	
	
	/**
	 * Drawable line from normal line
	 * 
	 * @param line to draw
	 * @param color of this line
	 */
	public DrawableLine(final ILine line, final Color color)
	{
		this.line = line;
		setColor(color);
	}
	
	
	/**
	 * Drawable line from normal line
	 *
	 * @param line to draw
	 * @param color of this line
	 */
	public DrawableLine(final ILineSegment line, final Color color)
	{
		this.line = Line.fromPoints(line.getStart(), line.getEnd());
		setColor(color);
	}
	
	
	/**
	 * Drawable line from normal line
	 *
	 * @param line to draw
	 */
	public DrawableLine(final ILine line)
	{
		this(line, Color.black);
	}
	
	
	/**
	 * Drawable line from normal line
	 *
	 * @param line to draw
	 */
	public DrawableLine(final ILineSegment line)
	{
		this(line, Color.black);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		
		// draw line
		final IVector2 lineStart = tool.transformToGuiCoordinates(line.supportVector(), invert);
		final IVector2 lineEnd = tool.transformToGuiCoordinates(line.directionVector().addNew(line.supportVector()),
				invert);
		g.drawLine((int) lineStart.x(), (int) lineStart.y(), (int) lineEnd.x(), (int) lineEnd.y());
	}
}
