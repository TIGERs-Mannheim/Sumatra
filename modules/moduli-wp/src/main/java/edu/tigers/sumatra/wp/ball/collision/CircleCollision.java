/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball.collision;

import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.shapes.circle.ICircle;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircleCollision implements ICollisionObject
{
	private final ICircle	circle;
	private final IVector2	vel;
	
	
	/**
	 * @param circle
	 * @param vel
	 */
	public CircleCollision(final ICircle circle, final IVector2 vel)
	{
		this.circle = circle;
		this.vel = vel;
	}
	
	
	@Override
	public IVector2 getVel()
	{
		return vel;
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		if (!circle.isPointInShape(prePos.getXYVector(), -1) && !circle.isPointInShape(postPos.getXYVector(), -1))
		{
			return Optional.empty();
		}
		
		ILine stateLine = Line.newLine(prePos.getXYVector(), postPos.getXYVector());
		
		IVector2 collisionPoint;
		if (stateLine.directionVector().isZeroVector() && circle.isPointInShape(prePos.getXYVector()))
		{
			// pre and post pos are identical, but inside circle.
			collisionPoint = circle.nearestPointOutside(prePos.getXYVector());
		} else
		{
			List<IVector2> intersections = circle.lineIntersections(stateLine);
			if (intersections.isEmpty())
			{
				return Optional.empty();
			} else if (intersections.size() == 1)
			{
				collisionPoint = intersections.get(0);
			} else
			{
				double dist1 = GeoMath.distancePP(stateLine.supportVector(), intersections.get(0));
				double dist2 = GeoMath.distancePP(stateLine.supportVector(), intersections.get(1));
				if (dist1 < dist2)
				{
					collisionPoint = intersections.get(0);
				} else
				{
					collisionPoint = intersections.get(1);
				}
			}
		}
		
		IVector2 normal = collisionPoint.subtractNew(circle.center());
		Collision collision = new Collision(collisionPoint, normal, getVel());
		return Optional.of(collision);
	}
	
	
	/**
	 * @return the circle
	 */
	public ICircle getCircle()
	{
		return circle;
	}
	
}
