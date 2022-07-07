package edu.tigers.sumatra.skillsystem.skills.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * The rectangular boundary of the penalty area.
 */
public class PenAreaBoundary implements Comparator<IVector2>
{
	private final double width;
	private final double depth;
	private final double sign;

	private final IVector2 start;
	private final IVector2 corner1;
	private final IVector2 corner2;
	private final IVector2 end;
	private final List<ILineSegment> edges = new ArrayList<>();


	private PenAreaBoundary(final double width, final double depth, final double sign)
	{
		this.width = width;
		this.depth = depth;
		this.sign = sign;

		start = Vector2.fromXY(Geometry.getFieldLength() / 2, width / 2).multiply(sign);
		corner1 = Vector2.fromXY(Geometry.getFieldLength() / 2 - depth, width / 2).multiply(sign);
		corner2 = Vector2.fromXY(Geometry.getFieldLength() / 2 - depth, -width / 2).multiply(sign);
		end = Vector2.fromXY(Geometry.getFieldLength() / 2, -width / 2).multiply(sign);

		edges.add(Lines.segmentFromPoints(start, corner1));
		edges.add(Lines.segmentFromPoints(corner1, corner2));
		edges.add(Lines.segmentFromPoints(corner2, end));
	}


	public static PenAreaBoundary ownWithMargin(final double margin)
	{
		return new PenAreaBoundary(
				Geometry.getPenaltyAreaWidth() + margin * 2,
				Geometry.getPenaltyAreaDepth() + margin,
				-1.0);
	}


	public PenAreaBoundary withMargin(final double margin)
	{
		return new PenAreaBoundary(width + margin * 2, depth + margin, sign);
	}


	public IVector2 closestPoint(final IVector2 p)
	{
		return edges.stream().min(Comparator.comparingDouble(l -> l.distanceTo(p)))
				.map(l -> l.closestPointOnLine(p)).orElseThrow(IllegalStateException::new);
	}


	public IVector2 projectPoint(final IVector2 p)
	{
		return projectPoint(Geometry.getGoalTheir().getCenter().multiplyNew(sign), p);
	}


	public IVector2 projectPoint(final IVector2 p1, final IVector2 p2)
	{
		IHalfLine line = Lines.halfLineFromPoints(p1, p2);
		return edges.stream()
				.map(e -> e.intersectHalfLine(line))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseGet(() -> closestPoint(p2));
	}


	public Optional<IVector2> nextTo(final IVector2 p, final double margin, final int direction)
	{
		// find the current edge
		final ILineSegment currentEdge = edges.stream().filter(l -> l.isPointOnLine(p)).findAny().orElse(null);
		if (currentEdge == null)
		{
			// point is not on boundary
			return Optional.empty();
		}
		// determine next point on current edge
		double baseDistance = edgeStartByDirection(currentEdge, direction).distanceTo(p);
		IVector2 np = stepAlongEdge(currentEdge, baseDistance + margin, direction);
		// Check if the next point is still on the current edge
		if (currentEdge.isPointOnLine(np))
		{
			return Optional.of(np);
		}

		// determine the remaining distance left on the current edge
		double remainingDistanceOnCurrentEdge = p.distanceTo(currentEdge.closestPointOnLine(np));
		// determine the additional distance from the start of the next edge so that the point is still `margin` away
		// assumption: that the edges are orthogonal to each other and connected
		double distanceOnNextEdge = SumatraMath
				.sqrt(margin * margin - remainingDistanceOnCurrentEdge * remainingDistanceOnCurrentEdge);
		// get the next edge, if present and step to the required point on that edge
		// assumption: margin is small enough so that the new point is definitely still on the next edge
		Optional<ILineSegment> nextEdge = nextEdge(currentEdge, direction);
		return nextEdge.map(e -> stepAlongEdge(e, distanceOnNextEdge, direction));
	}


	private IVector2 edgeStartByDirection(final ILineSegment edge, final int direction)
	{
		if (direction > 0)
		{
			return edge.getStart();
		}
		return edge.getEnd();
	}


	private IVector2 stepAlongEdge(final ILineSegment edge, final double distance, final int direction)
	{
		return LineMath.stepAlongLine(
				edgeStartByDirection(edge, direction),
				edgeStartByDirection(edge, -direction),
				distance);
	}


