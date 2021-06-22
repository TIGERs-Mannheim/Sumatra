/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * A simple drawable point
 */
@Persistent
public class DrawablePoint extends ADrawable
{
	private final IVector2 point;
	/** Size of a point in field unit [mm] */
	private double pointSize = 25;


	@SuppressWarnings("unused")
	private DrawablePoint()
	{
		point = null;
	}


	/**
	 * @param point the point to draw
	 * @param color of the point
	 */
	public DrawablePoint(final IVector2 point, final Color color)
	{
		this(point);
		setColor(color);
	}


	/**
	 * @param point the point to draw
	 */
	public DrawablePoint(final IVector2 point)
	{
		this.point = Vector2f.copy(point);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 transPoint = tool.transformToGuiCoordinates(point, invert);
		int guiPointSize = tool.scaleXLength(pointSize);

		final int drawingX = (int) transPoint.x() - (guiPointSize / 2);
		final int drawingY = (int) transPoint.y() - (guiPointSize / 2);

		g.fillOval(drawingX, drawingY, guiPointSize, guiPointSize);
	}


	/**
	 * @param size of the point
	 * @return this
	 */
	public DrawablePoint withSize(final double size)
	{
		pointSize = size;
		return this;
	}


	/**
	 * @return the point of this drawable
	 */
	public IVector2 getPoint()
	{
		return point;
	}
}
