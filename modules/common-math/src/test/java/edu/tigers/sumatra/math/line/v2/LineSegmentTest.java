/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import static edu.tigers.sumatra.Present.isNotPresent;
import static edu.tigers.sumatra.Present.isPresentAnd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;


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
		IVector2 start = Vector2.fromXY(1, 2);
		IVector2 end = Vector2.fromXY(21, 42);
		IVector2 displacement = end.subtractNew(start);
		IVector2 dV = displacement.normalizeNew();
		
		ILineSegment segment = LineSegment.fromPoints(start, end);
		
		assertThat(segment.getStart(), is(start));
		
		assertThat(segment.getEnd(), is(end));
		
		assertThat(segment.directionVector(), is(dV));
		
		assertThat(segment.getDisplacement(), is(displacement));
	}
	
	
	@Test
	public void testFromIdenticalPoints()
	{
		IVector2 point = Vector2.fromXY(21, 42);
		ILineSegment segment = LineSegment.fromPoints(point, point);
		
		assertThat(segment.getStart(), is(point));
		assertThat(segment.getEnd(), is(point));
		assertThat(segment.getDisplacement(), is(Vector2f.ZERO_VECTOR));
		assertThat(segment.directionVector(), is(Vector2f.ZERO_VECTOR));
	}
	
	
	@Test
	public void testFromDisplacement()
	{
		IVector2 start = Vector2.fromXY(-10, 20);
		IVector2 displacement = Vector2.fromXY(1, 5);
		
		ILineSegment segment = LineSegment.fromOffset(start, displacement);
		
		assertThat(segment.getStart(), is(start));
		assertThat(segment.getEnd(), is(start.addNew(displacement)));
		assertThat(segment.directionVector().isParallelTo(displacement), is(true));
		assertThat(segment.getDisplacement(), is(displacement));
		assertThat(segment.getDisplacement() != displacement, is(true));
	}
	
	
	@Test
	public void testFromZeroDisplacement()
	{
		IVector2 start = Vector2.fromXY(4, 5);
		IVector2 displacement = Vector2f.ZERO_VECTOR;
		
		ILineSegment segment = LineSegment.fromOffset(start, displacement);
		
		assertThat(segment.getStart(), is(start));
		assertThat(segment.getEnd(), is(start));
		assertThat(segment.directionVector(), is(Vector2f.ZERO_VECTOR));
		assertThat(segment.getDisplacement(), is(Vector2f.ZERO_VECTOR));
	}
	
	
	@Test
	public void testIsValid()
	{
		IVector2 start = Vector2.fromXY(10, -20);
		IVector2 end = Vector2f.ZERO_VECTOR;
		
		ILineSegment properLine = LineSegment.fromPoints(start, end);
		ILineSegment invalidLine = LineSegment.fromPoints(start, start);
		
		assertThat(properLine.isValid(), is(true));
		assertThat(invalidLine.isValid(), is(false));
		
		properLine = LineSegment.fromOffset(start, end.subtractNew(start));
		invalidLine = LineSegment.fromOffset(start, Vector2f.ZERO_VECTOR);
		
		assertThat(properLine.isValid(), is(true));
		assertThat(invalidLine.isValid(), is(false));
	}
	
	
	@Test
	public void testDistanceTo()
	{
		IVector2 point = Vector2f.ZERO_VECTOR;
		
		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			
			IVector2 start = Vector2.fromXY(0, 1);
			IVector2 end = Vector2f.X_AXIS.turnToNew(radAngle).add(Vector2.fromXY(0, 1));
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
			
			assertThat(segment.closestPointOnLine(point), is(expectedClosestPoint));
			assertThat(segment.distanceTo(point), closeTo(expectedDistance, ACCURACY));
			
			assertThat(invalidSegment.closestPointOnLine(point), is(start));
			assertThat(invalidSegment.distanceTo(point), closeTo(start.distanceTo(point), ACCURACY));
		}
	}
	
	
	@Test
	public void testEqualsContract()
	{
		EqualsVerifier.forClass(LineSegment.class)
				.suppress(Warning.NULL_FIELDS)
				.withIgnoredFields("directionVector", "displacement")
				.verify();
	}
	
	
	@Test
	public void testEquals()
	{
		IVector2 a = Vector2.fromXY(10, 20);
		IVector2 b = Vector2.fromXY(30, 40);
		IVector2 c = Vector2.fromXY(80, 100);
		
		ILineSegment segmentA = LineSegment.fromPoints(a, b);
		ILineSegment segmentB = LineSegment.fromPoints(a, b);
		assertThat(segmentA, is(segmentB));
		
		segmentB = LineSegment.fromPoints(b, a);
		assertThat(segmentA, not(segmentB));
		
		segmentB = LineSegment.fromPoints(a, c);
		assertThat(segmentA, not(segmentB));
		
		segmentB = LineSegment.fromPoints(c, b);
		assertThat(segmentA, not(segmentB));
	}
	
	
	@Test
	public void testEqualsForInvalid()
	{
		IVector2 start = Vector2.fromXY(10, 15);
		IVector2 end = Vector2.fromXY(20, 3);
		
		ILineSegment valid = LineSegment.fromPoints(start, end);
		ILineSegment invalidA = LineSegment.fromPoints(start, start);
		ILineSegment invalidACopy = LineSegment.fromPoints(start, start);
		ILineSegment invalidB = LineSegment.fromPoints(end, end);
		
		assertThat(valid, not(invalidA));
		assertThat(valid, not(invalidB));
		
		assertThat(invalidA, not(invalidB));
		assertThat(invalidACopy, is(invalidA));
	}
	
	
	@Test
	public void testToHalfLine()
	{
		IVector2 start = Vector2.fromXY(10, 20);
		IVector2 end = Vector2.fromXY(30, 40);
		
		ILineSegment segment = LineSegment.fromPoints(start, end);
		IHalfLine line = segment.toHalfLine();
		
		assertThat(line.supportVector(), is(segment.getStart()));
		assertThat(line.directionVector().getLength(), closeTo(1.0d, ACCURACY));
		assertThat(line.directionVector(), is(segment.directionVector()));
	}
	
	
	@Test
	public void testToLine()
	{
		IVector2 start = Vector2.fromXY(10, 20);
		IVector2 end = Vector2.fromXY(30, 40);
		
		ILineSegment segment = LineSegment.fromPoints(start, end);
		ILine line = segment.toLine();
		
		assertThat(line.supportVector(), is(segment.getStart()));
		assertThat(line.directionVector().getLength(), closeTo(1.0d, ACCURACY));
		assertThat(line.directionVector().isParallelTo(segment.directionVector()), is(true));
	}
	
	
	@Test
	public void testCopy()
	{
		ILineSegment original = LineSegment.fromOffset(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		ILineSegment copy = original.copy();
		
		assertThat(original.getStart(), is(copy.getStart()));
		assertThat(original.getEnd(), is(copy.getEnd()));
		assertThat(original, is(copy));
	}
	
	
	@Test
	public void testToLineFromInvalidSegment()
	{
		IVector2 point = Vector2.fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);
		
		ILine line = segment.toLine();
		
		assertThat(line.supportVector(), is(segment.getStart()));
		assertThat(line.supportVector(), is(segment.getEnd()));
		
		assertThat(line.directionVector(), is(Vector2f.ZERO_VECTOR));
	}
	
	
	@Test
	public void testToHalfLineFromInvalidSegment()
	{
		IVector2 point = Vector2.fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);
		
		IHalfLine line = segment.toHalfLine();
		
		assertThat(line.supportVector(), is(segment.getStart()));
		assertThat(line.supportVector(), is(segment.getEnd()));
		
		assertThat(line.directionVector(), is(Vector2f.ZERO_VECTOR));
	}
	
	
	@Test
	public void testIntersectLine()
	{
		ILine line = Line.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		
		rotateSegment(0, 360, ((degAngle, radAngle, segment) -> {
			
			Optional<IVector2> intersection = segment.intersectLine(line);
			if (degAngle < 210 || degAngle > 330)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = Vector2.fromXY(xVal, 0);
				assertThat(intersection, isPresentAnd(is(expected)));
			}
		}));
	}
	
	
	@Test
	public void testIntersectLineWithInvalid()
	{
		ILine properLine = Line.fromDirection(Vector2.fromXY(1, 1), Vector2f.X_AXIS);
		ILine invalidLine = Line.fromDirection(Vector2.fromXY(1, 0), Vector2f.ZERO_VECTOR);
		
		IVector2 start = Vector2.fromXY(1, -1);
		IVector2 end = Vector2.fromXY(1, 2);
		ILineSegment properSegment = LineSegment.fromPoints(start, end);
		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);
		
		assertThat(properLine.intersectSegment(invalidSegment), isNotPresent());
		assertThat(properSegment.intersectLine(invalidLine), isNotPresent());
		assertThat(invalidSegment.intersectLine(invalidLine), isNotPresent());
		
		assertThat(properSegment.intersectLine(invalidLine), isNotPresent());
		assertThat(properLine.intersectSegment(invalidSegment), isNotPresent());
		assertThat(invalidLine.intersectSegment(invalidSegment), isNotPresent());
	}
	
	
	@Test
	public void testIntersectHalfLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		
		rotateSegment(0, 360, ((degAngle, radAngle, segment) -> {
			
			Optional<IVector2> intersection = segment.intersectHalfLine(halfLine);
			if (degAngle < 270 || degAngle > 330)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = Vector2.fromXY(xVal, 0);
				assertThat(intersection, isPresentAnd(is(expected)));
			}
		}));
	}
	
	
	@Test
	public void testIntersectHalfLineWithInvalid()
	{
		IHalfLine properLine = HalfLine.fromDirection(Vector2.fromXY(1, 1), Vector2f.X_AXIS);
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2.fromXY(1, 0), Vector2f.ZERO_VECTOR);
		
		IVector2 start = Vector2.fromXY(1, -1);
		IVector2 end = Vector2.fromXY(1, 2);
		ILineSegment properSegment = LineSegment.fromPoints(start, end);
		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);
		
		assertThat(properLine.intersectSegment(invalidSegment), isNotPresent());
		assertThat(properSegment.intersectHalfLine(invalidLine), isNotPresent());
		assertThat(invalidSegment.intersectHalfLine(invalidLine), isNotPresent());
		
		assertThat(properSegment.intersectHalfLine(invalidLine), isNotPresent());
		assertThat(properLine.intersectSegment(invalidSegment), isNotPresent());
		assertThat(invalidLine.intersectSegment(invalidSegment), isNotPresent());
	}
	
	
	@Test
	public void testIntersectLineSegment()
	{
		ILineSegment segment = LineSegment.fromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		
		rotateSegment(0, 360, ((degAngle, radAngle, rotatedSegment) -> {
			
			Optional<IVector2> intersection = rotatedSegment.intersectSegment(segment);
			if (degAngle < 270 || degAngle > 315)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = Vector2.fromXY(xVal, 0);
				assertThat(intersection, isPresentAnd(is(expected)));
			}
		}));
	}
	
	
	@Test
	public void testIntersectSegmentWithInvalid()
	{
		ILineSegment validSegment = LineSegment.fromOffset(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		
		ILineSegment invalidSegmentA = LineSegment.fromOffset(Vector2.fromXY(10, 1), Vector2f.ZERO_VECTOR);
		ILineSegment invalidSegmentB = LineSegment.fromOffset(Vector2.fromXY(1, 12), Vector2f.ZERO_VECTOR);
		
		Optional<IVector2> intersection = invalidSegmentA.intersectSegment(validSegment);
		Optional<IVector2> inverseIntersection = validSegment.intersectSegment(invalidSegmentA);
		assertThat(intersection, isNotPresent());
		assertThat(intersection, is(inverseIntersection));
		
		intersection = invalidSegmentA.intersectSegment(invalidSegmentB);
		inverseIntersection = invalidSegmentB.intersectSegment(invalidSegmentA);
		assertThat(intersection, isNotPresent());
		assertThat(intersection, is(inverseIntersection));
		
		intersection = invalidSegmentA.intersectSegment(invalidSegmentA);
		assertThat(intersection, isNotPresent());
	}
	
	
	private void rotateSegment(final int startAngle, final int endAngle, final SegmentRotatable rotatable)
	{
		for (int degAngle = startAngle; degAngle < endAngle; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			
			IVector2 start = Vector2.fromXY(0, 1);
			IVector2 end = Vector2.fromXY(2, 0).turnToNew(radAngle).add(Vector2.fromXY(0, 1));
			ILineSegment intersectSegment = LineSegment.fromPoints(start, end);
			
			rotatable.rotate(degAngle, radAngle, intersectSegment);
		}
	}
	
	
	@Test
	public void testIsPointOnLine()
	{
		ILineSegment segment = LineSegment.fromPoints(Vector2f.ZERO_VECTOR, Vector2.fromXY(3, 0));
		
		IVector2 point = Vector2.fromXY(-10, 0);
		assertThat(segment.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN * 4);
		assertThat(segment.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(1, 0);
		assertThat(segment.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN / 4);
		assertThat(segment.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(3 + ALine.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(10, 0);
		assertThat(segment.isPointOnLine(point), is(false));
	}
	
	
	@Test
	public void testTsPointOnLineInvalidSegment()
	{
		IVector2 sV = Vector2f.ZERO_VECTOR;
		ILineSegment invalidSegment = LineSegment.fromPoints(sV, sV);
		
		IVector2 point = Vector2.fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnLine(point), is(false));
		
		point = Vector2f.ZERO_VECTOR;
		assertThat(invalidSegment.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(ALine.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnLine(point), is(false));
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
		IVector2 end = Vector2.fromXY(21, 42);
		
		ILineSegment segment = LineSegment.fromPoints(start, end);
		double length = end.subtractNew(start).getLength();
		assertThat(segment.getLength(), closeTo(length, ACCURACY));
		
		ILineSegment invalidSegment = LineSegment.fromPoints(start, start);
		assertThat(invalidSegment.getLength(), closeTo(0.0d, ACCURACY));
	}
	
	
	@Test
	public void testStepAlongLine()
	{
		IVector2 start = Vector2.fromXY(5, 12);
		IVector2 end = Vector2.fromXY(30, 400);
		ILineSegment segment = LineSegment.fromPoints(start, end);
		
		IVector2 displacement = end.subtractNew(start);
		for (int stepSize = -10000; stepSize <= 10000; stepSize += 100)
		{
			IVector2 expected = displacement.scaleToNew(stepSize).add(start);
			IVector2 actual = segment.stepAlongLine(stepSize);
			assertThat(actual, is(expected));
		}
	}
	
	
	@Test
	public void testStepAlongInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(5, 12);
		ILineSegment segment = LineSegment.fromPoints(sV, sV);
		
		for (int i = -100; i <= 100; i += 10)
		{
			assertThat(segment.stepAlongLine(i), is(sV));
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
