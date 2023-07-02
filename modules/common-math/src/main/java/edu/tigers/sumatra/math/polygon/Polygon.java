/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.polygon;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author KaiE
 */
@Value
public class Polygon implements I2DShape
{

	private static final double ACCURACY = 1e-3;
	private static final double SQRT2 = SumatraMath.sqrt(2);
	List<IVector2> points;
	IVector2 centroid;


	/**
	 * @param polyPoints
	 * @param centroid
	 */
	public Polygon(Collection<IVector2> polyPoints, IVector2 centroid)
	{
		this.points = List.copyOf(polyPoints);
		this.centroid = centroid;
	}


	private IVector2 getMarginPoint(final IVector2 point, final double margin)
	{
		return point.subtractNew(centroid).scaleTo(SQRT2 * margin).add(point);
	}


	/**
	 * @see <a href="http://alienryderflex.com/polygon">web-reference site for implementation</a>
	 */
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		var j = points.size() - 1;
		var oddNodes = false;

		for (int i = 0; i < points.size(); i++)
		{
			var x = point.x();
			var y = point.y();
			var a = getMarginPoint(points.get(i), ACCURACY);
			var b = getMarginPoint(points.get(j), ACCURACY);

			var yCheckAB = (a.y() < y) && (b.y() >= y);
			var yCheckBA = (b.y() < y) && (a.y() >= y);
			var xCheck = (a.x() <= x) || (b.x() <= x);
			if (yCheckBA || (yCheckAB && xCheck))
			{
				oddNodes ^= ((a.x() + (((y - a.y()) / (b.y() - a.y())) * (b.x() - a.x()))) < x);
			}
			j = i;
		}
		return oddNodes;


	}


	@Override
	public IVector2 nearestPointOnPerimeterPath(IVector2 point)
	{
		var best = point;
		var minDist = Double.MAX_VALUE;

		for (int i = 0; i < points.size(); ++i)
		{
			var edge = Lines.segmentFromPoints(points.get(i), points.get((i + 1) % points.size()));
			var nPoint = edge.closestPointOnPath(point);
			var dist = nPoint.distanceToSqr(point);

			if (minDist > dist)
			{
				minDist = dist;
				best = nPoint;
			}
		}

		return best;
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		List<IBoundedPath> result = new ArrayList<>();
		for (int i = 0; i < points.size(); ++i)
		{
			var curr = points.get(i);
			var next = points.get((i + 1) % points.size());
			result.add(Lines.segmentFromPoints(curr, next));
		}
		return result;
	}


	@Override
	public Polygon withMargin(double margin)
	{
		return new Polygon(
				points.stream().map(p -> getMarginPoint(p, margin)).toList(),
				centroid
		);
	}
}
