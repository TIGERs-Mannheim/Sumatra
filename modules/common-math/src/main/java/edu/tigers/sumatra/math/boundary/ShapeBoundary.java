/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.boundary;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Value
public class ShapeBoundary implements IShapeBoundary
{
	I2DShape shape;

	IVector2 start;
	IVector2 end;
	@Getter(AccessLevel.PRIVATE)
	List<IBoundedPath> edges;


	public ShapeBoundary(I2DShape shape)
	{
		this.shape = shape;
		edges = shape.getPerimeterPath();
		start = edges.get(0).getPathStart();
		end = edges.get(edges.size() - 1).getPathEnd();
	}


	@Override
	public boolean isPointInShape(IVector2 point)
	{
		return shape.isPointInShape(point);
	}


	@Override
	public IShapeBoundary withMargin(final double margin)
	{
		return new ShapeBoundary(shape.withMargin(margin));
	}


	@Override
	public IVector2 closestPoint(IVector2 p)
	{
		return shape.nearestPointOnPerimeterPath(p);
	}


	@Override
	public IVector2 projectPoint(final IVector2 pStart, final IVector2 pEnd)
	{
		IHalfLine line = Lines.halfLineFromPoints(pStart, pEnd);
		return edges.stream()
				.map(e -> e.intersect(line))
				.flatMap(IIntersections::stream)
				.findFirst()
				.orElseGet(() -> closestPoint(pEnd));
	}


	@Override
	public Optional<IVector2> nextIntermediateCorner(final IVector2 pFrom, final IVector2 pTo)
	{
		var edgeFrom = edgeOf(pFrom);
		var edgeTo = edgeOf(pTo);
		if (edgeFrom == edgeTo)
		{
			return Optional.empty();
		}

		boolean reverse = false;
		for (var edge : edges)
		{
			if (edgeFrom == edge)
			{
				if (reverse)
				{
					return Optional.of(edgeFrom.getPathStart());
				}
				return Optional.of(edgeFrom.getPathEnd());
			} else if (edgeTo == edge)
			{
				reverse = true;
			}
		}
		throw new IllegalStateException("Could not find all points on edges");
	}


	@Override
	public double distanceFromStart(final IVector2 p)
	{
		return distanceBetween(start, p);
	}


	@Override
	public double distanceFromEnd(final IVector2 p)
	{
		return distanceBetween(end, p);
	}


	@Override
	public double distanceBetween(final IVector2 p1, final IVector2 p2)
	{
		var edge1 = edgeOf(p1);
		var edge2 = edgeOf(p2);
		if (edge1 == edge2)
		{
			return p1.distanceTo(p2);
		}

		boolean started = false;
		double distance = 0;
		for (var edge : edges)
		{
			if (started)
			{
				if (edge1 == edge)
				{
					distance += edge1.getPathStart().distanceTo(p1);
					return distance;
				} else if (edge2 == edge)
				{
					distance += edge2.getPathStart().distanceTo(p2);
					return distance;
				} else
				{
					distance += edge.getLength();
				}
			} else
			{
				if (edge1 == edge)
				{
					distance += edge1.getPathEnd().distanceTo(p1);
					started = true;
				} else if (edge2 == edge)
				{
					distance += edge2.getPathEnd().distanceTo(p2);
					started = true;
				}
			}
		}
		throw new IllegalStateException("Could not find all points on edges");
	}


	/**
	 * Implementation of the {@code Comparator<IVector2>} interface to allow to sort positions along the shapes boundary
	 *
	 * @param p1 the first object to be compared.
	 * @param p2 the second object to be compared.
	 * @return
	 */
	@Override
	public int compare(IVector2 p1, IVector2 p2)
	{
		final IVector2 cp1 = closestPoint(p1);
		final IVector2 cp2 = closestPoint(p2);
		var edge1 = edgeOf(cp1);
		var edge2 = edgeOf(cp2);
		if (edge1 == edge2)
		{
			double distance1 = edge1.getPathStart().distanceToSqr(cp1);
			double distance2 = edge2.getPathStart().distanceToSqr(cp2);
			if (distance1 < distance2)
			{
				return -1;
			} else if (distance1 > distance2)
			{
				return 1;
			}
			return 0;
		}
		for (var edge : edges)
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


	@Override
	public Optional<IVector2> stepAlongBoundary(double distance)
	{
		return stepAlongBoundary(start, distance);
	}


	@Override
	public Optional<IVector2> stepAlongBoundary(final IVector2 start, final double distance)
	{
		if (distance < 0)
		{
			return Optional.empty();
		}
		var edge = edgeOf(start);
		return stepAlongBoundary(start, edge, distance);
	}


	private Optional<IVector2> stepAlongBoundary(final IVector2 start, final IBoundedPath edge, final double distance)
	{
		double remainingDist = edge.getLength();
		if (remainingDist + 1e-6 >= distance)
		{
			return Optional.of(edge.stepAlongPath(distance + edge.distanceFromStart(start)));
		}
		return nextEdge(edge)
				.map(e -> stepAlongBoundary(e.getPathStart(), e, distance - remainingDist))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}


	private IBoundedPath edgeOf(IVector2 p)
	{
		var directlyOn = edges.stream().filter(l -> l.isPointOnPath(p)).count();
		if (directlyOn > 1)
		{
			return edges.stream().min(Comparator.comparing(l -> l.getPathStart().distanceTo(p)))
					.orElseThrow(IllegalStateException::new);
		}
		return edges.stream().min(Comparator.comparingDouble(l -> l.distanceTo(p)))
				.orElseThrow(IllegalStateException::new);
	}


	private Optional<IBoundedPath> nextEdge(final IBoundedPath currentEdge)
	{
		for (int i = 0; i < edges.size(); i++)
		{
			if (edges.get(i) == currentEdge)
			{
				int j = i + 1;
				if (j < edges.size())
				{
					return Optional.of(edges.get(j));
				}
				break;
			}
		}
		return Optional.empty();
	}
}
