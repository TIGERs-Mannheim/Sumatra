/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


/**
 * Mutable implementation of {@link ITriangle}.
 * 
 * @author Malte
 */
@Persistent
public class Triangle extends ATriangle
{
	private List<IVector2>	points	= new ArrayList<IVector2>();
	
	
	@SuppressWarnings("unused")
	private Triangle()
	{
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	public Triangle(final IVector2 a, final IVector2 b, final IVector2 c)
	{
		points.add(a);
		points.add(b);
		points.add(c);
	}
	
	
	/**
	 * @param triangle
	 */
	public Triangle(final Triangle triangle)
	{
		points.addAll(triangle.points);
	}
	
	
	@Override
	public List<IVector2> getCorners()
	{
		return points;
	}
	
	
	@Override
	public float getArea()
	{
		float x1 = getCorners().get(0).x();
		float x2 = getCorners().get(1).x();
		float x3 = getCorners().get(2).x();
		float y1 = getCorners().get(0).y();
		float y2 = getCorners().get(1).y();
		float y3 = getCorners().get(2).y();
		return Math.abs(((x1 * (y2 - y3)) + (x2 * (y3 - y1)) + (x3 * (y1 - y2))) / 2.0f);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		IVector2 a = getCorners().get(0);
		IVector2 b = getCorners().get(1);
		IVector2 c = getCorners().get(2);
		
		float v1X = a.x();
		float v2X = b.x();
		float v3X = c.x();
		float v1Y = a.y();
		float v2Y = b.y();
		float v3Y = c.y();
		
		float pointX = point.x();
		float pointY = point.y();
		
		float A = ((-v2Y * v3X) + (v1Y * (-v2X + v3X)) + (v1X * (v2Y - v3Y)) + (v2X * v3Y)) / 2f;
		float sign = A < 0 ? -1 : 1;
		float s = (((v1Y * v3X) - (v1X * v3Y)) + ((v3Y - v1Y) * pointX) + ((v1X - v3X) * pointY)) * sign;
		float t = (((v1X * v2Y) - (v1Y * v2X)) + ((v1Y - v2Y) * pointX) + ((v2X - v1X) * pointY)) * sign;
		return (s > 0) && (t > 0) && ((s + t) < (2 * A * sign));
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
}
