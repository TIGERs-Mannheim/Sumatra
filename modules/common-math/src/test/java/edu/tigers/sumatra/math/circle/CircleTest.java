/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.I2DShapeComplianceChecker;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.IBoundedPathComplianceChecker;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.stream.StreamUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Test method for {@link Circle}.
 *
 * @author Malte
 */
class CircleTest
{


	private static final double ACCURACY = 1e-3;


	/**
	 * Testmethod for Circle#isIntersectingWithLine.
	 *
	 * @author Dion
	 */
	@Test
	void testIsLineIntersectingShape()
	{
		// Test true
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		ILine line1 = Lines.lineFromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		assertTrue(circle.isIntersectingWithPath(line1));

		// Test true2
		ILine line2 = Lines.lineFromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		assertTrue(circle.isIntersectingWithPath(line2));

		// Test false
		ILine line3 = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(-1, 1));
		assertFalse(circle.isIntersectingWithPath(line3));
	}


	/**
	 * Testmethod for Circle#LineIntersections.
	 *
	 * @author Dion
	 */
	@Test
	void LineIntersections()
	{
		// Test 1
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		ILine line1 = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(-1, 1));
		assertEquals(0, circle.intersectPerimeterPath(line1).size());

		// Test 2
		ILine line2 = Lines.lineFromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		List<IVector2> result = circle.intersectPerimeterPath(line2);
		assertTrue(
				(result.get(0).x() == 2) && (result.get(0).y() == 1) && (result.get(1).x() == 0) && (result.get(1).y()
						== 1));
	}


	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Test
	void testTangentialIntersections()
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(5, 7), 6);
		IVector2 externalPoint = Vector2.fromXY(1, 1);
		List<IVector2> res = circle.tangentialIntersections(externalPoint);
		for (IVector2 p : res)
		{
			if (p.isCloseTo(Vector2.fromXY(5, 1), ACCURACY))
			{
				return;
			}
		}
		fail();
	}


	/**
	 * @author AndreR
	 */
	@Test
	void testCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 1);

		ICircle circle = Circle.from3Points(P1, P2, P3).orElseThrow(() -> new AssertionError("No circle found"));

		assertEquals(1.0, circle.radius(), 1e-10);
		assertEquals(1.0, circle.center().x(), 1e-10);
		assertEquals(1.0, circle.center().y(), 1e-10);
	}


	/**
	 * @author AndreR
	 */
	@Test
	void testInvalidCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 0);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 0);

		if (Circle.from3Points(P1, P2, P3).isPresent())
		{
			fail();
		}
	}


	/**
	 * @author AndreR
	 */
	@Test
	void testSmallCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1e-9);
		IVector2 P2 = Vector2.fromXY(1e-9, 0);
		IVector2 P3 = Vector2.fromXY(2e-9, 1e-9);

		ICircle circle = Circle.from3Points(P1, P2, P3).orElseThrow(() -> new AssertionError("No circle found"));

		assertEquals(1e-9, circle.radius(), 1e-20);
		assertEquals(1e-9, circle.center().x(), 1e-20);
		assertEquals(1e-9, circle.center().y(), 1e-20);
	}


	@Test
	void testCircleFrom2Points()
	{
		IVector2 P1 = Vector2.fromXY(1, 0);
		IVector2 P2 = Vector2.fromXY(-1, 0);

		ICircle circle = Circle.from2Points(P1, P2);

		assertEquals(1, circle.radius(), Double.MIN_VALUE);
		assertEquals(0, circle.center().x(), Double.MIN_VALUE);
		assertEquals(0, circle.center().y(), Double.MIN_VALUE);
	}


	@Test
	void testHullCircle()
	{
		List<IVector2> points = new ArrayList<>();
		points.add(Vector2.fromXY(1, 0));
		points.add(Vector2.fromXY(-1, 0));
		points.add(Vector2.fromXY(0, 0.5));
		points.add(Vector2.fromXY(0, -0.5));

		ICircle circle = Circle.hullCircle(points).orElseThrow(AssertionError::new);

		assertEquals(1.0, circle.radius(), Double.MIN_VALUE);
		assertEquals(0.0, circle.center().x(), Double.MIN_VALUE);
		assertEquals(0.0, circle.center().y(), Double.MIN_VALUE);
	}


	@Test
	void testHullCircleRandomPoints()
	{
		Random rnd = new Random(42);

		long iterations = 1000;
		for (int i = 0; i < iterations; i++)
		{
			int n = 2 + rnd.nextInt(10);
			List<IVector2> points = new ArrayList<>();
			for (int j = 0; j < n; j++)
			{
				points.add(Vector2.fromXY((rnd.nextDouble() * 2000) - 1000, (rnd.nextDouble() + 2000) - 1000));
			}
			var hullFast = Circle.hullCircle(points);
			var hullSlow = Stream.concat(StreamUtil.nonRepeatingPermutation2Fold(points),
							StreamUtil.nonRepeatingPermutation3Fold(points)).map(Circle::fromNPoints)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(c -> points.stream().allMatch(p -> c.withMargin(1e-10).isPointInShape(p)))
					.min(Comparator.comparing(ICircle::radius));

			assertTrue(hullSlow.isPresent());
			assertTrue(hullFast.isPresent());

			assertEquals(hullSlow.get().center().x(), hullFast.get().center().x(), 1e-10);
			assertEquals(hullSlow.get().center().y(), hullFast.get().center().y(), 1e-10);
			assertEquals(hullSlow.get().radius(), hullFast.get().radius(), 1e-10);

			ICircle hullFastNonOpt = hullFast.orElseThrow(AssertionError::new);
			assertThat(points).allMatch(p -> hullFastNonOpt.withMargin(1e-10).isPointInShape(p));
		}
	}


	@Test
	void testIsPointInShape()
	{
		var circle = Circle.createCircle(Vector2.fromX(2), 1);
		assertThat(circle.isPointInShape(Vector2.fromXY(2, 0))).isTrue();
		assertThat(circle.isPointInShape(Vector2.fromXY(3, 0))).isTrue();
		assertThat(circle.isPointInShape(Vector2.fromXY(1, 0))).isTrue();
		assertThat(circle.isPointInShape(Vector2.fromXY(2, 1))).isTrue();
		assertThat(circle.isPointInShape(Vector2.fromXY(2, -1))).isTrue();

		assertThat(circle.isPointInShape(Vector2.fromXY(3.001, 0))).isFalse();
		assertThat(circle.isPointInShape(Vector2.fromXY(0.999, 0))).isFalse();
		assertThat(circle.isPointInShape(Vector2.fromXY(2, 1.001))).isFalse();
		assertThat(circle.isPointInShape(Vector2.fromXY(2, -1.001))).isFalse();
	}


	@Test
	void testWithMargin()
	{
		var circle = Circle.createCircle(Vector2f.fromY(3), 2);
		var withMargin = circle.withMargin(-0.1);
		assertThat(withMargin.center()).isEqualTo(circle.center());
		assertThat(withMargin.radius()).isCloseTo(1.9, within(1e-6));
		withMargin = circle.withMargin(0.1);
		assertThat(withMargin.center()).isEqualTo(circle.center());
		assertThat(withMargin.radius()).isCloseTo(2.1, within(1e-10));
	}


	@Test
	void testGetPerimeterPath()
	{
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		assertThat(circle.getPerimeterPath()).containsExactly(circle);
	}


	@Test
	void testPerimeterPathOrder()
	{
		var perimeter = Circle.createCircle(Vector2.zero(), 1).getPerimeterPath();
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
	void testPointsAroundPerimeter()
	{
		var circle = Circle.createCircle(Vector2.fromX(2), 1);
		assertThat(circle.nearestPointInside(circle.center())).isEqualTo(circle.center());
		assertThat(circle.nearestPointOnPerimeterPath(circle.center())).isEqualTo(Vector2.fromX(3));
		assertThat(circle.nearestPointOutside(circle.center())).isEqualTo(Vector2.fromX(3));

		var segments = List.of(
				Lines.segmentFromPoints(Vector2.fromXY(2.999, 0), Vector2.fromXY(3.001, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(1.001, 0), Vector2.fromXY(0.999, 0)),
				Lines.segmentFromPoints(Vector2.fromXY(2, 0.999), Vector2.fromXY(2, 1.001)),
				Lines.segmentFromPoints(Vector2.fromXY(2, -0.999), Vector2.fromXY(2, -1.001))
		);

		for (var segment : segments)
		{
			assertThat(circle.nearestPointInside(segment.getPathStart())).isEqualTo(segment.getPathStart());
			assertThat(circle.nearestPointInside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(circle.nearestPointInside(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(circle.nearestPointOutside(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(circle.nearestPointOutside(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(circle.nearestPointOutside(segment.getPathEnd())).isEqualTo(segment.getPathEnd());

			assertThat(circle.nearestPointOnPerimeterPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(circle.nearestPointOnPerimeterPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(circle.nearestPointOnPerimeterPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(circle.closestPointOnPath(segment.getPathStart())).isEqualTo(segment.getPathCenter());
			assertThat(circle.closestPointOnPath(segment.getPathCenter())).isEqualTo(segment.getPathCenter());
			assertThat(circle.closestPointOnPath(segment.getPathEnd())).isEqualTo(segment.getPathCenter());

			assertThat(circle.distanceTo(segment.getPathStart())).isCloseTo(0.001, within(1e-10));
			assertThat(circle.distanceTo(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(circle.distanceTo(segment.getPathEnd())).isCloseTo(0.001, within(1e-10));

			assertThat(circle.distanceToSqr(segment.getPathStart())).isCloseTo(0.000001, within(1e-10));
			assertThat(circle.distanceToSqr(segment.getPathCenter())).isCloseTo(0, within(1e-10));
			assertThat(circle.distanceToSqr(segment.getPathEnd())).isCloseTo(0.000001, within(1e-10));

			assertThat(circle.isPointOnPath(segment.getPathStart())).isFalse();
			assertThat(circle.isPointOnPath(segment.getPathCenter())).isTrue();
			assertThat(circle.isPointOnPath(segment.getPathEnd())).isFalse();
		}
	}


	@Test
	void testIntersectPerimeterPathLine()
	{
		// Data generated with GeoGebra
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);

		var path = Lines.lineFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(circle.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(-0.70711, -0.70711)
		);
	}


	@Test
	void testIntersectPerimeterPathHalfLine()
	{
		// Data generated with GeoGebra
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);

		var path = Lines.halfLineFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(circle.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(-0.70711, -0.70711)
		);
		path = Lines.halfLineFromPoints(Vector2f.ZERO_VECTOR, Vector2.fromXY(1, 1));
		assertThat(circle.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711)
		);
	}


	@Test
	void testIntersectPerimeterPathLineSegment()
	{
		// Data generated with GeoGebra
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);

		var path = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(circle.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711),
				Vector2.fromXY(-0.70711, -0.70711)
		);
		path = Lines.segmentFromPoints(Vector2f.ZERO_VECTOR, Vector2.fromXY(1, 1));
		assertThat(circle.intersectPerimeterPath(path)).containsExactlyInAnyOrder(
				Vector2.fromXY(0.70711, 0.70711)
		);
		path = Lines.segmentFromPoints(Vector2f.ZERO_VECTOR, Vector2.fromXY(0.5, 0.5));
		assertThat(circle.intersectPerimeterPath(path)).isEmpty();

	}


	@Test
	void testIntersectPerimeterPathCircle()
	{
		// Data generated with GeoGebra
		var circle1 = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		var circle2 = Circle.createCircle(Vector2f.fromXY(1, 1), 1);

		assertThat(circle1.intersectPerimeterPath(circle2)).containsExactlyInAnyOrder(
				Vector2.fromX(1),
				Vector2.fromY(1)
		);
	}


	@Test
	void testIntersectPerimeterPathArc()
	{
		// Data generated with GeoGebra
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		var arc = Arc.createArc(Vector2f.fromXY(1, 1), 1, -3 * AngleMath.PI_QUART, AngleMath.PI_QUART);
		assertThat(circle.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromX(1)
		);
		arc = Arc.createArc(Vector2f.fromXY(1, 1), 1, -3 * AngleMath.PI_QUART, -AngleMath.PI_QUART);
		assertThat(circle.intersectPerimeterPath(arc)).containsExactlyInAnyOrder(
				Vector2.fromY(1)
		);
	}


	@Test
	void testIsValid()
	{
		var center = Vector2f.ZERO_VECTOR;
		var proper = Circle.createCircle(center, 1);
		var invalid = Circle.createCircle(center, 1e-6);

		assertThat(proper.isValid()).isTrue();
		assertThat(invalid.isValid()).isFalse();

		proper = Circle.from2Points(center, Vector2f.fromX(1));
		invalid = Circle.from2Points(center, center);

		assertThat(proper.isValid()).isTrue();
		assertThat(invalid.isValid()).isFalse();
	}


	@Test
	void testGetPathPoints()
	{
		var radius = 1.0;
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, radius);
		assertThat(circle.getPathStart()).isEqualTo(Vector2.fromX(radius));
		assertThat(circle.getPathStart()).isEqualTo(circle.getPathEnd());
		assertThat(circle.getPathCenter()).isEqualTo(Vector2.fromX(-radius));

		radius = 0.1;
		circle = Circle.createCircle(Vector2f.ZERO_VECTOR, radius);
		assertThat(circle.getPathStart()).isEqualTo(Vector2.fromX(radius));
		assertThat(circle.getPathStart()).isEqualTo(circle.getPathEnd());
		assertThat(circle.getPathCenter()).isEqualTo(Vector2.fromX(-radius));
	}


	@Test
	void testGetPathLength()
	{
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		assertThat(circle.getLength()).isCloseTo(AngleMath.PI_TWO, within(1e-6));
		assertThat(circle.getLength()).isEqualTo(circle.getPerimeterLength());

		circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 0.5);
		assertThat(circle.getLength()).isCloseTo(AngleMath.PI, within(1e-6));
		assertThat(circle.getLength()).isEqualTo(circle.getPerimeterLength());
	}


	@Test
	void testStepAlongPath()
	{
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		assertThat(circle.stepAlongPath(0 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(circle.stepAlongPath(1 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromY(1));
		assertThat(circle.stepAlongPath(2 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(-1));
		assertThat(circle.stepAlongPath(3 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromY(-1));
		assertThat(circle.stepAlongPath(4 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(circle.stepAlongPath(8 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
		assertThat(circle.stepAlongPath(100 * AngleMath.PI_HALF)).isEqualTo(Vector2.fromX(1));
	}


	@Test
	void testCompliance()
	{
		var circles = List.of(
				Circle.createCircle(Vector2f.ZERO_VECTOR, 1),
				Circle.createCircle(Vector2f.ZERO_VECTOR, 0.5),
				Circle.createCircle(Vector2.fromXY(10, 3), 7)
		);
		for (var circle : circles)
		{
			IBoundedPathComplianceChecker.checkCompliance(circle, true);
			I2DShapeComplianceChecker.checkCompliance(circle, true);
		}
	}
}
