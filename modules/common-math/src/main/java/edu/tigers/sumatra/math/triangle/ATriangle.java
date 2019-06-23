/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author MarkG
 */
@Persistent
abstract class ATriangle implements ITriangle
{
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return TriangleMath.isPointInShape(this, point);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		ITriangle marginTriangle = TriangleMath.withMargin(this, margin);
		return marginTriangle.isPointInShape(point);
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean isNeighbour(ITriangle triangle)
	{
		throw new NotImplementedException();
	}

	@Override
	public double area(){ throw new NotImplementedException();}
}
