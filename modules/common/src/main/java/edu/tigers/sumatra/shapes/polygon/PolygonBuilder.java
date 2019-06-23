/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2016
 * Author(s): KaiE
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.polygon;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author KaiE
 */
public class PolygonBuilder
{
	private List<IVector2> points = new ArrayList<IVector2>();
	
	
	/**
	 * @param point
	 * @throws IllegalArgumentException when the new point would cause an intersection (destroy the polygon)
	 */
	public void addPoint(final IVector2 point) throws IllegalArgumentException
	{
		for (int i = 0; i < (points.size() - 1); ++i)
		{
			final IVector2 startPoint = points.get(0);
			final IVector2 lastPoint = points.get(points.size() - 1);
			final IVector2 p1 = points.get(i);
			final IVector2 p2 = points.get(i + 1);
			final IVector2 fpoint = GeoMath.intersectionBetweenPaths(p1, p2, point, startPoint);
			if ((fpoint != null) && !startPoint.equals(fpoint, 0.001))
			{
				throw new IllegalArgumentException("polygon is damaged by point " + point);
			}
			final IVector2 lpoint = GeoMath.intersectionBetweenPaths(p1, p2, point, lastPoint);
			if ((lpoint != null) && !lastPoint.equals(lpoint, 0.001))
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
		double Cx = 0;
		double Cy = 0;
		final int s = points.size();
		for (int i = 0; i < s; ++i)
		{
			final IVector2 current = points.get(i);
			final IVector2 next = points.get((i + 1) % s);
			Cx += (current.x() + next.x()) * ((current.x() * next.y()) - (next.x() * current.y()));
			Cy += (current.y() + next.y()) * ((current.x() * next.y()) - (next.x() * current.y()));
		}
		
		return new Vector2(Cx * constant, Cy * constant);
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
		return new Polygon(points, getSignedArea(), getCentroid());
	}
}
