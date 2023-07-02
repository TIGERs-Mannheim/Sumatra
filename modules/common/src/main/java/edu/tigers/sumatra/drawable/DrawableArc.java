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
public class DrawableArc extends ADrawableWithStroke
{
	private IArc arc;
	private boolean fill = false;
	private int arcType = Arc2D.PIE;


	@SuppressWarnings("unused") // used by berkeley
	protected DrawableArc()
	{
		arc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, 1);
	}


	public DrawableArc(final IArc arc)
	{
		this.arc = arc;
	}


	public DrawableArc(final IArc arc, final Color color)
	{
		this.arc = arc;
		setColor(color);
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);
		final IVector2 transCenter = tool.transformToGuiCoordinates(arc.center(), invert);
		double radius = tool.scaleGlobalToGui(arc.radius());
		int drawingX = (int) (transCenter.x() - radius);
		int drawingY = (int) (transCenter.y() - radius);

		double startAngle = AngleMath.rad2deg(tool.transformToGuiAngle(arc.getStartAngle(), invert));
		double extendAngle = AngleMath.rad2deg(arc.getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, arcType);
		g.setColor(getColor());
		g.draw(arcShape);
		if (fill)
		{
			g.fill(arcShape);
		}
	}

	@Override
	public DrawableArc setFill(final boolean fill)
	{
		this.fill = fill;
		return this;
	}


	public DrawableArc setArcType(final int arcType)
	{
		if (arcType >= Arc2D.OPEN && arcType <= Arc2D.PIE)
		{
			this.arcType = arcType;
		} else
		{
			throw new IllegalArgumentException("invalid type for Arc: " + arcType);
		}
		return this;
	}

}
