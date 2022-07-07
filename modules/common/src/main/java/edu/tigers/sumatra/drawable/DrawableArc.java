/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawableArc extends Arc implements IDrawableShape
{
	private Color color;
	private boolean fill = false;
	private int arcType = Arc2D.PIE;


	@SuppressWarnings("unused") // used by berkeley
	protected DrawableArc()
	{
		super(Vector2f.ZERO_VECTOR, 1, 0, 1);
		color = Color.black;
	}


	public DrawableArc(final IArc arc)
	{
		super(arc);
	}


	public DrawableArc(final IArc arc, final Color color)
	{
		super(arc);
		this.color = color;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final IVector2 transBotPos = tool.transformToGuiCoordinates(center(), invert);
		double radius = tool.scaleGlobalToGui(radius());
		int drawingX = (int) (transBotPos.x() - radius);
		int drawingY = (int) (transBotPos.y() - radius);

		double startAngle = AngleMath.rad2deg(tool.transformToGuiAngle(getStartAngle(), invert));
		double extendAngle = AngleMath.rad2deg(getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, arcType);
		g.setColor(color);
		g.draw(arcShape);
		if (fill)
		{
			g.fill(arcShape);
		}
	}


	@Override
	public DrawableArc setColor(final Color color)
	{
		this.color = color;
		return this;
	}


	@Override
	public DrawableArc setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}


	public void setArcType(final int arcType)
	{
		if (arcType >= Arc2D.OPEN && arcType <= Arc2D.PIE)
		{
			this.arcType = arcType;
		} else
		{
			throw new IllegalArgumentException("invalid type for Arc: " + arcType);
		}
	}

}
