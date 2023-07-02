/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class AngleRangeGeneratorTest
{
	private AngleRangeGenerator processor;


	@Before
	public void before()
	{
		processor = AngleRangeGenerator.forLineSegment(Lines.segmentFromPoints(
				Vector2.fromXY(2000, 500),
				Vector2.fromXY(2000, -500)
		));
	}


	@Test
	public void processOneBotInside()
	{
		var obstacles = List.of(
				Circle.createCircle(Vector2.fromXY(1000, 0), 150)
		);
		var start = Vector2.zero();

		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges(start, obstacles);
		assertThat(coveredAngles).hasSize(1);
		assertThat(coveredAngles.get(0).getLeft()).isBetween(0.0, 0.2);
		assertThat(coveredAngles.get(0).getRight()).isBetween(-0.2, 0.0);
	}


	@Test
	public void processTwoBotsInside()
	{
		var obstacles = List.of(
				Circle.createCircle(Vector2.fromXY(1000, 0), 150),
				Circle.createCircle(Vector2.fromXY(1500, 300), 200)
		);
		var start = Vector2.zero();

		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges(start, obstacles);
		assertThat(coveredAngles).hasSize(2);
		assertThat(coveredAngles.get(0).getLeft()).isBetween(0.0, 0.4);
		assertThat(coveredAngles.get(0).getRight()).isBetween(-0.4, 0.0);
		assertThat(coveredAngles.get(1).getLeft()).isBetween(0.3, 0.4);
		assertThat(coveredAngles.get(1).getRight()).isBetween(0.0, 0.2);
	}


	@Test
	public void processOneBotCoveringAll()
	{
		var obstacles = List.of(
				Circle.createCircle(Vector2.fromXY(1000, 0), 800)
		);
		var start = Vector2.zero();

		List<AngleRange> coveredAngles = processor.findCoveredAngleRanges(start, obstacles);
		assertThat(coveredAngles).hasSize(1);
	}
}