	public Optional<IVector2> nextIntermediateCorner(final IVector2 pFrom, final IVector2 pTo)
	{
		ILineSegment edgeFrom = edgeOf(pFrom);
		ILineSegment edgeTo = edgeOf(pTo);
		if (edgeFrom == edgeTo)
		{
			return Optional.empty();
		}

		boolean reverse = false;
		for (ILineSegment edge : edges)
		{
			if (edgeFrom == edge)
			{
				if (reverse)
				{
					return Optional.of(edgeFrom.getStart());
				}
				return Optional.of(edgeFrom.getEnd());
			} else if (edgeTo == edge)
			{
				reverse = true;
			}
		}
		throw new IllegalStateException("Could not find all points on edges");
	}


	public double distanceBetween(final IVector2 p1, final IVector2 p2)
	{
		ILineSegment edge1 = edgeOf(p1);
		ILineSegment edge2 = edgeOf(p2);
		if (edge1 == edge2)
		{
			return p1.distanceTo(p2);
		}

		boolean started = false;
		double distance = 0;
		for (ILineSegment edge : edges)
		{
			if (started)
			{
				if (edge1 == edge)
				{
					distance += edge1.getStart().distanceTo(p1);
					return distance;
				} else if (edge2 == edge)
				{
					distance += edge2.getStart().distanceTo(p2);
					return distance;
				} else
				{
					distance += edge.getLength();
				}
			} else
			{
				if (edge1 == edge)
				{
					distance += edge1.getEnd().distanceTo(p1);
					started = true;
				} else if (edge2 == edge)
				{
					distance += edge2.getEnd().distanceTo(p2);
					started = true;
				}
			}
		}
		throw new IllegalStateException("Could not find all points on edges");
	}


	private ILineSegment edgeOf(final IVector2 p)
	{
		return edges.stream().min(Comparator.comparingDouble(l -> l.distanceTo(p)))
				.orElseThrow(IllegalStateException::new);
	}


	private Optional<ILineSegment> nextEdge(final ILineSegment currentEdge, final int direction)
	{
		for (int i = 0; i < edges.size(); i++)
		{
			if (edges.get(i) == currentEdge)
			{
				int j = i + direction;
				if (j >= 0 && j < edges.size())
				{
					return Optional.of(edges.get(j));
				}
				break;
			}
		}
		return Optional.empty();
	}


	@Override
	public int compare(IVector2 p1, IVector2 p2)
	{
		final IVector2 cp1 = projectPoint(p1);
		final IVector2 cp2 = projectPoint(p2);
		ILineSegment edge1 = edgeOf(cp1);
		ILineSegment edge2 = edgeOf(cp2);
		if (edge1 == edge2)
		{
			double distance1 = edge1.getStart().distanceToSqr(cp1);
			double distance2 = edge2.getStart().distanceToSqr(cp2);
			if (distance1 < distance2)
			{
				return -1;
			} else if (distance1 > distance2)
			{
				return 1;
			}
			return 0;
		}
		for (ILineSegment edge : edges)
		{
			if (edge1 == edge)
			{
				return -1;
			}
			if (edge2 == edge)
			{
				return 1;
			}
		}
		return 0;
	}


	public Optional<IVector2> stepAlongBoundary(final IVector2 start, final double distance)
	{
		if (distance < 0)
		{
			return Optional.empty();
		}
		ILineSegment edge = edgeOf(start);
		return stepAlongBoundary(start, edge, distance);
	}


	private Optional<IVector2> stepAlongBoundary(final IVector2 start, final ILineSegment edge, final double distance)
	{
		double remainingDist = edge.getEnd().distanceTo(start);
		if (remainingDist > distance)
		{
			return Optional.of(LineMath.stepAlongLine(start, edge.getEnd(), distance));
		}
		return nextEdge(edge, 1)
				.map(e -> stepAlongBoundary(e.getStart(), e, distance - remainingDist))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}


	/**
	 * Check if the given point is inside the boundary
	 *
	 * @param point
	 * @return
	 */
	public boolean isPointInShape(IVector2 point)
	{
		return Rectangle.fromPoints(start, corner2).isPointInShape(point);
	}


	public double getWidth()
	{
		return width;
	}


	public double getDepth()
	{
		return depth;
	}


	public IVector2 getStart()
	{
		return start;
	}


	public IVector2 getCorner1()
	{
		return corner1;
	}


	public IVector2 getCorner2()
	{
		return corner2;
	}


	public IVector2 getEnd()
	{
		return end;
	}


	List<ILineSegment> getEdges()
	{
		return Collections.unmodifiableList(edges);
	}


	public IVector2 getCenter()
	{
		return Vector2.fromX(corner1.x());
	}
}
