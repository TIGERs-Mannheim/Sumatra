/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Abstract implementation of an ellipse.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
abstract class AEllipse implements IEllipse
{
	@Override
	public IVector2 nearestPointInside(IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 getFocusPositive()
	{
		return center().addNew(getFocusFromCenter());
	}
	
	
	@Override
	public IVector2 getFocusNegative()
	{
		return center().addNew(getFocusFromCenter().multiplyNew(-1));
	}
	
	
	@Override
	public IVector2 getFocusFromCenter()
	{
		final double x;
		final double y;
		if (getRadiusX() > getRadiusY())
		{
			x = SumatraMath.sqrt((getRadiusX() * getRadiusX()) - (getRadiusY() * getRadiusY()));
			y = 0;
		} else
		{
			x = 0;
			y = SumatraMath.sqrt((getRadiusY() * getRadiusY()) - (getRadiusX() * getRadiusX()));
		}
		return Vector2.fromXY(x, y).turn(getTurnAngle());
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		final double lenPos = getFocusPositive().subtractNew(point).getLength2();
		final double lenNeg = getFocusNegative().subtractNew(point).getLength2();
		return (lenPos + lenNeg) <= getDiameterMax();
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		Ellipse marginEllipse = Ellipse.createTurned(center(), getRadiusX() + margin, getRadiusY() + margin,
				getTurnAngle());
		return marginEllipse.isPointInShape(point);
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		// get a point, that lies on one line with center and point and is outside field (greater radius distance from
		// center)
		IVector2 pointOutside = center().addNew(point.subtractNew(center()).scaleTo(getDiameterMax()));
		List<IVector2> intsPt = EllipseMath.lineSegmentIntersections(this, Line.fromPoints(center(), pointOutside));
		if (intsPt.size() != 1)
		{
			throw new IllegalStateException("Exactly one intersecting point expected, but was " + intsPt.size());
		}
		return intsPt.get(0);
	}
	
	
	@Override
	public IVector2 stepOnCurve(final IVector2 start, final double step)
	{
		return EllipseMath.stepOnCurve(this, start, step);
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return EllipseMath.lineIntersections(this, line);
	}
	
	
	@Override
	public double getCircumference()
	{
		double l = (getRadiusX() - getRadiusY()) / (getRadiusX() + getRadiusY());
		double e = 1 + ((3 * l * l) / (10.0 + SumatraMath.sqrt(4 - (3 * l * l))));
		return Math.PI * (getRadiusX() + getRadiusY()) * e;
	}
	
	
	@Override
	public double getDiameterMax()
	{
		return Math.max(getRadiusX(), getRadiusY()) * 2;
	}
}
