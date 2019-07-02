/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.ellipse.Ellipse;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * Implementations for {@link ICircle}
 */
@Persistent(version = 1)
public abstract class ACircle extends ACircular implements ICircle
{
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return CircleMath.isPointInCircle(this, point, margin);
	}
	
	
	@Override
	public List<IVector2> tangentialIntersections(final IVector2 externalPoint)
	{
		return CircleMath.tangentialIntersections(this, externalPoint);
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
