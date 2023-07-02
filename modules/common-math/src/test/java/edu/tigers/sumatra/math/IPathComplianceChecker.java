/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class IPathComplianceChecker
{

	private final IPath path;


	private IPathComplianceChecker(IPath path)
	{
		this.path = path;
	}


	public static void checkCompliance(IPath path)
	{
		var checker = new IPathComplianceChecker(path);
		checker.checkDistanceTo();
		checker.checkClosestPointOnPath();
	}


	private void checkDistanceTo()
	{
		for (var point : createPoints())
		{
			var distance = path.distanceTo(point);
			var tolerance = 1e-9 + 1e-9 * Math.abs(distance);
			assertThat(path.distanceToSqr(point)).isCloseTo(distance * distance, within(tolerance));
		}
	}


	private void checkClosestPointOnPath()
	{
		for (var point : createPoints())
		{
			var closestPoint = path.closestPointOnPath(point);
			var tolerance = 1e-10 + 1e-10 * Math.abs(point.getLength());
			assertThat(path.distanceTo(closestPoint)).isCloseTo(0, within(tolerance));
			assertThat(path.distanceToSqr(closestPoint)).isCloseTo(0, within(tolerance));
			assertThat(path.isPointOnPath(closestPoint)).isTrue();
		}
	}


	private List<IVector2> createPoints()
	{
		List<Double> components = List.of(
				-1_000_000.,
				-1_000.,
				-1.,
				-0.1,
				0.,
				0.1,
				1.,
				1_000.,
				1_000_000.
		);
		return components.stream()
				.map(e1 -> components.stream().map(e2 -> Vector2.fromXY(e1, e2)).toList())
				.flatMap(List::stream)
				.map(IVector2.class::cast)
				.toList();
	}
}
