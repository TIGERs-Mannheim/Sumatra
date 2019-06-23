/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Abstract {@link ILine Line}
 * 
 * @author Malte
 */
@Persistent
abstract class ALine implements ILine
{
	@Override
	public IVector2 getStart()
	{
		return supportVector();
	}
	
	
	@Override
	public IVector2 getEnd()
	{
		return supportVector().addNew(directionVector());
	}
	
	
	@Override
	public Optional<Double> getSlope()
	{
		return LineMath.getSlope(this);
	}
	
	
	@Override
	public Optional<Double> getYIntercept()
	{
		return LineMath.getYIntercept(this);
	}
	
	
	@Override
	public Optional<Double> getYValue(final double x)
	{
		return LineMath.getYValue(this, x);
	}
	
	
	@Override
	public Optional<Double> getXValue(final double y)
	{
		return LineMath.getXValue(this, y);
	}
	
	
	@Override
	public Optional<Double> getAngle()
	{
		return LineMath.getAngle(this);
	}
	
	
	@Override
	public ILine getOrthogonalLine()
	{
		return Line.fromDirection(supportVector(), directionVector().getNormalVector());
	}
	
	
	@Override
	public boolean isVertical()
	{
		return directionVector().isVertical();
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		return directionVector().isHorizontal();
	}
	
	
	@Override
	public boolean isPointInFront(final IVector2 point)
	{
		return LineMath.isPointInFront(this, point);
	}
	
	
	@Override
	public boolean isPointOnLineSegment(final IVector2 point)
	{
		return isPointOnLineSegment(point, 1e-6);
	}
	
	
	@Override
	public boolean isPointOnLineSegment(final IVector2 point, final double margin)
	{
		return LineMath.isPointOnLineSegment(this, point, margin);
	}
	
	
	@Override
	public boolean isParallelTo(ILine line)
	{
		return directionVector().isParallelTo(line.directionVector());
	}
	
	
	@Override
	public Optional<IVector2> intersectionWith(ILine line)
	{
		return LineMath.intersectionPoint(this, line);
	}
	
	
	@Override
	public Optional<IVector2> intersectionOfSegments(ILine line)
	{
		return LineMath.intersectionPointOfSegments(this, line);
	}
	
	
	@Override
	public Vector2 leadPointOf(IVector2 point)
	{
		return LineMath.leadPointOnLine(this, point);
	}
	
	
	@Override
	public Vector2 nearestPointOnLine(final IVector2 point)
	{
		return leadPointOf(point);
	}
	
	
	@Override
	public Vector2 nearestPointOnLineSegment(final IVector2 point)
	{
		return LineMath.nearestPointOnLineSegment(this, point);
	}
	
	
	@Override
	public double distanceTo(IVector2 point)
	{
		return LineMath.distancePL(point, this);
	}
	
	
	@Override
	public edu.tigers.sumatra.math.line.v2.ILine v2()
	{
		return Lines.lineFromLegacyLine(this);
	}
}
