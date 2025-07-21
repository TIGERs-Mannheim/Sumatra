/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IBoundedPathComplianceChecker;
import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static edu.tigers.sumatra.math.line.Lines.segmentFromPoints;
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

		assertThat(segment.getPathStart()).isEqualTo(start);

		assertThat(segment.getPathEnd()).isEqualTo(end);

		assertThat(segment.directionVector()).isEqualTo(dV);
	}


	@Test
	public void testFromIdenticalPoints()
	{
		IVector2 point = fromXY(21, 42);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		assertThat(segment.getPathStart()).isEqualTo(point);
		assertThat(segment.getPathEnd()).isEqualTo(point);
		assertThat(segment.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testFromDisplacement()
	{
		IVector2 start = fromXY(-10, 20);
		IVector2 displacement = fromXY(1, 5);

		ILineSegment segment = LineSegment.fromOffset(start, displacement);

		assertThat(segment.getPathStart()).isEqualTo(start);
		assertThat(segment.getPathEnd()).isEqualTo(start.addNew(displacement));
		assertThat(segment.directionVector()).isEqualTo(displacement);
		assertThat(segment.directionVector()).isNotSameAs(displacement);
	}


	@Test
	public void testFromZeroDisplacement()
	{
		IVector2 start = fromXY(4, 5);
		IVector2 displacement = Vector2f.ZERO_VECTOR;

		ILineSegment segment = LineSegment.fromOffset(start, displacement);

		assertThat(segment.getPathStart()).isEqualTo(start);
		assertThat(segment.getPathEnd()).isEqualTo(start);
		assertThat(segment.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIsValid()
	{
		IVector2 start = fromXY(10, -20);
		IVector2 end = Vector2f.ZERO_VECTOR;

		ILineSegment properLine = LineSegment.fromPoints(start, end);
		ILineSegment invalidLine = LineSegment.fromPoints(start, start);

		assertThat(properLine.isValid()).isTrue();
		assertThat(invalidLine.isValid()).isFalse();

		properLine = LineSegment.fromOffset(start, end.subtractNew(start));
		invalidLine = LineSegment.fromOffset(start, Vector2f.ZERO_VECTOR);

		assertThat(properLine.isValid()).isTrue();
		assertThat(invalidLine.isValid()).isFalse();
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

			assertThat(segment.closestPointOnPath(point)).isEqualTo(expectedClosestPoint);
			assertThat(segment.distanceTo(point)).isCloseTo(expectedDistance, within(ACCURACY));

			assertThat(invalidSegment.closestPointOnPath(point)).isEqualTo(start);
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
				.withIgnoredFields("valid")
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

		assertThat(valid)
				.isNotEqualTo(invalidA)
				.isNotEqualTo(invalidB);

		assertThat(invalidA)
				.isNotEqualTo(invalidB)
				.isEqualTo(invalidACopy);
	}


	@Test
	public void testToHalfLine()
	{
		IVector2 start = fromXY(10, 20);
		IVector2 end = fromXY(30, 40);

		ILineSegment segment = LineSegment.fromPoints(start, end);
		IHalfLine line = segment.toHalfLine();

		assertThat(line.supportVector()).isEqualTo(segment.getPathStart());
		assertThat(line.directionVector()).isEqualTo(segment.directionVector());
	}


	@Test
	public void testToLine()
	{
		IVector2 start = fromXY(10, 20);
		IVector2 end = fromXY(30, 40);

		ILineSegment segment = LineSegment.fromPoints(start, end);
		ILine line = segment.toLine();

		assertThat(line.supportVector()).isEqualTo(segment.getPathStart());
		assertThat(line.directionVector().isParallelTo(segment.directionVector())).isTrue();
	}


	@Test
	public void testCopy()
	{
		ILineSegment original = LineSegment.fromOffset(fromXY(0, 0), fromXY(1, 1));
		ILineSegment copy = original.copy();

		assertThat(original.getPathStart()).isEqualTo(copy.getPathStart());
		assertThat(original.getPathEnd()).isEqualTo(copy.getPathEnd());
		assertThat(original).isEqualTo(copy);
	}


	@Test
	public void testToLineFromInvalidSegment()
	{
		IVector2 point = fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		ILine line = segment.toLine();

		assertThat(line.supportVector()).isEqualTo(segment.getPathStart());
		assertThat(line.supportVector()).isEqualTo(segment.getPathEnd());

		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testToHalfLineFromInvalidSegment()
	{
		IVector2 point = fromXY(2, 1);
		ILineSegment segment = LineSegment.fromPoints(point, point);

		IHalfLine line = segment.toHalfLine();

		assertThat(line.supportVector()).isEqualTo(segment.getPathStart());
		assertThat(line.supportVector()).isEqualTo(segment.getPathEnd());

		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIsPointOnLine()
	{
		ILineSegment segment = LineSegment.fromPoints(Vector2f.ZERO_VECTOR, fromXY(3, 0));

		IVector2 point = fromXY(-10, 0);
		assertThat(segment.isPointOnPath(point)).isFalse();

		point = fromXY(-IPath.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnPath(point)).isFalse();

		point = fromXY(1, IPath.LINE_MARGIN * 4);
		assertThat(segment.isPointOnPath(point)).isFalse();

		point = fromXY(1, 0);
		assertThat(segment.isPointOnPath(point)).isTrue();

		point = fromXY(1, IPath.LINE_MARGIN / 4);
		assertThat(segment.isPointOnPath(point)).isTrue();

		point = fromXY(3 + IPath.LINE_MARGIN * 4, 0);
		assertThat(segment.isPointOnPath(point)).isFalse();

		point = fromXY(10, 0);
		assertThat(segment.isPointOnPath(point)).isFalse();
	}


	@Test
	public void testTsPointOnLineInvalidSegment()
	{
		IVector2 sV = Vector2f.ZERO_VECTOR;
		ILineSegment invalidSegment = LineSegment.fromPoints(sV, sV);

		IVector2 point = fromXY(-IPath.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnPath(point)).isFalse();

		point = Vector2f.ZERO_VECTOR;
		assertThat(invalidSegment.isPointOnPath(point)).isTrue();

		point = fromXY(IPath.LINE_MARGIN * 4, 0);
		assertThat(invalidSegment.isPointOnPath(point)).isFalse();
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
			IVector2 actual = segment.stepAlongPath(stepSize);
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
			assertThat(segment.stepAlongPath(i)).isEqualTo(sV);
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


	@Test
	public void testClosestPointOnPath()
	{
		var segment = Lines.segmentFromOffset(Vector2.zero(), Vector2.fromX(1));

		assertThat(segment.closestPointOnPath(Vector2.fromXY(-0.0001, 0))).isEqualTo(Vector2.fromX(0));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(0.5, 1))).isEqualTo(Vector2.fromX(0.5));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(0.5, 0.0001))).isEqualTo(Vector2.fromX(0.5));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(0.5, 0))).isEqualTo(Vector2.fromX(0.5));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(0.5, -0.0001))).isEqualTo(Vector2.fromX(0.5));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(0.5, -1))).isEqualTo(Vector2.fromX(0.5));
		assertThat(segment.closestPointOnPath(Vector2.fromXY(1.0001, 0))).isEqualTo(Vector2.fromX(1));
	}


	@Test
	public void testIsPointOnPath()
	{
		var segment = Lines.segmentFromOffset(Vector2.zero(), Vector2.fromX(1));

		assertThat(segment.isPointOnPath(Vector2.fromXY(-0.0001, 0))).isFalse();
		assertThat(segment.isPointOnPath(Vector2.fromXY(0.5, 1))).isFalse();
		assertThat(segment.isPointOnPath(Vector2.fromXY(0.5, 0.0001))).isFalse();
		assertThat(segment.isPointOnPath(Vector2.fromXY(0.5, 0))).isTrue();
		assertThat(segment.isPointOnPath(Vector2.fromXY(0.5, -0.0001))).isFalse();
		assertThat(segment.isPointOnPath(Vector2.fromXY(0.5, -1))).isFalse();
		assertThat(segment.isPointOnPath(Vector2.fromXY(1.0001, 0))).isFalse();
	}


	@Test
	public void testGetPathPoints()
	{
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(segment.getPathStart()).isEqualTo(Vector2.fromXY(-1, -1));
		assertThat(segment.getPathCenter()).isEqualTo(Vector2.zero());
		assertThat(segment.getPathEnd()).isEqualTo(Vector2.fromXY(1, 1));
	}


	@Test
	public void testGetPathLength()
	{
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 1));
		assertThat(segment.getLength()).isCloseTo(2, within(1e-6));
		segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		assertThat(segment.getLength()).isCloseTo(SumatraMath.sqrt(8), within(1e-6));
	}


	@Test
	public void testStepAlongPath()
	{
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));

		assertThat(segment.stepAlongPath(0)).isEqualTo(Vector2.fromXY(-1, -1));
		assertThat(segment.stepAlongPath(SumatraMath.sqrt(2))).isEqualTo(Vector2.zero());
		assertThat(segment.stepAlongPath(SumatraMath.sqrt(8))).isEqualTo(Vector2.fromXY(1, 1));
	}


	@Test
	public void checkCompliance()
	{
		var segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		IBoundedPathComplianceChecker.checkCompliance(segment, false);
		segment = Lines.segmentFromPoints(Vector2.fromXY(-1, -1), Vector2.fromXY(1, 1));
		IBoundedPathComplianceChecker.checkCompliance(segment, false);
		segment = Lines.segmentFromOffset(Vector2.zero(), Vector2.fromX(1));
		IBoundedPathComplianceChecker.checkCompliance(segment, false);
	}
}
