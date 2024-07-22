/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;

import java.util.Optional;


/**
 * A collision object of a line segment, single or double sided
 */
public class LineSegmentCollisionObject implements ICollisionObject
{
	protected final ILineSegment obstacleLine;
	private final IVector2 singleSidedNormal;

	private final boolean isFieldBoundary;


	public LineSegmentCollisionObject(ILineSegment obstacleLine, IVector2 singleSidedNormal, boolean isFieldBoundary)
	{
		this.obstacleLine = obstacleLine;
		this.singleSidedNormal = singleSidedNormal;
		this.isFieldBoundary = isFieldBoundary;
	}


	private IVector2 calcNormal(final IVector3 prePos)
	{
		if (singleSidedNormal != null && !singleSidedNormal.isZeroVector())
		{
			return singleSidedNormal;
		}
		IVector2 lp = obstacleLine.closestPointOnPath(prePos.getXYVector());
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

		Optional<IVector2> collisionPoint = obstacleLine.intersect(stateLine).asOptional();
		if (collisionPoint.isEmpty())
		{
			return Optional.empty();
		}

		Collision collision = new Collision(collisionPoint.get(), normal, this);
		return Optional.of(collision);
	}


	@Override
	public boolean isFieldBoundary()
	{
		return isFieldBoundary;
	}
}
