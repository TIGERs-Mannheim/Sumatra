/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SingleSidedLineCollision implements ICollisionObject
{
	private final ILineSegment obstacleLine;
	private final IVector3	vel;
	private final IVector2	normal;
	private IVector3			impulse	= Vector3.ZERO_VECTOR;
	private boolean			sticky;
	
	
	/**
	 * @param obstacleLine
	 * @param vel
	 * @param normal
	 */
	public SingleSidedLineCollision(final ILine obstacleLine, final IVector3 vel, final IVector2 normal)
	{
		this.obstacleLine = Lines.segmentFromPoints(obstacleLine.getStart(), obstacleLine.getEnd());
		this.vel = vel;
		this.normal = normal;
	}
	
	
	@Override
	public IVector3 getImpulse(final IVector3 prePos)
	{
		if (prePos.z() < 1)
		{
			return impulse;
		}
		return Vector3.from2d(impulse.getXYVector(), 0);
	}
	
	
	public void setImpulse(final IVector3 impulse)
	{
		this.impulse = impulse;
	}
	
	
	@Override
	public boolean isSticky()
	{
		return sticky;
	}
	
	
	public void setSticky(final boolean sticky)
	{
		this.sticky = sticky;
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		ILineSegment stateLine = Lines.segmentFromPoints(prePos.getXYVector(), postPos.getXYVector());
		
		if (stateLine.directionVector().isZeroVector())
		{
			if (obstacleLine.distanceTo(postPos.getXYVector()) < 1)
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
	
	
	@Override
	public Optional<ICollision> getInsideCollision(final IVector3 pos)
	{
		return Optional.empty();
	}
}
