/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author KaiE
 */
public class PolygonBuilder
{
	private List<IVector2> points = new ArrayList<>();
	
	
	/**
	 * @param point
	 * @throws IllegalArgumentException when the new point would cause an intersection (destroy the polygon)
	 */
	public void addPoint(final IVector2 point)
	{
		for (int i = 0; i < (points.size() - 1); ++i)
		{
			final IVector2 startPoint = points.get(0);
			final IVector2 lastPoint = points.get(points.size() - 1);
			final IVector2 p1 = points.get(i);
			final IVector2 p2 = points.get(i + 1);
			Optional<IVector2> fpoint = LineMath.intersectionPointOfSegments(
					Line.fromPoints(p1, p2),
					Line.fromPoints(point, startPoint));
			
			if (fpoint.isPresent() && !startPoint.isCloseTo(fpoint.get(), 0.001))
			{
				throw new IllegalArgumentException("polygon is damaged by point " + point);
			}
			Optional<IVector2> lpoint = LineMath.intersectionPointOfSegments(
					Line.fromPoints(p1, p2),
					Line.fromPoints(point, lastPoint));
			
			if (lpoint.isPresent() && !lastPoint.isCloseTo(lpoint.get(), 0.001))
			{
				throw new IllegalArgumentException("polygon is damaged by point " + point);
			}
		}
		points.add(point);
	}
	
	
	/**
	 * @return the signed area ccw: >0//cw: <0
	 */
	private double getSignedArea()
	{
		double area = 0;
		final int s = points.size();
		for (int i = 0; i < points.size(); ++i)
		{
			final IVector2 p1 = points.get(i % s);
			final IVector2 p2 = points.get((i + 1) % s);
			area += (p1.x() * p2.y()) - (p1.y() * p2.x());
		}
		return area / 2;
	}
	
	
	/**
	 * @return
	 */
	private IVector2 getCentroid()
	{
		final double constant = 1.0 / (6 * getSignedArea());
		double cx = 0;
		double cy = 0;
		final int s = points.size();
		for (int i = 0; i < s; ++i)
		{
			final IVector2 current = points.get(i);
			final IVector2 next = points.get((i + 1) % s);
			cx += (current.x() + next.x()) * ((current.x() * next.y()) - (next.x() * current.y()));
			cy += (current.y() + next.y()) * ((current.x() * next.y()) - (next.x() * current.y()));
		}
		
		return Vector2f.fromXY(cx * constant, cy * constant);
	}
	
	
	/**
	 * @return
	 */
	public Polygon build()
	{
		if (points.size() < 3)
		{
			throw new IllegalStateException("at least 3 points required");
		}
		return new Polygon(points, getCentroid());
	}
}
