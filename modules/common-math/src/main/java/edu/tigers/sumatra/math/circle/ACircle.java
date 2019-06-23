/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent
public abstract class ACircle implements ICircle
{
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return CircleMath.isPointInCircle(this, point, margin);
	}
	
	
	@Override
	public boolean isIntersectingWithLine(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		return CircleMath.lineIntersectionsCircle(this, line);
	}
	
	
	@Override
	public List<IVector2> lineSegmentIntersections(final ILine line)
	{
		return CircleMath.lineSegmentIntersections(this, line);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		return CircleMath.nearestPointOutsideCircle(this, point);
	}
	
	
	@Override
	public List<IVector2> tangentialIntersections(final IVector2 externalPoint)
	{
		return CircleMath.tangentialIntersections(this, externalPoint);
	}
	
	
	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IEllipse projectToGround(final IVector3 origin, final double height)
	{
		if (origin.z() <= height)
		{
			throw new IllegalArgumentException("origin.z() must be above height");
		}
		
		IVector2 newCenter = Vector3.from2d(center(), height).projectToGroundNew(origin);
		
		double dist = center().distanceTo(origin.getXYVector());
		
		IVector2 projected = Vector3.fromXYZ(dist + radius(), radius(), height)
				.projectToGroundNew(Vector3.fromXYZ(0, 0, origin.z()));
		
		return Ellipse.createTurned(newCenter, projected.x() - dist, projected.y(),
				origin.getXYVector().subtractNew(newCenter).getAngle());
	}
}
