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
import java.util.Collection;
import java.util.List;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.shapes.I2DShape;


/**
 * @author KaiE
 */
public class Polygon implements I2DShape
{
	
	private final IVector2[]		points;
	private final double				signedArea;
	private final IVector2			centroid;
	private static final double	ACCURACY	= 1e-3;
	private static final double	SQRT2		= Math.sqrt(2);
														
														
	/**
	 * @param polyPoints
	 * @param sarea
	 * @param centroid
	 */
	public Polygon(final Collection<IVector2> polyPoints, final double sarea, final IVector2 centroid)
	{
		points = polyPoints.toArray(new IVector2[polyPoints.size()]);
		signedArea = sarea;
		this.centroid = centroid;
	}
	
	
	private IVector2 getMarginPoint(final IVector2 point, final double margin)
	{
		return point.subtractNew(centroid).scaleTo(SQRT2 * margin).add(point);
	}
	
	
	@Override
	public double getArea()
	{
		return Math.abs(signedArea);
	}
	
	
	/**
	 * @see <a href="http://alienryderflex.com/polygon">web-reference site for implementation</a>
	 */
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		int j = points.length - 1;
		boolean oddNodes = false;
		
		for (int i = 0; i < points.length; i++)
		{
			final double x = point.x();
			final double y = point.y();
			final IVector2 a = getMarginPoint(points[i], margin + ACCURACY);
			final IVector2 b = getMarginPoint(points[j], margin + ACCURACY);
			
			final boolean yCheckAB = (a.y() < y) && (b.y() >= y);
			final boolean yCheckBA = (b.y() < y) && (a.y() >= y);
			final boolean xCheck = (a.x() <= x) || (b.x() <= x);
			if (yCheckBA || (yCheckAB && xCheck))
			{
				oddNodes ^= ((a.x() + (((y - a.y()) / (b.y() - a.y())) * (b.x() - a.x()))) < x);
			}
			j = i;
		}
		return oddNodes;
		
		
	}
	
	
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		return lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return nearestPointOutside(point, 0);
	}
	
	
	/**
	 * nearest point with margin
	 * 
	 * @param point
	 * @param margin
	 * @return
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final double margin)
	{
		if (!isPointInShape(point, margin))
		{
			return point;
		}
		
		IVector2 best = point;
		double minDist = Double.MAX_VALUE;
		
		for (int i = 0; i < points.length; ++i)
		{
			final IVector2 nPoint = GeoMath.leadPointOnLine(point,
					getMarginPoint(points[i], margin),
					getMarginPoint(points[(i + 1) % points.length], margin));
			final double dist = GeoMath.distancePPSqr(nPoint, point);
			
			if (minDist > dist)
			{
				minDist = dist;
				best = nPoint;
			}
		}
		
		return best;
	}
	
	
	/**
	 * nearest point with line hint
	 * 
	 * @param point
	 * @param p2bl
	 * @param margin
	 * @return
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 p2bl, final double margin)
	{
		if (!isPointInShape(point, margin))
		{
			return point;
		}
		
		if (point.equals(p2bl, ACCURACY))
		{
			return nearestPointOutside(point, margin);
		}
		
		return GeoMath.nearestPointInList(lineIntersections(Line.newLine(point, p2bl), margin), point);
		
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return lineIntersections(line, 0);
	}
	
	
	/**
	 * intersections with polygon deformed by margin
	 * 
	 * @param line
	 * @param margin
	 * @return
	 */
	public List<IVector2> lineIntersections(final ILine line, final double margin)
	{
		List<IVector2> result = new ArrayList<IVector2>();
		for (int i = 0; i < points.length; ++i)
		{
			final IVector2 curr = getMarginPoint(points[i], margin);
			final IVector2 next = getMarginPoint(points[(i + 1) % points.length], margin);
			final ILine path = Line.newLine(curr, next);
			final IVector2 intersection = GeoMath.intersectionPointLinePath(line, path.supportVector(),
					path.directionVector());
					
			if ((intersection != null) && !intersection.equals(curr, ACCURACY))
			{
				result.add(intersection);
			}
			
		}
		return result;
	}
	
}
