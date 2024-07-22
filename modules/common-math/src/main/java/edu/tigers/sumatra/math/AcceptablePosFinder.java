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
import java.util.Optional;
import java.util.function.Predicate;


@RequiredArgsConstructor
public class AcceptablePosFinder
{
	private final double distanceStepSize;
	private final int maxIterations;


	public Optional<IVector2> findAcceptablePos(IVector2 start, Predicate<IVector2> check)
	{
		if (check.test(start))
		{
			return Optional.of(start);
		}

		int iteration = 0;
		for (double radius = distanceStepSize; radius < 10_000; radius += distanceStepSize)
		{
			ICircle circle = Circle.createCircle(start, radius);
			double perimeterLength = circle.getPerimeterLength();
			for (double c = 0; c < perimeterLength; c += distanceStepSize)
			{
				IVector2 point = circle.stepAlongPath(c);
				if (check.test(point))
				{
					return Optional.of(point);
				}
				iteration++;
				if (iteration >= maxIterations)
				{
					return Optional.empty();
				}
			}
		}
		throw new IllegalStateException("No acceptable position found after " + iteration + " iterations.");
	}


	public Optional<IVector2> findAcceptablePosWithReference(IVector2 start, Predicate<IVector2> check,
			IVector2 reference)
	{
		if (check.test(start))
		{
			return Optional.of(start);
		}

		int iteration = 0;
		ArrayList<IVector2> possibleSolutions = new ArrayList<>();
		for (double radius = distanceStepSize; radius < 10_000; radius += distanceStepSize)
		{
			ICircle circle = Circle.createCircle(start, radius);
			double perimeterLength = circle.getPerimeterLength();
			for (double c = 0; c < perimeterLength; c += distanceStepSize)
			{
				IVector2 point = circle.stepAlongPath(c);
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
				return possibleSolutions.stream().min(Comparator.comparingDouble(reference::distanceToSqr));
			}
		}
		throw new IllegalStateException("No acceptable position found after " + iteration + " iterations.");
	}
}
