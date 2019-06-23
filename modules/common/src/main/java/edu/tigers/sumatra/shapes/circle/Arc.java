/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.circle;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Arc extends ACircle
{
	private final double		startAngle, rotation;
									
	/** Center of the circle! */
	private final IVector2	center;
									
	/** Radius of the circle. Mustn't be negative! */
	private final double		radius;
									
									
	@SuppressWarnings("unused")
	protected Arc()
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
	public Arc(final IVector2 center, final double radius, final double startAngle, final double rotation)
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
	public final double getStartAngle()
	{
		return startAngle;
	}
	
	
	/**
	 * @return the angle
	 */
	public final double getRotation()
	{
		return rotation;
	}
	
	
	@Override
	public double getArea()
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
		double a = dir.getAngle();
		double b = AngleMath.normalizeAngle(startAngle + (rotation / 2.0));
		if (Math.abs(AngleMath.difference(a, b)) < (Math.abs(rotation) / 2.0))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
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
	public double radius()
	{
		return radius;
	}
	
	
	@Override
	public IVector2 center()
	{
		return center;
	}
}
