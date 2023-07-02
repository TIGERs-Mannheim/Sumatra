/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import edu.tigers.sumatra.math.SumatraMath;

import java.util.ArrayList;
import java.util.function.Predicate;


public class VectorDistinctStreamFilter
{
	private VectorDistinctStreamFilter()
	{
	}


	public static Predicate<IVector> byIsCloseTo()
	{
		return byIsCloseTo(SumatraMath.getEqualTol());

	}


	public static Predicate<IVector> byIsCloseTo(double margin)
	{
		var seen = new ArrayList<IVector>();
		return t -> {
			if (seen.stream().anyMatch(s -> s.isCloseTo(t, margin)))
			{
				return false;
			}
			seen.add(t);
			return true;
		};
	}
}
