/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.Optional;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LineCollision implements ICollisionObject
{
	private final ILine		obstacleLine;
	private final IVector2	vel;
	private final IVector2	normal;
	
	
	/**
	 * @param obstacleLine
	 * @param vel
	 * @param normal
	 */
	public LineCollision(final ILine obstacleLine, final IVector2 vel, final IVector2 normal)
	{
		this.obstacleLine = obstacleLine;
		this.vel = vel;
		this.normal = normal;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		ILine stateLine = Line.newLine(prePos.getXYVector(), postPos.getXYVector());
		
		// pre and post identical
		IVector2 lp = GeoMath.leadPointOnLine(postPos.getXYVector(), obstacleLine);
		IVector2 lp2Pos = postPos.getXYVector().subtractNew(lp);
		if (lp2Pos.isZeroVector() || (GeoMath.angleBetweenVectorAndVector(lp2Pos, normal) < 0.1))
		{
			// point is outside
			return Optional.empty();
		}
		
		double dist2Line = lp2Pos.getLength();
		if (dist2Line > 50)
		{
			return Optional.empty();
		}
		
		IVector2 collisionPoint = lp;
		if (stateLine.directionVector().getLength() > 0.01)
		{
			try
			{
				collisionPoint = GeoMath.intersectionPoint(obstacleLine, stateLine);
			} catch (MathException e)
			{
			}
		}
		
		if (!obstacleLine.isPointOnLine(collisionPoint, 1))
		{
			return Optional.empty();
		}
		
		Collision collision = new Collision(collisionPoint, normal, getVel());
		return Optional.of(collision);
	}
}
