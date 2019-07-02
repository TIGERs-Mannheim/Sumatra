/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.triangle.TriangleMath;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * Test for geometric math functions:
 * 
 * <pre>
 * further Internet references:
 * <a href="https://en.wikipedia.org/wiki/Archimedean_spiral">wikipedia.org: arithmetic spiral</a>
 * </pre>
 * 
 * @author KaiE
 */
public class LineMathTest
{
	
	private Random random;
	private static final double TEST_ACCURACY = 1e-3;
	private static final double TEST_ACCURACY_SQR = TEST_ACCURACY * TEST_ACCURACY;
	
	
	private Vector2 createRandomVector(final double scale)
	{
		return Vector2.fromXY(scale * random.nextDouble(), scale * random.nextDouble());
	}
	
	
	@Before
	public void before()
	{
		// we want a fresh random generator for each test, because the test case ordering is not defined
		random = new Random(0);
	}
	
	
	/**
	 * {@link VectorMath#distancePP(IVector2, IVector2)}
	 *
	 * <pre>
	 * What is covered:
	 * 1) default tests with standard values of euclidean space
	 * 2) using points on the arithmetic spiral
	 * 3) multiple random generated values covering the range from 1e-5 to 1e5
	 * </pre>
	 */
	@Test
	public void testDistancePP()
	{
		// 1)
		Assert.assertEquals(1, VectorMath.distancePP(Vector2f.X_AXIS, Vector2f.ZERO_VECTOR), TEST_ACCURACY);
		Assert.assertEquals(1, VectorMath.distancePP(Vector2f.Y_AXIS, Vector2f.ZERO_VECTOR), TEST_ACCURACY);
		Assert.assertEquals(SumatraMath.sqrt(2), VectorMath.distancePP(Vector2f.X_AXIS, Vector2f.Y_AXIS), TEST_ACCURACY);
		
		// 2)
		for (double i = 0; i < (2 * Math.PI); i += 0.1)
		{
			final double spiralCoeff = i + 1e-3;
			IVector2 p = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i), spiralCoeff * SumatraMath.sin(i));
			Assert.assertEquals(spiralCoeff, VectorMath.distancePP(Vector2f.ZERO_VECTOR, p), TEST_ACCURACY_SQR);
		}
		
		// 3)
		for (int i = 0; i <= 10; ++i)
		{
			Vector2 a = createRandomVector(Math.pow(10, (i - 5)));
			Vector2 b = createRandomVector((i - 5));
			double distance = VectorMath.distancePP(a, b);
			Assert.assertEquals(
					SumatraMath.sqrt(((b.x() - a.x()) * (b.x() - a.x())) + ((b.y() - a.y()) * (b.y() - a.y()))),
					distance,
					TEST_ACCURACY_SQR);
		}
		
	}
	
	
	/**
	 * {@link VectorMath#distancePPSqr(IVector2, IVector2)}
	 *
	 * <pre>
	 * what is covered:
	 * random points in range from 1e5 to 1e-5 with check if result matches
	 * the {@link VectorMath#distancePP(IVector2, IVector2)} function squared
	 * </pre>
	 */
	@Test
	public void testDistancePPSqr()
	{
		
		final IVector2[][] testpoints = {
				{ createRandomVector(1e5), createRandomVector(1e5) },
				{ createRandomVector(1e4), createRandomVector(1e4) },
				{ createRandomVector(1e3), createRandomVector(1e3) },
				{ createRandomVector(1e2), createRandomVector(1e2) },
				{ createRandomVector(1e1), createRandomVector(1e1) },
				{ Vector2f.X_AXIS, Vector2f.ZERO_VECTOR },
				{ Vector2f.Y_AXIS, Vector2f.ZERO_VECTOR },
				{ Vector2f.X_AXIS, Vector2f.Y_AXIS },
				{ createRandomVector(1e-1), createRandomVector(1e-1) },
				{ createRandomVector(1e-2), createRandomVector(1e-2) },
				{ createRandomVector(1e-3), createRandomVector(1e-3) },
				{ createRandomVector(1e-4), createRandomVector(1e-4) },
				{ createRandomVector(1e-5), createRandomVector(1e-5) },
				{ createRandomVector(1e-6), createRandomVector(1e-6) }
		};
		
		for (IVector2[] pnts : testpoints)
		{
			final double euclDis = VectorMath.distancePP(pnts[0], pnts[1]);
			Assert.assertEquals(SumatraMath.sqrt(VectorMath.distancePPSqr(pnts[0], pnts[1])), euclDis,
					TEST_ACCURACY_SQR);
		}
		
		
	}
	
	
	/**
	 * {@link LineMath#distancePL(IVector2, ILine)}<br/>
	 *
	 * <pre>
	 * What is covered:
	 * sample lines from the origin in a circle.
	 * The length of the direction vector increases to check numeric
	 * correctness
	 * </pre>
	 *
	 * <pre>
	 * Explanation:
	 * -> first a set of points is generated on the arithmetic spiral resulting
	 * in a set of lines through the origin. The length of the direction-vector differs avoid unit-length vectors
	 * -> because of the definition of these lines the distance to the x and y axis
	 * can be described as the sine or the cosine of the index of the spiral (see unit-circle)
	 * -> a scale-factor is used to transform the distance from [0,1] to [0,~7000]
	 * </pre>
	 */
	@Test
	public void testDistancePL()
	{
		List<ILine> lines = new ArrayList<>();
		List<Double> idx = new ArrayList<>();
		for (double i = 0; i < (2 * Math.PI); i += 0.1)
		{
			idx.add(i);
			final double spiralCoeff = i + 1e-3;
			IVector2 p2 = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i), spiralCoeff * SumatraMath.sin(i));
			lines.add(Line.fromDirection(Vector2f.ZERO_VECTOR, p2));
		}
		for (int i = 0; i < idx.size(); ++i)
		{
			final double scalefactor = idx.get(i) * Math.pow(10, idx.get(i) - Math.PI);
			final IVector2 scaledX = Vector2f.X_AXIS.multiplyNew(scalefactor);
			final IVector2 scaledY = Vector2f.Y_AXIS.multiplyNew(scalefactor);
			final double distanceX = LineMath.distancePL(scaledX, lines.get(i));
			final double distanceY = LineMath.distancePL(scaledY, lines.get(i));
			Assert.assertEquals(Math.abs(scalefactor * SumatraMath.sin(idx.get(i))), distanceX, TEST_ACCURACY_SQR);
			Assert.assertEquals(Math.abs(scalefactor * SumatraMath.cos(idx.get(i))), distanceY, TEST_ACCURACY_SQR);
			
			final double distanceX2 = LineMath.distancePL(scaledX, lines.get(i));
			final double distanceY2 = LineMath.distancePL(scaledY, lines.get(i));
			Assert.assertEquals(Math.abs(scalefactor * SumatraMath.sin(idx.get(i))), distanceX2, TEST_ACCURACY_SQR);
			Assert.assertEquals(Math.abs(scalefactor * SumatraMath.cos(idx.get(i))), distanceY2, TEST_ACCURACY_SQR);
			
			assertThat(distanceX).isCloseTo(distanceX2, within(TEST_ACCURACY));
			assertThat(distanceY).isCloseTo(distanceY2, within(TEST_ACCURACY));
		}
	}
	
	
	/**
	 * {@link LineMath#leadPointOnLine(ILine, IVector2)}<br/>
	 *
	 * <pre>
	 * what is covered:
	 * - parallel cases with the coordinate axis;
	 * - special cases with a line rotating around
	 *   the centre and a point with the centre as
	 *   lead point; specially constructed with pi/2
	 * - the distance increases with growing 'i' to
	 *   check stability
	 * </pre>
	 */
	@Test
	public void testLeadPointOnLine()
	{
		
		ILine line = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		for (double i = 0.0; i < 10; i += 0.1)
		{
			IVector2 point = Vector2.fromXY(i * i, Math.pow(10, i - 5));
			IVector2 expected = Vector2.fromXY(i * i, 0);
			
			Assert.assertTrue(expected.isCloseTo(LineMath.leadPointOnLine(line, point), TEST_ACCURACY_SQR));
		}
		
		line = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);
		for (double i = 0.0; i < 10; i += 0.1)
		{
			IVector2 point = Vector2.fromXY(Math.pow(10, i - 5), i * i);
			IVector2 expected = Vector2.fromXY(0, i * i);
			
			Assert.assertTrue(expected.isCloseTo(LineMath.leadPointOnLine(line, point), TEST_ACCURACY_SQR));
		}
		// three complete unit-circle runs
		for (double i = 0; i < (3 * (2 * Math.PI)); i += 0.1)
		{
			final double spiralCoeff = i + 1e-3;
			IVector2 linepoint = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i), spiralCoeff * SumatraMath.sin(i));
			IVector2 point = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i + (Math.PI / 2)),
					spiralCoeff * SumatraMath.sin(i + (Math.PI / 2)));
			line = Line.fromPoints(Vector2f.ZERO_VECTOR, linepoint);
			Assert.assertTrue(Vector2f.ZERO_VECTOR.isCloseTo(LineMath.leadPointOnLine(line, point), TEST_ACCURACY_SQR));
		}
	}
	
	
	/**
	 * {@link TriangleMath#bisector(IVector2, IVector2, IVector2)} points on a spiral from very small to very big vector
	 * lengths to test numeric stability
	 */
	@Test
	public void testBisector()
	{
		for (double i = 0; i < (2 * Math.PI); i += 0.01)
		{
			final double spiralCoeff = Math.pow(10, i - (0.5 * Math.PI));
			IVector2 p1 = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i), spiralCoeff * SumatraMath.sin(i));
			IVector2 p2 = Vector2.fromXY(spiralCoeff * SumatraMath.cos(i + 0.5), spiralCoeff * SumatraMath.sin(i + 0.5));
			IVector2 e = Vector2.fromXY(p1.x() + (0.5 * (p2.x() - p1.x())), p1.y() + (0.5 * (p2.y() - p1.y())));
			final IVector calculated = TriangleMath.bisector(Vector2f.ZERO_VECTOR, p1, p2);
			Assert.assertTrue(calculated.isCloseTo(e, TEST_ACCURACY));
		}
	}
	
	
	private void intersectionTest(final double lambda, final IFP<IVector2> methods)
	{
		final IVector2 s1 = createRandomVector(1000);
		final IVector2 d1 = createRandomVector(1000);
		final IVector2 s2 = createRandomVector(1000);
		final IVector2 expected = d1.scaleToNew(lambda).add(s1);
		final IVector2 d2 = expected.subtractNew(s2);
		
		try
		{
			final IVector2 r1 = methods.function(s1, d1, s2, d2);
			Assert.assertTrue(r1.isCloseTo(expected, TEST_ACCURACY));
		} catch (Exception e)
		{
			if (!Line.fromDirection(s1, d1).isParallelTo(Line.fromDirection(s1, d1)))
			{
				Assert.fail();
			}
		}
	}
	
	
	@Test
	public void testIntersectionPointAndLineAndPath()
	{
		final IFP<IVector2> intersectionL = (obj) -> LineMath
				.intersectionPoint(Line.fromDirection(obj[0], obj[1]), Line.fromDirection(obj[3], obj[4]))
				.get();
		final IFP<IVector2> intersectionP = (obj) -> LineMath
				.intersectionPointOfSegments(Line.fromDirection(obj[0], obj[1]), Line.fromDirection(obj[3], obj[4]))
				.get();
		
		for (long i = 0; i < 1e6; ++i)
		{
			final double lambda = random.nextDouble();
			intersectionTest(lambda * 100, intersectionL);
			intersectionTest(lambda, intersectionP);
		}
	}
	
	
	/**
	 * {@link CircleMath#stepAlongCircle(IVector2, IVector2, double)}
	 */
	@Test
	public void testStepAlongCircle()
	{
		for (double i = 0; i < (10 * Math.PI); i += 0.001)
		{
			Assert.assertTrue(CircleMath.stepAlongCircle(Vector2f.X_AXIS, Vector2f.ZERO_VECTOR, i).isCloseTo(
					Vector2.fromXY(SumatraMath.cos(i), SumatraMath.sin(i)),
					TEST_ACCURACY));
		}
	}
	
	
	/**
	 * checking 1 Million random lines in a square between +/- 500k in x and y with 10 steps each
	 * on different length values from [0,1000) {@link LineMath#stepAlongLine(IVector2, IVector2, double)}
	 */
	@Test
	public void testStepAlongLine()
	{
		for (double length = 0.0; length < 1000; length += 1)
		{
			final double s1 = random.nextDouble() - 0.5;
			final double s2 = random.nextDouble() - 0.5;
			final double r1 = random.nextDouble();
			final double r2 = random.nextDouble();
			Vector2 start = Vector2.fromXY(s1 * 1e4, s2 * 1e4);
			if ((r1 + r2) < TEST_ACCURACY)
			{
				continue;
			}
			Vector2 endD = Vector2.fromXY(r1 - 0.5, r2 - 0.5);
			Vector2 end = endD.scaleToNew(length).add(start);
			
			for (double i = 0.0; i < length; i += (0.01 * length))
			{
				final IVector2 result = LineMath.stepAlongLine(start, end, i);
				final IVector2 expected = endD.scaleToNew(i).add(start);
				assertThat(result).isEqualTo(expected);
			}
		}
	}
	
	
	@Test
	public void testNearestPointOnLineSegment()
	{
		final IVector2 l1p1 = Vector2.fromXY(-0.8, 0);
		final IVector2 l1p2 = Vector2.fromXY(0.8, 0);
		
		for (double i = 0; i < 10; i += 0.001)
		{
			final IVector2 point = Vector2.fromXY(SumatraMath.cos(i), SumatraMath.sin(i));
			ILine line = Line.fromPoints(l1p1, l1p2);
			final IVector2 result = LineMath.nearestPointOnLineSegment(line, point);
			IVector2 expected;
			if ((Math.abs(point.x()) - TEST_ACCURACY) > 0.8)
			{
				expected = point.x() < 0 ? l1p1 : l1p2;
			} else
			{
				expected = Vector2.fromXY(point.x(), 0);
			}
			Assert.assertTrue(result.isCloseTo(expected, TEST_ACCURACY));
		}
		
	}
	
	
	@Test
	public void testIsPointOnLineSegment()
	{
		ILine segment = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		
		final double margin = 1e-5;
		IVector2 point = Vector2.fromXY(1 + margin / 2.0, 0);
		Assert.assertTrue(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(1 + margin * 2, 0);
		Assert.assertFalse(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(0.5, 0);
		Assert.assertTrue(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(0.5, 0 + margin * 2);
		Assert.assertFalse(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(0.5, 0 + margin);
		Assert.assertTrue(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(0.0 - margin * 2, 0.0);
		Assert.assertFalse(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(0.0 - margin / 2, 0.0);
		Assert.assertTrue(LineMath.isPointOnLineSegment(segment, point, margin));
		
		point = Vector2.fromXY(-SumatraMath.cos(AngleMath.deg2rad(45)) * margin,
				-SumatraMath.cos(AngleMath.deg2rad(45)) * margin);
		Assert.assertTrue(LineMath.isPointOnLineSegment(segment, point, margin));
	}
	
	
	private interface IFP<T1>
	{
		@SuppressWarnings("unchecked")
		T1 function(T1... objects) throws Exception;
	}
	
	/**
	 * Geometry math problems testing.
	 *
	 * @author stei_ol
	 */
	public static class OldLineMathTest
	{
		private static final double ACCURACY = 0.001;
		
		
		/**
		 * Test method for {@link LineMath#intersectionPoint(ILine, ILine)}
		 *
		 * @author Malte
		 */
		@Test
		public void testIntersectionPoint()
		{
			Vector2 p1 = Vector2.fromXY(0, 0);
			Vector2 v1 = Vector2.fromXY(1, 1);
			Vector2 p2 = Vector2.fromXY(0, 1);
			Vector2 v2 = Vector2.fromXY(1, -1);
			IVector2 result = LineMath.intersectionPoint(Line.fromDirection(p1, v1), Line.fromDirection(p2, v2)).get();
			Assert.assertEquals(result.x(), 0.5, ACCURACY);
			Assert.assertEquals(result.y(), 0.5, ACCURACY);
			
			p1 = Vector2.fromXY(4, 4);
			v1 = Vector2.fromXY(453, 13);
			p2 = Vector2.fromXY(4, 4);
			v2 = Vector2.fromXY(-45, 18);
			result = LineMath.intersectionPoint(Line.fromDirection(p1, v1), Line.fromDirection(p2, v2)).get();
			Assert.assertEquals(result.x(), 4, ACCURACY);
			Assert.assertEquals(result.y(), 4, ACCURACY);
			
			// parallel lines
			p1 = Vector2.fromXY(0, 0);
			v1 = Vector2.fromXY(0, 1);
			p2 = Vector2.fromXY(4, 4);
			v2 = Vector2.fromXY(0, 1);
			Assert.assertFalse(
					LineMath.intersectionPoint(Line.fromDirection(p1, v1), Line.fromDirection(p2, v2)).isPresent());
			
			// equal lines
			p1 = Vector2.fromXY(-1, 0);
			v1 = Vector2.fromXY(1, 0);
			p2 = Vector2.fromXY(5, 0);
			v2 = Vector2.fromXY(1, 0);
			Assert.assertFalse(
					LineMath.intersectionPoint(Line.fromDirection(p1, v1), Line.fromDirection(p2, v2)).isPresent());
			
			
			// y-axis
			p1 = Vector2.fromXY(-1, 0);
			v1 = Vector2.fromXY(0, -1);
			p2 = Vector2.fromXY(3, 0);
			v2 = Vector2.fromXY(-1, 1);
			result = LineMath.intersectionPoint(Line.fromDirection(p1, v1), Line.fromDirection(p2, v2)).get();
			Assert.assertEquals(-1, result.x(), ACCURACY);
			Assert.assertEquals(4, result.y(), ACCURACY);
			
			
			// Tests from Malte:
			Line l1 = Line.fromDirection(Vector2.fromXY(0, -1), Vector2.fromXY(1, 1));
			Line l2 = Line.fromDirection(Vector2.fromXY(2, 0), Vector2.fromXY(2, -3));
			Assert.assertTrue(LineMath.intersectionPoint(l1, l2).isPresent());
			
			l1 = Line.fromDirection(Vector2.fromXY(0, 0), Vector2f.Y_AXIS);
			l2 = Line.fromDirection(Vector2.fromXY(2, 0), Vector2f.Y_AXIS);
			Assert.assertFalse(LineMath.intersectionPoint(l1, l2).isPresent());
		}
		
		
		/**
		 * Test method for {@link LineMath#distancePL(IVector2, ILine)}
		 *
		 * @author Malte
		 */
		@Test
		public void testDistancePL()
		{
			Vector2 point = Vector2.fromXY(0, 0);
			ILine line = Line.fromPoints(Vector2.fromXY(-2, 0), Vector2.fromXY(3, 0));
			
			Assert.assertEquals(LineMath.distancePL(point, line), 0, ACCURACY);
			
			point.setY(3);
			Assert.assertEquals(LineMath.distancePL(point, line), 3, ACCURACY);
			
			point.setY(-3);
			Assert.assertEquals(LineMath.distancePL(point, line), 3, ACCURACY);
			
			line = Line.fromPoints(Vector2.fromXY(0, 1), Vector2.fromXY(1, 0));
			point = Vector2.fromXY(0, 0);
			Assert.assertEquals(LineMath.distancePL(point, line), 1.0 / SumatraMath.sqrt(2), ACCURACY);
		}
		
		
		/**
		 * Test method for {@link Circle#isPointInShape(IVector2)}
		 *
		 * @author Steffen
		 */
		@Test
		public void testIsPointInCircle()
		{
			Vector2 center = Vector2.fromXY(6, 4); // St�tzvektor
			ICircle circle = Circle.createCircle(center, 4.9); // Kreis
			
			Vector2 point = Vector2.fromXY(2, 7); // TestPunkt
			
			assertThat(circle.isPointInShape(point)).isFalse();
		}
		
		
		/**
		 * Test method for {@link LineMath#leadPointOnLine(ILine, IVector2)}
		 *
		 * @author Malte
		 */
		@Test
		public void testLeadPointOnLine()
		{
			// normal case
			Vector2 pointA = Vector2.fromXY(4, 1);
			ILine lineA = Line.fromPoints(Vector2.fromXY(1, 2), Vector2.fromXY(5, 4));
			
			IVector2 resultA = LineMath.leadPointOnLine(lineA, pointA);
			Assert.assertEquals(3, resultA.x(), ACCURACY);
			Assert.assertEquals(3, resultA.y(), ACCURACY);
			
			// special case 1. line is orthogonal to x-axis
			Vector2 pointB = Vector2.fromXY(-1, -1);
			ILine lineB = Line.fromPoints(Vector2.fromXY(-2, 0), Vector2.fromXY(3, 0));
			Vector2 result1B = Vector2.fromXY(-1, 0);
			
			IVector2 result2B = LineMath.leadPointOnLine(lineB, pointB);
			Assert.assertEquals(result1B.x(), result2B.x(), ACCURACY);
			Assert.assertEquals(result1B.y(), result2B.y(), ACCURACY);
			
			// special case 2. line is orthogonal to y-axis
			Vector2 pointC = Vector2.fromXY(-3, 3);
			ILine lineC = Line.fromPoints(Vector2.fromXY(2, -2), Vector2.fromXY(2, 4));
			
			IVector2 resultC = LineMath.leadPointOnLine(lineC, pointC);
			Assert.assertEquals(2, resultC.x(), ACCURACY);
			Assert.assertEquals(3, resultC.y(), ACCURACY);
		}
		
		
		/**
		 * Test method for {@link TriangleMath#bisector}
		 *
		 * @author Malte
		 */
		@Test
		public void testCalculateBisector()
		{
			Vector2 result = TriangleMath.bisector(Vector2.fromXY(0, 0), Vector2.fromXY(0, 2), Vector2.fromXY(2, 0));
			Assert.assertEquals(result.x(), 1, ACCURACY);
			Assert.assertEquals(result.y(), 1, ACCURACY);
			
			result = TriangleMath.bisector(Vector2.fromXY(-1, -1), Vector2.fromXY(-1, 0), Vector2.fromXY(0, -1));
			Assert.assertEquals(result.x(), -0.5, ACCURACY);
			Assert.assertEquals(result.y(), -0.5, ACCURACY);
		}
		
		
		/**
		 * Test method for {@link CircleMath#stepAlongCircle(IVector2, IVector2, double)}
		 */
		@Test
		public void testGetNextPointOnCircle()
		{
			Vector2 v = CircleMath.stepAlongCircle(Vector2.fromXY(0f, 1), Vector2.fromXY(0f, 0), AngleMath.PI_HALF);
			Assert.assertEquals(-1, v.x(), ACCURACY);
			Assert.assertEquals(0, v.y(), ACCURACY);
			
			Vector2 v2 = CircleMath.stepAlongCircle(Vector2.fromXY(0f, 1), Vector2.fromXY(0f, 0), -AngleMath.PI_HALF);
			Assert.assertEquals(1, v2.x(), ACCURACY);
			Assert.assertEquals(0, v2.y(), ACCURACY);
			
			Vector2 v3 = CircleMath.stepAlongCircle(Vector2.fromXY(0f, 1), Vector2.fromXY(1f, 1), -AngleMath.PI_HALF);
			Assert.assertEquals(1, v3.x(), ACCURACY);
			Assert.assertEquals(2, v3.y(), ACCURACY);
		}
		
		
		/**
		 * Test method for {@link VectorMath#distancePP}
		 */
		@Test
		public void testDistancePP()
		{
			Vector2 point1 = Vector2.fromXY(1, 2);
			Vector2 point2 = Vector2.fromXY(5, 4);
			
			double result2 = VectorMath.distancePP(point1, point2);
			Assert.assertEquals(4.47213, result2, ACCURACY);
			
			// other tests by malte
			Assert.assertEquals((0), VectorMath.distancePP(Vector2.fromXY(0, 0), Vector2.fromXY(0, 0)), ACCURACY);
			Assert.assertEquals((3), VectorMath.distancePP(Vector2.fromXY(-12, 2), Vector2.fromXY(-12, 5)), ACCURACY);
			Assert.assertEquals((SumatraMath.sqrt(2)), VectorMath.distancePP(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1)),
					ACCURACY);
		}
	}
}
