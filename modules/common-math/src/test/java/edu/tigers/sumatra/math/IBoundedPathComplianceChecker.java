/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


public class IBoundedPathComplianceChecker
{

	private final IBoundedPath path;
	private final boolean closedPath;


	private IBoundedPathComplianceChecker(IBoundedPath path, boolean closedPath)
	{
		this.path = path;
		this.closedPath = closedPath;
	}


	public static void checkCompliance(IBoundedPath path, boolean closedPath)
	{
		IPathComplianceChecker.checkCompliance(path);
		var checker = new IBoundedPathComplianceChecker(path, closedPath);
		checker.checkCloseness();
		checker.checkDistanceFromStart();
	}


	private void checkCloseness()
	{
		if (closedPath)
		{
			assertThat(path.getPathEnd()).isEqualTo(path.getPathStart());
		}
	}


	private void checkDistanceFromStart()
	{
		var totalLength = path.getLength();
		List<Double> lengths;
		if (closedPath)
		{
			lengths = Stream.of(0.001, 0.1, 0.25, 0.5, 0.75, 0.9, 0.999)
					.map(ratio -> ratio * totalLength)
					.toList();
		} else
		{
			lengths = Stream.of(0.0, 0.001, 0.1, 0.25, 0.5, 0.75, 0.9, 0.999, 1.0)
					.map(ratio -> ratio * totalLength)
					.toList();
		}
		for (var length : lengths)
		{
			var pos = path.stepAlongPath(length);
			assertThat(path.distanceFromStart(pos)).isCloseTo(length, within(1e-6));
		}
	}
}
