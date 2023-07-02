/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.intersections;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


@Value
public class Intersections implements IIntersections
{
	List<IVector2> points;


	private Intersections(List<IVector2> points)
	{
		this.points = points;
	}


	public static Intersections of()
	{
		return new Intersections(List.of());
	}


	public static Intersections of(List<IVector2> points)
	{
		return new Intersections(points);
	}


	public static Intersections of(IVector2... points)
	{
		return new Intersections(List.of(points));
	}


	@Override
	public List<IVector2> asList()
	{
		return Collections.unmodifiableList(points);
	}


	@Override
	public Stream<IVector2> stream()
	{
		return points.stream();
	}


	@Override
	public boolean isEmpty()
	{
		return points.isEmpty();
	}


	@Override
	public int size()
	{
		return points.size();
	}
}
