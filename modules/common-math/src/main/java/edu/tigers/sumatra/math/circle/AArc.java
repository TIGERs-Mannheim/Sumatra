/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Abstract implementation of an arc
 */
@Persistent
abstract class AArc implements IArc
{
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return isPointInShape(point, 0.0);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return CircleMath.isPointInArc(this, point, margin);
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return CircleMath.lineIntersectionsArc(this, line);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return CircleMath.nearestPointOutsideArc(this, point);
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
}
