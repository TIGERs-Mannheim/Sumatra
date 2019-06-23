/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Arc extends ACircle
{
	private final float		startAngle, rotation;
	
	/** Center of the circle! */
	private final IVector2	center;
	
	/** Radius of the circle. Mustn't be negative! */
	private final float		radius;
	
	
	@SuppressWarnings("unused")
	private Arc()
	{
		center = AVector2.ZERO_VECTOR;
		radius = 1;
		startAngle = 0;
		rotation = 1;
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 */
	public Arc(final IVector2 center, final float radius, final float startAngle, final float rotation)
	{
		this.center = center;
		this.radius = radius;
		this.startAngle = AngleMath.normalizeAngle(startAngle);
		this.rotation = rotation;
	}
	
	
	/**
	 * @param arc
	 */
	public Arc(final Arc arc)
	{
		center = arc.center;
		radius = arc.radius;
		startAngle = arc.startAngle;
		rotation = arc.rotation;
	}
	
	
	/**
	 * @return the startAngle
	 */
	public final float getStartAngle()
	{
		return startAngle;
	}
	
	
	/**
	 * @return the angle
	 */
	public final float getRotation()
	{
		return rotation;
	}
	
	
	@Override
	public float getArea()
	{
		return (super.getArea() * rotation) / AngleMath.PI_TWO;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		if (super.isPointInShape(point))
		{
			return isPointInArc(point);
		}
		return false;
	}
	
	
	private boolean isPointInArc(final IVector2 point)
	{
		IVector2 dir = point.subtractNew(center());
		if (dir.isZeroVector())
		{
			return true;
		}
		float a = dir.getAngle();
		float b = AngleMath.normalizeAngle(startAngle + (rotation / 2));
		if (Math.abs(AngleMath.difference(a, b)) < (Math.abs(rotation) / 2))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final float margin)
	{
		if (super.isPointInShape(point, margin))
		{
			return isPointInArc(point);
		}
		return false;
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		List<IVector2> intersecs = super.lineIntersections(line);
		List<IVector2> nIntersecs = new ArrayList<>(intersecs.size());
		for (IVector2 inters : intersecs)
		{
			if (isPointInArc(inters))
			{
				nIntersecs.add(inters);
			}
		}
		return nIntersecs;
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		IVector2 npo = super.nearestPointOutside(point);
		if (isPointInArc(npo))
		{
			return npo;
		}
		return point;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Arc [startAngle=");
		builder.append(startAngle);
		builder.append(", rotation=");
		builder.append(rotation);
		builder.append(", radius()=");
		builder.append(radius());
		builder.append(", center()=");
		builder.append(center());
		builder.append("]");
		return builder.toString();
	}
	
	
	@Override
	public float radius()
	{
		return radius;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final IVector2 transBotPos = fieldPanel.transformToGuiCoordinates(center(), invert);
		float radius = fieldPanel.scaleXLength(radius());
		int drawingX = (int) (transBotPos.x() - radius);
		int drawingY = (int) (transBotPos.y() - radius);
		
		float startAngle = AngleMath
				.rad2deg((getStartAngle() + fieldPanel.getFieldTurn().getAngle()) - (AngleMath.PI_HALF * (invert ? -1 : 1)));
		float extendAngle = AngleMath.rad2deg(getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, Arc2D.PIE);
		g.setColor(Color.RED);
		g.draw(arcShape);
	}
}
