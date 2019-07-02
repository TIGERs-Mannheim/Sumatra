package edu.tigers.sumatra.sim.collision.ball;

import java.util.Optional;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * A collision object of a line segment, single or double sided
 */
public class LineSegmentCollisionObject implements ICollisionObject
{
	private final ILineSegment obstacleLine;
	private final IVector2 singleSidedNormal;
	
	
	public LineSegmentCollisionObject(final ILineSegment obstacleLine, final IVector2 singleSidedNormal)
	{
		this.obstacleLine = obstacleLine;
		this.singleSidedNormal = singleSidedNormal;
	}
	
	
	private IVector2 calcNormal(final IVector3 prePos)
	{
		if (singleSidedNormal != null && !singleSidedNormal.isZeroVector())
		{
			return singleSidedNormal;
		}
		IVector2 lp = obstacleLine.closestPointOnLine(prePos.getXYVector());
		return prePos.getXYVector().subtractNew(lp).normalize();
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		ILineSegment stateLine = Lines.segmentFromPoints(prePos.getXYVector(), postPos.getXYVector());
		
		IVector2 normal = calcNormal(prePos);
		if (stateLine.directionVector().isZeroVector())
		{
			if (obstacleLine.distanceTo(postPos.getXYVector()) < 0.001)
			{
				return Optional.of(new Collision(postPos.getXYVector(), normal, this));
			}
			return Optional.empty();
		}
		
		if (stateLine.directionVector().angleToAbs(normal).orElse(0.0) < AngleMath.PI_HALF)
		{
			// collision from wrong side
			return Optional.empty();
		}
		
		Optional<IVector2> collisionPoint = obstacleLine.intersectSegment(stateLine);
		if (!collisionPoint.isPresent())
		{
			return Optional.empty();
		}
		
		Collision collision = new Collision(collisionPoint.get(), normal, this);
		return Optional.of(collision);
	}
}
