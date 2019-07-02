package edu.tigers.sumatra.sim.collision.ball;

import java.util.Optional;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * A collision object in rectangular form
 */
public class RectCollisionObject implements ICollisionObject
{
	private final IRectangle rect;
	
	
	public RectCollisionObject(final IRectangle rect)
	{
		this.rect = rect;
	}
	
	
	private IVector2 calcNormal(final IVector3 prePos)
	{
		IVector2 pointOnBorder = rect.nearestPointInside(prePos.getXYVector());
		return prePos.getXYVector().subtractNew(pointOnBorder).normalize();
	}
	
	
	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		ILine stateLine = Line.fromPoints(prePos.getXYVector(), postPos.getXYVector());
		
		if (stateLine.directionVector().isZeroVector())
		{
			return Optional.empty();
		}
		
		for (ILine edge : rect.getEdges())
		{
			Optional<IVector2> collisionPoint = edge.intersectionOfSegments(stateLine);
			if (collisionPoint.isPresent())
			{
				return Optional.of(new Collision(collisionPoint.get(), calcNormal(prePos), this));
			}
		}
		
		return Optional.empty();
	}
}
