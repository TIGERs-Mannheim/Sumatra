/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TubeTest
{
	private IVector2 startCenter = Vector2.zero();
	private IVector2 endCenter = Vector2.fromXY(1000, 0);
	private double radius = 10;


	@Test
	void testIsPointInShape()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);

		assertTrue(tube.isPointInShape(startCenter));
		assertTrue(tube.isPointInShape(endCenter));
		assertTrue(tube.isPointInShape(Vector2.fromXY(-9, 0)));
		assertTrue(tube.isPointInShape(Vector2.fromXY(1009, 0)));
		assertTrue(tube.isPointInShape(Vector2.fromXY(0, 9)));
		assertTrue(tube.isPointInShape(Vector2.fromXY(1000, -9)));
		assertTrue((tube.isPointInShape(Vector2.fromXY(500, 0))));
		assertTrue(tube.isPointInShape(Vector2.fromXY(800, 9)));

		assertTrue(tubeZeroLength.isPointInShape(startCenter));
		assertTrue(tubeZeroLength.isPointInShape(Vector2.fromXY(-9, 0)));
		assertTrue(tubeZeroLength.isPointInShape(Vector2.fromXY(0, 9)));

		assertFalse(tube.isPointInShape(Vector2.fromXY(-11, 0)));
		assertFalse(tube.isPointInShape(Vector2.fromXY(0, -11)));
		assertFalse(tube.isPointInShape(Vector2.fromXY(-1000, 0)));
		assertFalse(tube.isPointInShape(Vector2.fromXY(500, 11)));
		assertFalse(tube.isPointInShape(Vector2.fromXY(1000, 11)));
		assertFalse(tube.isPointInShape(Vector2.fromXY(1011, 0)));

		assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(-11, 0)));
		assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(0, -11)));
		assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(11, 0)));
		assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(0, 11)));
	}


	@Test
	void testIsIntersectingWithLine()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);

		IVector2 p1 = Vector2.fromXY(0, 20);
		IVector2 p2 = Vector2.fromXY(0, -20);
		IVector2 p3 = Vector2.fromXY(1000, 20);
		IVector2 p4 = Vector2.fromXY(500, -20);
		IVector2 p5 = Vector2.fromXY(-20, -20);
		IVector2 p6 = Vector2.fromXY(-10, 10);
		IVector2 p7 = Vector2.fromXY(-10, -10);
		IVector2 p8 = Vector2.fromXY(1000, 10);

		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p1, p2)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p2, p3)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p1, p5)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p4, p3)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p6, p7)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p6, p8)));
		assertTrue(tube.isIntersectingWithPath(Lines.lineFromPoints(p1, p8)));

		assertTrue(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p1, p2)));
		assertTrue(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p1, p5)));
		assertTrue(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p6, p7)));
		assertTrue(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p6, p8)));

		assertFalse(tube.isIntersectingWithPath(Lines.lineFromPoints(p1, p3)));
		assertFalse(tube.isIntersectingWithPath(Lines.lineFromPoints(p5, p4)));

		assertFalse(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p1, p3)));
		assertFalse(tubeZeroLength.isIntersectingWithPath(Lines.lineFromPoints(p8, p3)));
	}


	@Test
	void testNearestPointOutside()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);

		IVector2 pInside = Vector2.fromXY(0, 1);
		IVector2 nearestPointOutside = Vector2.fromXY(0, 10);

		assertEquals(tube.nearestPointOutside(pInside), nearestPointOutside);
		assertEquals(tube.nearestPointOutside(nearestPointOutside), nearestPointOutside);
		assertEquals(tubeZeroLength.nearestPointOutside(pInside), nearestPointOutside);
	}


	@Test
	void testNearestPointInside()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);

		IVector2 nearestPointInside = Vector2.fromXY(0, 10);
		IVector2 pOutside = Vector2.fromXY(0, 11);

		assertEquals(tube.nearestPointInside(pOutside), nearestPointInside);
		assertEquals(tube.nearestPointInside(nearestPointInside), nearestPointInside);
		assertEquals(tubeZeroLength.nearestPointInside(pOutside), nearestPointInside);
	}


	@Test
	void testLineIntersections()
	{
		final Vector2 start = Vector2.fromXY(1, 5);
		final Vector2 end = Vector2.fromXY(-2, 5);
		ITube tube = Tube.create(start, end, 10);

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(start, Vector2.fromXY(12, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(11, 5));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(start, Vector2.fromXY(-13, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-12, 5));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(start, Vector2.fromXY(1, 16))))
				.containsExactlyInAnyOrder(Vector2.fromXY(1, 15));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(start, Vector2.fromXY(1, -6))))
				.containsExactlyInAnyOrder(Vector2.fromXY(1, -5));

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(end, Vector2.fromXY(-13, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-12, 5));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(end, Vector2.fromXY(12, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(11, 5));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(end, Vector2.fromXY(-2, 16))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, 15));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(end, Vector2.fromXY(-2, -6))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, -5));

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(1, 5), Vector2.fromXY(-1, 25))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, 15));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(1, 5), Vector2.fromXY(-1, -15))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, -5));

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(0, -100), Vector2.fromXY(0, 100))))
				.containsExactlyInAnyOrder(Vector2.fromXY(0, -5), Vector2.fromXY(0, 15));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(1, -100), Vector2.fromXY(1, 100))))
				.containsExactlyInAnyOrder(Vector2.fromXY(1, -5), Vector2.fromXY(1, 15));
		assertThat(
				tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(-2, -100), Vector2.fromXY(-2, 100))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, -5), Vector2.fromXY(-2, 15));

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(-100, 5), Vector2.fromXY(100, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(11, 5), Vector2.fromXY(-12, 5));

		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(-2, -100), Vector2.fromXY(-2, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, -5));
		assertThat(tube.intersectPerimeterPath(Lines.segmentFromPoints(Vector2.fromXY(-2, 5), Vector2.fromXY(-2, 100))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, 15));

		assertThat(tube.intersectPerimeterPath(Lines.lineFromPoints(Vector2.fromXY(-2, -100), Vector2.fromXY(-2, 5))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, -5), Vector2.fromXY(-2, 15));
		assertThat(tube.intersectPerimeterPath(Lines.lineFromPoints(Vector2.fromXY(-2, 5), Vector2.fromXY(-2, 100))))
				.containsExactlyInAnyOrder(Vector2.fromXY(-2, -5), Vector2.fromXY(-2, 15));
	}


	private ITube buildTube()
	{
		return Tube.create(Vector2.fromXY(-10, 4), Vector2.fromXY(10, 4), 4);
	}


	@Test
	void testWithMargin()
	{
		var tube = buildTube();
		var margin = 1;
		var withMargin = tube.withMargin(margin);
		assertThat(withMargin.startCenter()).isEqualTo(tube.startCenter());
		assertThat(withMargin.endCenter()).isEqualTo(tube.endCenter());
		assertThat(withMargin.center()).isEqualTo(tube.center());
		assertThat(withMargin.radius()).isCloseTo(tube.radius() + margin, within(1e-10));

		margin = -1;
		withMargin = tube.withMargin(margin);
		assertThat(withMargin.startCenter()).isEqualTo(tube.startCenter());
		assertThat(withMargin.endCenter()).isEqualTo(tube.endCenter());
		assertThat(withMargin.center()).isEqualTo(tube.center());
		assertThat(withMargin.radius()).isCloseTo(tube.radius() + margin, within(1e-10));

	}


	@Test
	void testGetPerimeterPath()
	{
		var corners = List.of(
				Vector2.fromXY(-10, 8),
				Vector2.fromXY(-10, 0),
				Vector2.fromXY(10, 0),
				Vector2.fromXY(10, 8)
		);

		var perimeter = buildTube().getPerimeterPath();
		assertThat(perimeter).hasSize(4);
		for (var corner : corners)
		{
			assertThat(perimeter).anyMatch(p -> p.getPathEnd().equals(corner));
			assertThat(perimeter).anyMatch(p -> p.getPathStart().equals(corner));
		}

	}


	@Test
	void testPerimeterPathOrder()
	{
		var perimeter = buildTube().getPerimeterPath();
		IBoundedPath lastPath = null;
		for (var p : perimeter)
		{
			if (lastPath != null)
			{
				assertThat(p.getPathStart()).isEqualTo(p.getPathStart());
			}
			lastPath = p;
		}
	}


	@Test
	void testGetPerimeterLength()
	{
		assertThat(buildTube().getPerimeterLength()).isCloseTo(40 + 4 * AngleMath.PI_TWO, within(1e-10));
	}


	@Test
	void testPointsAroundPerimeter()
	{
		var tube = buildTube();

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(-13.999, 4), Vector2.fromXY(-14.001, 4)),
				Lines.segmentFromPoints(Vector2.fromXY(13.999, 4), Vector2.fromXY(14.001, 4)),
				Lines.segmentFromPoints(Vector2.fromXY(0, 7.999), Vector2.fromXY(0, 8.001)),
				Lines.segmentFromPoints(Vector2.fromXY(0, 0.001), Vector2.fromXY(0, -0.001))
		);

		for (var segment : segments)
		{
			assertThat(tube.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(tube.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(tube.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(tube.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(tube.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(tube.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(tube.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(tube.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(tube.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());
		}
	}


	@Test
	void testIntersectPerimeterPathCircle()
	{
		var tube = buildTube();
		var circle = Circle.createCircle(Vector2.fromY(4), 10);
		assertThat(tube.intersectPerimeterPath(circle)).containsExactlyInAnyOrder(
				Vector2.fromXY(9.16515, 8),
				Vector2.fromXY(-9.16515, 8),
				Vector2.fromXY(9.16515, 0),
				Vector2.fromXY(-9.16515, 0)
		);
	}


	@Test
	void testIntersectPerimeterPathArc()
	{
		var tube = buildTube();
		var arc = Arc.createArc(Vector2.fromY(4), 10, 0, AngleMath.PI);
		assertThat(tube.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(9.16515, 8),
				Vector2.fromXY(-9.16515, 8)
		);
		arc = Arc.createArc(Vector2.fromY(4), 10, 0, -AngleMath.PI);
		assertThat(tube.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromXY(9.16515, 0),
				Vector2.fromXY(-9.16515, 0)
		);
	}

	@Test
	void testCompliance()
	{
		I2DShapeComplianceChecker.checkCompliance(buildTube(), true);
	}
}
