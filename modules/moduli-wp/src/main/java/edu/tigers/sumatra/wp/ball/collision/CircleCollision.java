/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircleCollision implements ICollisionObject
{
	private final ICircle	circle;
	private final IVector3	vel;
	
	
	/**
	 * @param circle
	 * @param vel
	 */
	CircleCollision(final ICircle circle, final IVector3 vel)
	{
		this.circle = circle;
		this.vel = vel;
	}
	
	
	@Override
	public IVector3 getVel()
	{
		return vel;
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		ILine stateLine = Line.fromPoints(prePos.getXYVector(), postPos.getXYVector());
		List<IVector2> points = circle.lineSegmentIntersections(stateLine);
		if (!points.isEmpty())
		{
			IVector2 collisionPoint = stateLine.supportVector().nearestTo(points);
			IVector2 normal = getNormal(collisionPoint);
			Collision collision = new Collision(collisionPoint, normal, this);
			return Optional.of(collision);
		}
		return Optional.empty();
	}
	
	
	@Override
	public Optional<ICollision> getInsideCollision(final IVector3 pos)
	{
		if (circle.isPointInShape(pos.getXYVector()))
		{
			IVector2 nearestOutside = circle.nearestPointOutside(pos.getXYVector());
			IVector2 normal = getNormal(nearestOutside);
			Collision collision = new Collision(nearestOutside, normal, this);
			return Optional.of(collision);
		}
		return Optional.empty();
	}
	
	
	private IVector2 getNormal(IVector2 point)
	{
		IVector2 normal = point.subtractNew(circle.center());
		if (normal.isZeroVector())
		{
			normal = Vector2.fromX(1);
		}
		return normal;
	}
	
	
	/**
	 * @return the circle
	 */
	public ICircle getCircle()
	{
		return circle;
	}
	
}
