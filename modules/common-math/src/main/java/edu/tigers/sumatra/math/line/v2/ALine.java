/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Abstract base class for all line types which implements the generic methods that are consistent for all types.
 * 
 * @author Lukas Magel
 */
@Persistent
abstract class ALine implements ILineBase
{
	static final double LINE_MARGIN = 1e-6;
	
	
	@Override
	public edu.tigers.sumatra.math.line.Line toLegacyLine()
	{
		return Line.fromDirection(toLine().supportVector(), toLine().directionVector());
	}
	
	
	@Override
	public Optional<Double> getSlope()
	{
		return LineMath.getSlope(this);
	}
	
	
	@Override
	public Optional<Double> getAngle()
	{
		return LineMath.getAngle(this);
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		return isValid() && directionVector().isHorizontal();
	}
	
	
	@Override
	public boolean isVertical()
	{
		return isValid() && directionVector().isVertical();
	}
	
	
	@Override
	public boolean isParallelTo(final ILineBase other)
	{
		return isValid()
				&& other.isValid()
				&& directionVector().isParallelTo(other.directionVector());
	}
	
	
	@Override
	public boolean isPointOnLine(final IVector2 point)
	{
		IVector2 closestPointOnLine = closestPointOnLine(point);
		return point.distanceTo(closestPointOnLine) <= LINE_MARGIN;
	}
	
	
	@Override
	public double distanceTo(final IVector2 point)
	{
		IVector2 closestPointOnLine = closestPointOnLine(point);
		return point.distanceTo(closestPointOnLine);
	}

	@Override
	public double distanceToSqr(final IVector2 point)
	{
		IVector2 closestPointOnLine = closestPointOnLine(point);
		return point.distanceToSqr(closestPointOnLine);
	}
}
