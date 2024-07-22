package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.intersections.ISingleIntersection;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;

import java.util.Comparator;
import java.util.Optional;


/**
 * A collision object in rectangular form
 */
public class RectCollisionObject implements ICollisionObject
{
	private final IRectangle rect;
	private final boolean isFieldBoundary;


	public RectCollisionObject(final IRectangle rect, boolean isFieldBoundary)
	{
		this.rect = rect;
		this.isFieldBoundary = isFieldBoundary;
	}


	private IVector2 calcNormal(final IVector3 prePos)
	{
		IVector2 pointOnBorder = rect.nearestPointInside(prePos.getXYVector());
		return prePos.getXYVector().subtractNew(pointOnBorder).normalize();
	}


	@Override
	public Optional<ICollision> getCollision(final IVector3 prePos, final IVector3 postPos)
	{
		var stateLine = Lines.segmentFromPoints(prePos.getXYVector(), postPos.getXYVector());

		if (stateLine.directionVector().isZeroVector())
		{
			return Optional.empty();
		}

		return rect.withMargin(Geometry.getBallRadius()).getEdges().stream()
				.map(stateLine::intersect)
				.map(ISingleIntersection::asOptional)
				.flatMap(Optional::stream)
				.min(Comparator.comparingDouble(pos -> prePos.getXYVector().distanceToSqr(pos)))
				.map(pos -> new Collision(pos, calcNormal(prePos), this));
	}


	@Override
	public boolean isFieldBoundary()
	{
		return isFieldBoundary;
	}
}
