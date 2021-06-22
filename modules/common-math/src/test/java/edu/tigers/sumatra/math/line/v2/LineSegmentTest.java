/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.util.Optional;

import static edu.tigers.sumatra.math.line.v2.Lines.segmentFromPoints;
import static edu.tigers.sumatra.math.vector.Vector2.fromXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Lukas Magel
 */
public class LineSegmentTest extends AbstractLineTest
{
	private final static LineConstructor lineConstructor = dV -> LineSegment.fromPoints(Vector2f.ZERO_VECTOR,
			Vector2f.ZERO_VECTOR.addNew(dV));


	@Test
	public void testFromPoints()
	{
		IVector2 start = fromXY(1, 2);
		IVector2 end = fromXY(21, 42);
		IVector2 dV = end.subtractNew(start);

		ILineSegment segment = LineSegment.fromPoints(start, end);

		assertThat(segment.getStart()).isEqualTo(start);

		assertThat(segment.getEnd()).isEqualTo(end);

		assertThat(segment.directionVector()).isEqualTo(dV);
	}


	@Test
	public void testFromIdenticalPoints()
	{
		IVector2 point = fromXY(21, 42);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		assertThat(segment.getStart()).isEqualTo(point);
		assertThat(segment.getEnd()).isEqualTo(point);
		assertThat(segment.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testFromDisplacement()
	{
		IVector2 start = fromXY(-10, 20);
		IVector2 displacement = fromXY(1, 5);

		ILineSegment segment = LineSegment.fromOffset(start, displacement);

		assertThat(segment.getStart()).isEqualTo(start);
		assertThat(segment.getEnd()).isEqualTo(start.addNew(displacement));
		assertThat(segment.directionVector()).isEqualTo(displacement);
		assertThat(segment.directionVector() != displacement).isEqualTo(true);
	}


	@Test
	public void testFromZeroDisplacement()
	{
		IVector2 start = fromXY(4, 5);
		IVector2 displacement = Vector2f.ZERO_VECTOR;

		ILineSegment segment = LineSegment.fromOffset(start, displacement);

		assertThat(segment.getStart()).isEqualTo(start);
		assertThat(segment.getEnd()).isEqualTo(start);
		assertThat(segment.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIsValid()
	{
		IVector2 start = fromXY(10, -20);
		IVector2 end = Vector2f.ZERO_VECTOR;

		ILineSegment properLine = LineSegment.fromPoints(start, end);
		ILineSegment invalidLine = LineSegment.fromPoints(start, start);

		assertThat(properLine.isValid()).isEqualTo(true);
		assertThat(invalidLine.isValid()).isEqualTo(false);

		properLine = LineSegment.fromOffset(start, end.subtractNew(start));
		invalidLine = LineSegment.fromOffset(start, Vector2f.ZERO_VECTOR);

		assertThat(properLine.isValid()).isEqualTo(true);
		assertThat(invalidLine.isValid()).isEqualTo(false);
	}


	@Test
	public void testDistanceTo()
	{
		IVector2 point = Vector2f.ZERO_VECTOR;

		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 start = fromXY(0, 1);
			IVector2 end = Vector2f.X_AXIS.turnToNew(radAngle).add(fromXY(0, 1));
			ILineSegment segment = LineSegment.fromPoints(start, end);
			ILineSegment invalidSegment = LineSegment.fromPoints(start, start);

			IVector2 expectedClosestPoint;
			double expectedDistance;
			if (degAngle <= 180)
			{
				expectedClosestPoint = start;
				expectedDistance = point.distanceTo(start);
			} else
			{
				double radAngleAtOrigin = radAngle - Math.PI;
				double distanceLinePoint = SumatraMath.cos(radAngleAtOrigin);

				expectedClosestPoint = Vector2f.Y_AXIS.turnNew(radAngleAtOrigin).scaleTo(distanceLinePoint);
				expectedDistance = Math.abs(distanceLinePoint);
			}

			assertThat(segment.closestPointOnLine(point)).isEqualTo(expectedClosestPoint);
			assertThat(segment.distanceTo(point)).isCloseTo(expectedDistance, within(ACCURACY));

			assertThat(invalidSegment.closestPointOnLine(point)).isEqualTo(start);
			assertThat(invalidSegment.distanceTo(point)).isCloseTo(start.distanceTo(point), within(ACCURACY));
		}
	}


	@Test
	public void testDistanceToLineSegment()
	{
		// two zero-lines
		assertThat(segmentFromPoints(fromXY(0, 0), fromXY(0, 0)).distanceTo(
				segmentFromPoints(fromXY(0, 0), fromXY(0, 0))))
				.isCloseTo(0, within(1e-10));
		// intersection at 0,0
		assertThat(segmentFromPoints(fromXY(1, 1), fromXY(-1, -1)).distanceTo(
				segmentFromPoints(fromXY(1, -1), fromXY(-1, 1))))
				.isCloseTo(0, within(1e-10));
		// parallel lines
		assertThat(segmentFromPoints(fromXY(1, -1), fromXY(1, 1)).distanceTo(
				segmentFromPoints(fromXY(-1, -1), fromXY(-1, 1))))
				.isCloseTo(2, within(1e-10));
		// other lines
		assertThat(segmentFromPoints(fromXY(2, -1), fromXY(1, 1)).distanceTo(
				segmentFromPoints(fromXY(-1, -1), fromXY(-1, 1))))
				.isCloseTo(2, within(1e-10));
		assertThat(segmentFromPoints(fromXY(2, -1), fromXY(0, 0)).distanceTo(
				segmentFromPoints(fromXY(-1, -1), fromXY(-1, 1))))
				.isCloseTo(1, within(1e-10));
		assertThat(segmentFromPoints(fromXY(1, -1), fromXY(1, 1)).distanceTo(
				segmentFromPoints(fromXY(-2, -1), fromXY(-1, 1))))
				.isCloseTo(2, within(1e-10));
		// behind each other
		assertThat(segmentFromPoints(fromXY(2, 0), fromXY(1, 0)).distanceTo(
				segmentFromPoints(fromXY(-1, 0), fromXY(-2, 0))))
				.isCloseTo(2, within(1e-10));
	}


	@Test
	public void testEqualsContract()
	{
		EqualsVerifier.forClass(LineSegment.class)
				.suppress(Warning.NULL_FIELDS)
				.withIgnoredFields("directionVector")
				.verify();
	}


	@Test
	public void testEquals()
	{
		IVector2 a = fromXY(10, 20);
		IVector2 b = fromXY(30, 40);
		IVector2 c = fromXY(80, 100);

		ILineSegment segmentA = LineSegment.fromPoints(a, b);
		ILineSegment segmentB = LineSegment.fromPoints(a, b);
		assertThat(segmentA).isEqualTo(segmentB);

		segmentB = LineSegment.fromPoints(b, a);
		assertThat(segmentA).isNotEqualTo(segmentB);

		segmentB = LineSegment.fromPoints(a, c);
		assertThat(segmentA).isNotEqualTo(segmentB);

		segmentB = LineSegment.fromPoints(c, b);
		assertThat(segmentA).isNotEqualTo(segmentB);
	}


	@Test
	public void testEqualsForInvalid()
	{
		IVector2 start = fromXY(10, 15);
		IVector2 end = fromXY(20, 3);

		ILineSegment valid = LineSegment.fromPoints(start, end);
		ILineSegment invalidA = LineSegment.fromPoints(start, start);
		ILineSegment invalidACopy = LineSegment.fromPoints(start, start);
		ILineSegment invalidB = LineSegment.fromPoints(end, end);

		assertThat(valid).isNotEqualTo(invalidA);
		assertThat(valid).isNotEqualTo(invalidB);

		assertThat(invalidA).isNotEqualTo(invalidB);
		assertThat(invalidACopy).isEqualTo(invalidA);
	}


	@Test
	public void testToHalfLine()
	{
		IVector2 start = fromXY(10, 20);
		IVector2 end = fromXY(30, 40);

		ILineSegment segment = LineSegment.fromPoints(start, end);
		IHalfLine line = segment.toHalfLine();

		assertThat(line.supportVector()).isEqualTo(segment.getStart());
		assertThat(line.directionVector()).isEqualTo(segment.directionVector());
	}


	@Test
	public void testToLine()
	{
		IVector2 start = fromXY(10, 20);
		IVector2 end = fromXY(30, 40);

		ILineSegment segment = LineSegment.fromPoints(start, end);
		ILine line = segment.toLine();

		assertThat(line.supportVector()).isEqualTo(segment.getStart());
		assertThat(line.directionVector().isParallelTo(segment.directionVector())).isEqualTo(true);
	}


	@Test
	public void testCopy()
	{
		ILineSegment original = LineSegment.fromOffset(fromXY(0, 0), fromXY(1, 1));
		ILineSegment copy = original.copy();

		assertThat(original.getStart()).isEqualTo(copy.getStart());
		assertThat(original.getEnd()).isEqualTo(copy.getEnd());
		assertThat(original).isEqualTo(copy);
	}


	@Test
	public void testToLineFromInvalidSegment()
	{
		IVector2 point = fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		ILine line = segment.toLine();

		assertThat(line.supportVector()).isEqualTo(segment.getStart());
		assertThat(line.supportVector()).isEqualTo(segment.getEnd());

		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testToHalfLineFromInvalidSegment()
	{
		IVector2 point = fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		IHalfLine line = segment.toHalfLine();

		assertThat(line.supportVector()).isEqualTo(segment.getStart());
		assertThat(line.supportVector()).isEqualTo(segment.getEnd());

		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIntersectLine()
	{
		ILine line = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		rotateSegment(0, 360, ((degAngle, radAngle, segment) -> {

			Optional<IVector2> intersection = segment.intersectLine(line);
			if (degAngle < 210 || degAngle > 330)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection.get()).isEqualTo(expected);
			}
		}));
	}


	@Test
	public void testIntersectLineWithInvalid()
	{
		ILine properLine = Line.fromDirection(fromXY(1, 1), Vector2f.X_AXIS);
		ILine invalidLine = Line.fromDirection(fromXY(1, 0), Vector2f.ZERO_VECTOR);

		IVector2 start = fromXY(1, -1);
		IVector2 end = fromXY(1, 2);
		ILineSegment properSegment = LineSegment.fromPoints(start, end);
		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);

		assertThat(properLine.intersectSegment(invalidSegment)).isNotPresent();
		assertThat(properSegment.intersectLine(invalidLine)).isNotPresent();
		assertThat(invalidSegment.intersectLine(invalidLine)).isNotPresent();

		assertThat(properSegment.intersectLine(invalidLine)).isNotPresent();
		assertThat(properLine.intersectSegment(invalidSegment)).isNotPresent();
		assertThat(invalidLine.intersectSegment(invalidSegment)).isNotPresent();
	}


	@Test
	public void testIntersectHalfLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		rotateSegment(0, 360, ((degAngle, radAngle, segment) -> {

			Optional<IVector2> intersection = segment.intersectHalfLine(halfLine);
			if (degAngle < 270 || degAngle > 330)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection.get()).isEqualTo(expected);
			}
		}));
	}


	@Test
	public void testIntersectHalfLineWithInvalid()
	{
		IHalfLine properLine = HalfLine.fromDirection(fromXY(1, 1), Vector2f.X_AXIS);
		IHalfLine invalidLine = HalfLine.fromDirection(fromXY(1, 0), Vector2f.ZERO_VECTOR);

		IVector2 start = fromXY(1, -1);
		IVector2 end = fromXY(1, 2);
		ILineSegment properSegment = LineSegment.fromPoints(start, end);
		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);

		assertThat(properLine.intersectSegment(invalidSegment)).isNotPresent();
		assertThat(properSegment.intersectHalfLine(invalidLine)).isNotPresent();
		assertThat(invalidSegment.intersectHalfLine(invalidLine)).isNotPresent();

		assertThat(properSegment.intersectHalfLine(invalidLine)).isNotPresent();
		assertThat(properLine.intersectSegment(invalidSegment)).isNotPresent();
		assertThat(invalidLine.intersectSegment(invalidSegment)).isNotPresent();
	}


	@Test
	public void testIntersectLineSegment()
	{
		ILineSegment segment = LineSegment.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		rotateSegment(0, 360, ((degAngle, radAngle, rotatedSegment) -> {

			Optional<IVector2> intersection = rotatedSegment.intersectSegment(segment);
			if (degAngle < 270 || degAngle > 315)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection.get()).isEqualTo(expected);
			}
		}));
	}


	@Test
	public void testIntersectSegmentWithInvalid()
	{
		ILineSegment validSegment = LineSegment.fromOffset(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		ILineSegment invalidSegmentA = LineSegment.fromOffset(fromXY(10, 1), Vector2f.ZERO_VECTOR);
		ILineSegment invalidSegmentB = LineSegment.fromOffset(fromXY(1, 12), Vector2f.ZERO_VECTOR);

		Optional<IVector2> intersection = invalidSegmentA.intersectSegment(validSegment);
		Optional<IVector2> inverseIntersection = validSegment.intersectSegment(invalidSegmentA);
		assertThat(intersection).isNotPresent();
		assertThat(intersection).isEqualTo(inverseIntersection);

		intersection = invalidSegmentA.intersectSegment(invalidSegmentB);
		inverseIntersection = invalidSegmentB.intersectSegment(invalidSegmentA);
		assertThat(intersection).isNotPresent();
		assertThat(intersection).isEqualTo(inverseIntersection);

		intersection = invalidSegmentA.intersectSegment(invalidSegmentA);
		assertThat(intersection).isNotPresent();
	}


	private void rotateSegment(final int startAngle, final int endAngle, final SegmentRotatable rotatable)
	{
		for (int degAngle = startAngle; degAngle < endAngle; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 start = fromXY(0, 1);
			IVector2 end = fromXY(2, 0).turnToNew(radAngle).add(fromXY(0, 1));
			ILineSegment intersectSegment = LineSegment.fromPoints(start, end);

			rotatable.rotate(degAngle, radAngle, intersectSegment);
		}
	}


	@Test
	public void testIsPointOnLine()
	{
		ILineSegment segment = LineSegment.fromPoints(Vector2f.ZERO_VECTOR, fromXY(3, 0));

		IVector2 point = fromXY(-10, 0);
		assertThat(segment.isPointOnLine(point)).isEqualTo(false);

		point = fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnLine(point)).isEqualTo(false);

		point = fromXY(1, ALine.LINE_MARGIN * 4);
		assertThat(segment.isPointOnLine(point)).isEqualTo(false);

		point = fromXY(1, 0);
		assertThat(segment.isPointOnLine(point)).isEqualTo(true);

		point = fromXY(1, ALine.LINE_MARGIN / 4);
		assertThat(segment.isPointOnLine(point)).isEqualTo(true);

		point = fromXY(3 + ALine.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnLine(point)).isEqualTo(false);

		point = fromXY(10, 0);
		assertThat(segment.isPointOnLine(point)).isEqualTo(false);
	}


	@Test
	public void testTsPointOnLineInvalidSegment()
	{
		IVector2 sV = Vector2f.ZERO_VECTOR;
		ILineSegment invalidSegment = LineSegment.fromPoints(sV, sV);

		IVector2 point = fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnLine(point)).isEqualTo(false);

		point = Vector2f.ZERO_VECTOR;
		assertThat(invalidSegment.isPointOnLine(point)).isEqualTo(true);

		point = fromXY(ALine.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnLine(point)).isEqualTo(false);
	}


	@Test
	public void testGetSlope()
	{
		doTestGetSlope(lineConstructor);
	}


	@Test
	public void testGetAngle()
	{
		doTestGetAngle(lineConstructor);
	}


	@Test
	public void testGetLength()
	{
		IVector2 start = Vector2f.ZERO_VECTOR;
		IVector2 end = fromXY(21, 42);

		ILineSegment segment = LineSegment.fromPoints(start, end);
		double length = end.subtractNew(start).getLength();
		assertThat(segment.getLength()).isCloseTo(length, within(ACCURACY));

		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);
		assertThat(invalidSegment.getLength()).isCloseTo(0.0d, within(ACCURACY));
	}


	@Test
	public void testStepAlongLine()
	{
		IVector2 start = fromXY(5, 12);
		IVector2 end = fromXY(30, 400);
		ILineSegment segment = LineSegment.fromPoints(start, end);

		IVector2 displacement = end.subtractNew(start);
		for (int stepSize = -10000; stepSize <= 10000; stepSize += 100)
		{
			IVector2 expected = displacement.scaleToNew(stepSize).add(start);
			IVector2 actual = segment.stepAlongLine(stepSize);
			assertThat(actual).isEqualTo(expected);
		}
	}


	@Test
	public void testStepAlongInvalidLine()
	{
		IVector2 sV = fromXY(5, 12);
		ILineSegment segment = LineSegment.fromPoints(sV, sV);

		for (int i = -100; i <= 100; i += 10)
		{
			assertThat(segment.stepAlongLine(i)).isEqualTo(sV);
		}
	}


	@Test
	public void testIsParallelTo()
	{
		doTestIsParallelTo(lineConstructor);
	}


	@Test
	public void testOrientation()
	{
		doTestOrientation(lineConstructor);
	}


	@FunctionalInterface
	private interface SegmentRotatable
	{

		void rotate(double degAngle, double radAngle, ILineSegment segment);
	}

}
