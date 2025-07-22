/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IPathComplianceChecker;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Lukas Magel
 */
public class LinesTest
{

	@Test
	public void testSegmentFromPoints()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);

		ILineSegment lineA = LineSegment.fromPoints(a, b);
		ILineSegment lineB = Lines.segmentFromPoints(a, b);
		assertThat(lineA).isEqualTo(lineB);
	}


	@Test
	public void testSegmentFromOffset()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);

		ILineSegment lineA = LineSegment.fromOffset(a, b);
		ILineSegment lineB = Lines.segmentFromOffset(a, b);
		assertThat(lineA).isEqualTo(lineB);
	}


	@Test
	public void testHalfLineFromDirection()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);

		IHalfLine lineA = HalfLine.fromDirection(a, b);
		IHalfLine lineB = Lines.halfLineFromDirection(a, b);
		assertThat(lineA).isEqualTo(lineB);
	}


	@Test
	public void testLineFromPoints()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);

		ILine lineA = Line.fromPoints(a, b);
		ILine lineB = Lines.lineFromPoints(a, b);
		assertThat(lineA).isEqualTo(lineB);
	}


	@Test
	public void testLineFromDirection()
	{
		IVector2 a = Vector2.fromXY(1, 2);
		IVector2 b = Vector2.fromXY(6, 3);

		ILine lineA = Line.fromDirection(a, b);
		ILine lineB = Lines.lineFromDirection(a, b);
		assertThat(lineA).isEqualTo(lineB);
	}


	@Test
	public void testCompliance()
	{
		var lines = List.of(
				Lines.lineFromPoints(Vector2.fromXY(1, 2), Vector2.fromXY(6, 3)),
				Lines.lineFromPoints(Vector2.fromXY(1, 2), Vector2.fromXY(3, 6)),
				Lines.lineFromPoints(Vector2.zero(), Vector2.fromXY(1, 0)),
				Lines.lineFromPoints(Vector2.zero(), Vector2.fromXY(0, 1))
		);
		for (var line : lines)
		{
			IPathComplianceChecker.checkCompliance(line);
		}
	}

}
