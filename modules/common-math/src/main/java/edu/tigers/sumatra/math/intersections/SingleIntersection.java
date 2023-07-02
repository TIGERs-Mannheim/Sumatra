/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.intersections;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Value
public class SingleIntersection implements ISingleIntersection
{
	private static final ISingleIntersection EMPTY = new SingleIntersection(null);
	IVector2 point;


	private SingleIntersection(IVector2 point)
	{
		this.point = point;
	}


	public static ISingleIntersection empty()
	{
		return EMPTY;
	}


	public static ISingleIntersection of(IVector2 point)
	{
		if (point == null)
		{
			return EMPTY;
		}
		return new SingleIntersection(point);
	}


	@Override
	public Optional<IVector2> asOptional()
	{
		return isPresent() ? Optional.of(point) : Optional.empty();
	}


	@Override
	public boolean isPresent()
	{
		return point != null;
	}


	@Override
	public boolean isEmpty()
	{
		return point == null;
	}


	@Override
	public List<IVector2> asList()
	{
		return isPresent() ? List.of(point) : List.of();
	}


	@Override
	public Stream<IVector2> stream()
	{
		return isPresent() ? Stream.of(point) : Stream.of();
	}


	@Override
	public int size()
	{
		return isPresent() ? 1 : 0;
	}
}
