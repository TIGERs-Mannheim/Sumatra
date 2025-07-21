/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


@RequiredArgsConstructor
public class AcceptablePosFinder
{
	private final double distanceStepSize;
	private final int maxIterations;


	public AcceptablePos findAcceptablePos(
			IVector2 start,
			Predicate<IVector2> check,
			IVector2 reference
	)
	{
		if (check.test(start))
		{
			return new AcceptablePos(Optional.of(start), List.of(), List.of());
		}

		int iteration = 0;
		List<IVector2> possibleSolutions = new ArrayList<>();
		List<IVector2> triedPositions = new ArrayList<>();
		for (double radius = distanceStepSize; radius < 10_000; radius += distanceStepSize)
		{
			ICircle circle = Circle.createCircle(start, radius);
			double perimeterLength = circle.getPerimeterLength();
			for (double c = 0; c < perimeterLength; c += distanceStepSize)
			{
				IVector2 point = circle.stepAlongPath(c);
				triedPositions.add(point);
				if (check.test(point))
				{
					possibleSolutions.add(point);
				}
				iteration++;
				if (iteration >= maxIterations)
				{
					break;
				}
			}
			if (iteration >= maxIterations || !possibleSolutions.isEmpty())
			{
				return new AcceptablePos(
						possibleSolutions.stream().min(Comparator.comparingDouble(reference::distanceToSqr)),
						triedPositions,
						possibleSolutions
				);
			}
		}
		throw new IllegalStateException("No acceptable position found after " + iteration + " iterations.");
	}


	public record AcceptablePos(
			Optional<IVector2> pos,
			List<IVector2> triedPositions,
			List<IVector2> possibleSolutions
	)
	{
	}
}
