/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.stream.StreamUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Test method for {@link Circle}.
 *
 * @author Malte
 */
public class CircleTest
{


	private static final double ACCURACY = 1e-3;


	/**
	 * Testmethod for Circle#isIntersectingWithLine.
	 *
	 * @author Dion
	 */
	@Test
	public void testIsLineIntersectingShape()
	{
		// Test true
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		Line line = Line.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		Assert.assertTrue(circle.isIntersectingWithLine(line));

		// Test true2
		Line line3 = Line.fromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		Assert.assertTrue(circle.isIntersectingWithLine(line3));

		// Test false
		Line line2 = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(-1, 1));
		Assert.assertFalse(circle.isIntersectingWithLine(line2));
	}


	/**
	 * Testmethod for Circle#LineIntersections.
	 *
	 * @author Dion
	 */
	@Test
	public void LineIntersections()
	{
		// Test 1
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		Line line = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(-1, 1));
		Assert.assertEquals(0, circle.lineIntersections(line).size());

		// Test 2
		Line line2 = Line.fromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		List<IVector2> result = circle.lineIntersections(line2);
		Assert.assertTrue(
				(result.get(0).x() == 2) && (result.get(0).y() == 1) && (result.get(1).x() == 0) && (result.get(1).y()
						== 1));
	}


	/**
	 * Testmethod for Circle#nearestPointOutsideCircle.
	 *
	 * @author Dion
	 */
	@Test
	public void testNearestPointOutside()
	{
		// Test true
		ICircle circle = Circle.createCircle(Vector2.fromXY(-2, 4), 3);
		Vector2 point = Vector2.fromXY(-1, 4);
		Assert.assertEquals(circle.nearestPointOutside(point), Vector2.fromXY(1, 4));

		// Test false
		Vector2 point3 = Vector2.fromXY(1, 2);
		Assert.assertEquals(circle.nearestPointOutside(point3), Vector2.fromXY(1, 2));
	}


	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Test
	public void testTangentialIntersections()
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
		Assert.fail();
	}


	/**
	 * @author AndreR
	 */
	@Test
	public void testCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 1);

		ICircle circle = Circle.from3Points(P1, P2, P3).orElseThrow(() -> new AssertionError("No circle found"));

		Assert.assertEquals(1.0, circle.radius(), 1e-10);
		Assert.assertEquals(1.0, circle.center().x(), 1e-10);
		Assert.assertEquals(1.0, circle.center().y(), 1e-10);
	}


	/**
	 * @author AndreR
	 */
	@Test
	public void testInvalidCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 0);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 0);

		if (Circle.from3Points(P1, P2, P3).isPresent())
		{
			Assert.fail();
		}
	}


	/**
	 * @author AndreR
	 */
	@Test
	public void testSmallCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1e-9);
		IVector2 P2 = Vector2.fromXY(1e-9, 0);
		IVector2 P3 = Vector2.fromXY(2e-9, 1e-9);

		ICircle circle = Circle.from3Points(P1, P2, P3).orElseThrow(() -> new AssertionError("No circle found"));

		Assert.assertEquals(1e-9, circle.radius(), 1e-20);
		Assert.assertEquals(1e-9, circle.center().x(), 1e-20);
		Assert.assertEquals(1e-9, circle.center().y(), 1e-20);
	}


	@Test
	public void testCircleFrom2Points()
	{
		IVector2 P1 = Vector2.fromXY(1, 0);
		IVector2 P2 = Vector2.fromXY(-1, 0);

		ICircle circle = Circle.from2Points(P1, P2);

		Assert.assertEquals(1, circle.radius(), Double.MIN_VALUE);
		Assert.assertEquals(0, circle.center().x(), Double.MIN_VALUE);
		Assert.assertEquals(0, circle.center().y(), Double.MIN_VALUE);
	}


	@Test
	public void testHullCircle()
	{
		List<IVector2> points = new ArrayList<>();
		points.add(Vector2.fromXY(1, 0));
		points.add(Vector2.fromXY(-1, 0));
		points.add(Vector2.fromXY(0, 0.5));
		points.add(Vector2.fromXY(0, -0.5));

		ICircle circle = Circle.hullCircle(points).orElseThrow(AssertionError::new);

		Assert.assertEquals(1.0, circle.radius(), Double.MIN_VALUE);
		Assert.assertEquals(0.0, circle.center().x(), Double.MIN_VALUE);
		Assert.assertEquals(0.0, circle.center().y(), Double.MIN_VALUE);
	}


	@Test
	public void testHullCircleRandomPoints()
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
					.filter(c -> points.stream().allMatch(p -> c.isPointInShape(p, 1e-10)))
					.min(Comparator.comparing(ICircle::radius));

			Assert.assertTrue(hullSlow.isPresent());
			Assert.assertTrue(hullFast.isPresent());

			Assert.assertEquals(hullSlow.get().center().x(), hullFast.get().center().x(), 1e-10);
			Assert.assertEquals(hullSlow.get().center().y(), hullFast.get().center().y(), 1e-10);
			Assert.assertEquals(hullSlow.get().radius(), hullFast.get().radius(), 1e-10);

			ICircle hullFastNonOpt = hullFast.orElseThrow(AssertionError::new);
			assertThat(points).allMatch(p -> hullFastNonOpt.isPointInShape(p, 1e-10));
		}
	}
}
