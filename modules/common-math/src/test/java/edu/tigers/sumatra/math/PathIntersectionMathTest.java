/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static edu.tigers.sumatra.math.vector.Vector2.fromXY;
import static org.assertj.core.api.Assertions.assertThat;


class PathIntersectionMathTest
{

	@Test
	void intersectLineAndLine()
	{
		var directionVector = Vector2f.X_AXIS;
		var line = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, directionVector);

		for (int degAngle = 0; degAngle <= 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			var curDirectionVector = directionVector.turnNew(radAngle);
			var intersectionLine = Lines.lineFromDirection(Vector2.fromXY(0, 1), curDirectionVector);

			var intersection = line.intersect(intersectionLine).asOptional();
			var inverseIntersection = intersectionLine.intersect(line).asOptional();
			assertThat(intersection).isEqualTo(inverseIntersection);

			if (degAngle % 180 == 0)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = 1 / SumatraMath.tan(-radAngle);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(Vector2.fromXY(xVal, 0));
			}
		}
	}


	@Test
	void intersectLineAndLineInvalid()
	{
		var validLine = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var invalidLineA = Lines.lineFromDirection(Vector2.fromXY(10, 1), Vector2f.ZERO_VECTOR);
		var invalidLineB = Lines.lineFromDirection(Vector2.fromXY(1, 12), Vector2f.ZERO_VECTOR);

		assertThat(invalidLineA.intersect(validLine).asOptional()).isEmpty();
		assertThat(invalidLineA.intersect(invalidLineB).asOptional()).isEmpty();
		assertThat(invalidLineA.intersect(invalidLineA).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndLineParallel()
	{
		// Horizontal line
		var reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var line = Lines.lineFromDirection(Vector2f.fromY(1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromY(0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromY(0), Vector2f.X_AXIS);
		assertThat(reference.intersect(line).asOptional()).contains(Vector2.fromX(0.5));
		line = Lines.lineFromDirection(Vector2f.fromY(-0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromY(-1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();

		// Vertical line
		reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);
		line = Lines.lineFromDirection(Vector2f.fromX(1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromX(0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromX(0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(line).asOptional()).contains(Vector2.fromY(0.5));
		line = Lines.lineFromDirection(Vector2f.fromX(-0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
		line = Lines.lineFromDirection(Vector2f.fromX(-1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(line).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndHalfLine()
	{
		var directionVector = Vector2f.X_AXIS;
		var halfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, directionVector);

		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			ILine line = Lines.lineFromDirection(Vector2.fromXY(0, 1), directionVector.turnToNew(radAngle));

			var intersection = line.intersect(halfLine).asOptional();
			var inverseIntersection = halfLine.intersect(line).asOptional();
			assertThat(intersection).isEqualTo(inverseIntersection);

			if (degAngle < 90 || (180 <= degAngle && degAngle < 270))
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = SumatraMath.tan(radAngle - Math.PI / 2);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(Vector2.fromXY(xVal, 0));
			}
		}
	}


	@Test
	void intersectLineAndHalfLineInvalid()
	{
		var properHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		var invalidHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		var properLine = Lines.lineFromDirection(Vector2.fromXY(10, 1), Vector2f.Y_AXIS);
		var invalidLine = Lines.lineFromDirection(Vector2.fromXY(1, 12), Vector2f.ZERO_VECTOR);

		assertThat(properHalfLine.intersect(invalidLine).asOptional()).isEmpty();
		assertThat(invalidHalfLine.intersect(properLine).asOptional()).isEmpty();
		assertThat(invalidHalfLine.intersect(invalidLine).asOptional()).isEmpty();

		assertThat(invalidLine.intersect(properHalfLine).asOptional()).isEmpty();
		assertThat(properLine.intersect(invalidHalfLine).asOptional()).isEmpty();
		assertThat(invalidLine.intersect(invalidHalfLine).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndHalfLineParallel()
	{
		// Horizontal line
		var reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var halfLine = Lines.halfLineFromDirection(Vector2f.fromY(1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(0), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2.fromX(0.5));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.5), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2f.fromX(1.0));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(-0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(-1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();

		// Vertical line
		reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);

		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2.fromY(0.5));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(0.5), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2f.fromY(1.0));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(-0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(-1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndLineSegment()
	{
		ILine line = Lines.lineFromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			var start = fromXY(0, 1);
			var end = fromXY(2, 0).turnToNew(radAngle).add(fromXY(0, 1));
			var segment = Lines.segmentFromPoints(start, end);
			var intersection = segment.intersect(line).asOptional();
			if (degAngle < 210 || degAngle > 330)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(expected);
			}
		}

		var intersections = line.intersect(Lines.segmentFromPoints(Vector2.fromY(1), Vector2.fromY(-1))).asOptional();
		assertThat(intersections).contains(Vector2.zero());

		intersections = line.intersect(Lines.segmentFromPoints(Vector2.fromY(1), Vector2.fromY(0))).asOptional();
		assertThat(intersections).contains(Vector2.zero());

		intersections = line.intersect(Lines.segmentFromPoints(Vector2.fromY(-1), Vector2.fromY(0))).asOptional();
		assertThat(intersections).contains(Vector2.zero());

		intersections = line.intersect(Lines.segmentFromPoints(Vector2.fromY(1), Vector2.fromY(0.1))).asOptional();
		assertThat(intersections).isEmpty();

		intersections = line.intersect(Lines.segmentFromPoints(Vector2.fromY(-1), Vector2.fromY(-0.1))).asOptional();
		assertThat(intersections).isEmpty();

	}


	@Test
	void intersectLineAndLineSegmentInvalid()
	{
		var properLine = Lines.lineFromDirection(fromXY(1, 1), Vector2f.X_AXIS);
		var invalidLine = Lines.lineFromDirection(fromXY(1, 0), Vector2f.ZERO_VECTOR);

		var start = fromXY(1, -1);
		var end = fromXY(1, 2);
		var properSegment = Lines.segmentFromPoints(start, end);
		var invalidSegment = Lines.segmentFromPoints(start, start);

		assertThat(properSegment.intersect(invalidLine).asOptional()).isEmpty();
		assertThat(invalidSegment.intersect(properLine).asOptional()).isEmpty();
		assertThat(invalidSegment.intersect(invalidLine).asOptional()).isEmpty();

		assertThat(invalidLine.intersect(properSegment).asOptional()).isEmpty();
		assertThat(properLine.intersect(invalidSegment).asOptional()).isEmpty();
		assertThat(invalidLine.intersect(invalidSegment).asOptional()).isEmpty();

		ILineSegment invalidButOnLine = Lines.segmentFromPoints(properLine.supportVector(), properLine.supportVector());
		assertThat(properLine.intersect(invalidButOnLine).asOptional()).contains(properLine.supportVector());
		ILineSegment properButOnInvalidLine = Lines.segmentFromOffset(invalidLine.supportVector(), Vector2f.Y_AXIS);
		assertThat(invalidLine.intersect(properButOnInvalidLine).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndLineSegmentParallel()
	{
		// Horizontal line
		var reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var segment = Lines.segmentFromOffset(Vector2f.fromY(1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromY(0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromY(0), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).contains(Vector2.fromX(0.5));
		segment = Lines.segmentFromOffset(Vector2f.fromX(0.5), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).contains(Vector2f.fromX(1.0));
		segment = Lines.segmentFromOffset(Vector2f.fromY(-0.1), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromY(-1.0), Vector2f.X_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();

		// Vertical line
		reference = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);

		segment = Lines.segmentFromOffset(Vector2f.fromX(1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromX(0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromX(0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).contains(Vector2.fromY(0.5));
		segment = Lines.segmentFromOffset(Vector2f.fromY(0.5), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).contains(Vector2f.fromY(1.0));
		segment = Lines.segmentFromOffset(Vector2f.fromX(-0.1), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromX(-1.0), Vector2f.Y_AXIS);
		assertThat(reference.intersect(segment).asOptional()).isEmpty();
	}


	@Test
	void intersectLineAndCircle()
	{
		var circle = Circle.createCircle(Vector2.fromXY(-300, 200), 100);

		var line = Lines.lineFromPoints(Vector2.fromXY(400, 400), Vector2.fromXY(0, 300));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-400, 200), Vector2.fromXY(-211.765, 247.059));

		line = Lines.lineFromPoints(Vector2.fromXY(-700, 500), Vector2.fromXY(-500, 300));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-300, 100), Vector2.fromXY(-400, 200));

		line = Lines.lineFromPoints(Vector2.fromXY(-400, 600), Vector2.fromXY(-400, 100));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-400, 200));

		line = Lines.lineFromPoints(Vector2.fromXY(100, 200), Vector2.fromXY(0, 100));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndCircle(line, circle).asList()).isEmpty();
	}


	@Test
	void intersectLineAndCircleInvalid()
	{
		var properCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		var invalidCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 0);

		var properLine = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		var invalidLine = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(properCircle.intersect(invalidLine).asList()).isEmpty();
		assertThat(invalidCircle.intersect(properLine).asList()).isEmpty();
		assertThat(invalidCircle.intersect(invalidLine).asList()).isEmpty();

		assertThat(invalidLine.intersect(properCircle).asList()).isEmpty();
		assertThat(properLine.intersect(invalidCircle).asList()).isEmpty();
		assertThat(invalidLine.intersect(invalidCircle).asList()).isEmpty();
	}


	@Test
	void intersectLineAndArc()
	{
		var arc = Arc.createArc(Vector2.fromXY(-300, 200), 100, 0, AngleMath.PI_TWO);

		var line = Lines.lineFromPoints(Vector2.fromXY(400, 400), Vector2.fromXY(0, 300));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-400, 200), Vector2.fromXY(-211.765, 247.059));

		line = Lines.lineFromPoints(Vector2.fromXY(-700, 500), Vector2.fromXY(-500, 300));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-300, 100), Vector2.fromXY(-400, 200));

		line = Lines.lineFromPoints(Vector2.fromXY(-400, 600), Vector2.fromXY(-400, 100));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(-400, 200));

		line = Lines.lineFromPoints(Vector2.fromXY(100, 200), Vector2.fromXY(0, 100));
		Assertions.assertThat(PathIntersectionMath.intersectLineAndArc(line, arc).asList()).isEmpty();
	}


	@Test
	void intersectLineAndArcInvalid()
	{
		var properArc = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);
		var invalidArc = Arc.createArc(Vector2f.ZERO_VECTOR, 0, 0, AngleMath.PI);

		var properLine = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		var invalidLine = Lines.lineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(properArc.intersect(invalidLine).asList()).isEmpty();
		assertThat(invalidArc.intersect(properLine).asList()).isEmpty();
		assertThat(invalidArc.intersect(invalidLine).asList()).isEmpty();

		assertThat(invalidLine.intersect(properArc).asList()).isEmpty();
		assertThat(properLine.intersect(invalidArc).asList()).isEmpty();
		assertThat(invalidLine.intersect(invalidArc).asList()).isEmpty();
	}


	@Test
	void intersectHalfLineAndHalfLine()
	{
		var lineA = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var sV = Vector2.fromXY(0, 1);
		var dV = Vector2f.X_AXIS;
		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			var lineB = Lines.halfLineFromDirection(sV, dV.turnToNew(radAngle));

			var intersection = lineA.intersect(lineB).asOptional();
			var inverseIntersection = lineB.intersect(lineA).asOptional();
			assertThat(intersection).isEqualTo(inverseIntersection);

			if (degAngle < 270)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				IVector2 expected = Vector2.fromXY(SumatraMath.tan(radAngle - Math.PI * 3 / 2), 0);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(expected);
			}
		}
	}


	@Test
	void intersectHalfLineAndHalfLineInvalid()
	{
		var validLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var invalidLineA = Lines.halfLineFromDirection(Vector2.fromXY(10, 25), Vector2f.ZERO_VECTOR);
		var invalidLineB = Lines.halfLineFromDirection(Vector2.fromXY(20, 11), Vector2f.ZERO_VECTOR);

		assertThat(validLine.intersect(invalidLineA).asOptional()).isEmpty();
		assertThat(invalidLineA.intersect(invalidLineB).asOptional()).isEmpty();
		assertThat(invalidLineA.intersect(invalidLineA).asOptional()).isEmpty();
	}


	@Test
	void intersectHalfLineAndHalfLineParallel()
	{
		var dir = Vector2f.X_AXIS;
		var dirInv = dir.multiplyNew(-1);
		IHalfLine reference = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, dir);

		var halfLine = Lines.halfLineFromDirection(Vector2f.fromY(1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(-0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(-1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();

		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.5), dir);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2f.fromX(1.0));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.5), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2.fromX(0.25));
		halfLine = Lines.halfLineFromDirection(Vector2.fromX(-0.1), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
	}


	@Test
	void intersectHalfLineAndLineSegment()
	{
		IHalfLine halfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 start = fromXY(0, 1);
			IVector2 end = fromXY(2, 0).turnToNew(radAngle).add(fromXY(0, 1));
			ILineSegment segment = Lines.segmentFromPoints(start, end);
			var intersection = segment.intersect(halfLine).asOptional();
			if (degAngle < 270 || degAngle > 330)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(expected);
			}
		}
	}


	@Test
	void intersectHalfLineAndLineSegmentInvalid()
	{
		var properHalfLine = Lines.halfLineFromDirection(fromXY(1, 1), Vector2f.X_AXIS);
		var invalidHalfLine = Lines.halfLineFromDirection(fromXY(1, 0), Vector2f.ZERO_VECTOR);

		var start = fromXY(1, -1);
		var end = fromXY(1, 2);
		var properSegment = Lines.segmentFromPoints(start, end);
		var invalidSegment = Lines.segmentFromPoints(start, start);

		assertThat(properSegment.intersect(invalidHalfLine).asOptional()).isEmpty();
		assertThat(invalidSegment.intersect(properHalfLine).asOptional()).isEmpty();
		assertThat(invalidSegment.intersect(invalidHalfLine).asOptional()).isEmpty();

		assertThat(invalidHalfLine.intersect(properSegment).asOptional()).isEmpty();
		assertThat(properHalfLine.intersect(invalidSegment).asOptional()).isEmpty();
		assertThat(invalidHalfLine.intersect(invalidSegment).asOptional()).isEmpty();

		ILineSegment invalidButOnLine = Lines.segmentFromPoints(properHalfLine.supportVector(),
				properHalfLine.supportVector());
		assertThat(properHalfLine.intersect(invalidButOnLine).asOptional()).contains(properHalfLine.supportVector());
		ILineSegment properButOnInvalidLine = Lines.segmentFromOffset(invalidHalfLine.supportVector(), Vector2f.Y_AXIS);
		assertThat(invalidHalfLine.intersect(properButOnInvalidLine).asOptional()).isEmpty();
	}


	@Test
	void intersectHalfLineAndLineSegmentParallel()
	{
		var dir = Vector2f.X_AXIS;
		var dirInv = dir.multiplyNew(-1);
		var reference = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, dir);

		var halfLine = Lines.segmentFromOffset(Vector2f.fromY(1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(-0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(-1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();

		halfLine = Lines.segmentFromOffset(Vector2f.fromX(0.5), dir);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2f.fromX(1.0));
		halfLine = Lines.segmentFromOffset(Vector2f.fromX(0.5), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2.fromX(0.25));
		halfLine = Lines.segmentFromOffset(Vector2.fromX(-0.1), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
	}


	@Test
	void intersectHalfLineAndCircle()
	{
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 10);

		var halfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(9.9), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(10), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(10.1), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).isEmpty();


		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(-10.1), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(-10), Vector2.fromX(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(-10), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(-10), Vector2.fromX(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(-9.9), Vector2f.X_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromX(10));


		halfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromY(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(9.9), Vector2f.Y_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromY(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(10), Vector2f.Y_AXIS);
		assertThat(circle.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2.fromY(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromY(10.1), Vector2f.Y_AXIS);
		assertThat(circle.intersect(halfLine).asList()).isEmpty();
	}


	@Test
	void intersectHalfLineAndCircleInvalid()
	{
		var properCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);
		var invalidCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 0);

		var properHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		var invalidHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(properCircle.intersect(invalidHalfLine).asList()).isEmpty();
		assertThat(invalidCircle.intersect(properHalfLine).asList()).isEmpty();
		assertThat(invalidCircle.intersect(invalidHalfLine).asList()).isEmpty();

		assertThat(invalidHalfLine.intersect(properCircle).asList()).isEmpty();
		assertThat(properHalfLine.intersect(invalidCircle).asList()).isEmpty();
		assertThat(invalidHalfLine.intersect(invalidCircle).asList()).isEmpty();
	}


	@Test
	void intersectHalfLineAndArc()
	{
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 10, AngleMath.PI_HALF, -AngleMath.PI);

		var halfLine = Lines.halfLineFromDirection(Vector2f.fromXY(-1, -100), Vector2f.Y_AXIS);
		assertThat(arc.intersect(halfLine).asList()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromXY(-0.1, 100), Vector2f.Y_AXIS.multiplyNew(-1));
		assertThat(arc.intersect(halfLine).asList()).isEmpty();
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.0), Vector2f.Y_AXIS);
		assertThat(arc.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2f.fromY(10));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(0.1), Vector2f.Y_AXIS);
		assertThat(arc.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(0.1, 9.9994));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(3.5), Vector2f.Y_AXIS);
		assertThat(arc.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(3.5, 9.3675));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromXY(3.5, -10), Vector2f.Y_AXIS);
		assertThat(arc.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(3.5, 9.3675),
				Vector2f.fromXY(3.5, -9.3675));
		halfLine = Lines.halfLineFromDirection(Vector2f.fromX(3.5), Vector2f.Y_AXIS.multiplyNew(-1));
		assertThat(arc.intersect(halfLine).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(3.5, -9.3675));
	}


	@Test
	void intersectHalfLineAndArcInvalid()
	{
		var properHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.fromX(10));
		var invalidHalfLine = Lines.halfLineFromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		var properArc = Arc.createArc(Vector2f.fromX(5), 3, -AngleMath.PI_HALF, AngleMath.PI);
		var invalidArc = Arc.createArc(Vector2f.fromX(5), 0, -AngleMath.PI_HALF, AngleMath.PI);

		assertThat(properHalfLine.intersect(invalidArc).asList()).isEmpty();
		assertThat(invalidHalfLine.intersect(properArc).asList()).isEmpty();
		assertThat(invalidHalfLine.intersect(invalidArc).asList()).isEmpty();

		assertThat(invalidArc.intersect(properHalfLine).asList()).isEmpty();
		assertThat(properArc.intersect(invalidHalfLine).asList()).isEmpty();
		assertThat(invalidArc.intersect(invalidHalfLine).asList()).isEmpty();
	}


	@Test
	void intersectLineSegmentAndLineSegment()
	{
		ILineSegment segment1 = Lines.segmentFromPoints(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			var start = fromXY(0, 1);
			var end = fromXY(2, 0).turnToNew(radAngle).add(fromXY(0, 1));
			var segment2 = Lines.segmentFromPoints(start, end);
			var intersection = segment1.intersect(segment2).asOptional();
			if (degAngle < 270 || degAngle > 315)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				double xVal = -1.0d / SumatraMath.tan(radAngle);
				IVector2 expected = fromXY(xVal, 0);
				assertThat(intersection).isPresent();
				assertThat(intersection).contains(expected);
			}
		}
	}


	@Test
	void intersectLineSegmentAndLineSegmentInvalid()
	{
		var validSegment = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		var invalidSegmentA = Lines.segmentFromOffset(Vector2.fromXY(10, 1), Vector2f.ZERO_VECTOR);
		var invalidSegmentB = Lines.segmentFromOffset(Vector2.fromXY(1, 12), Vector2f.ZERO_VECTOR);

		assertThat(invalidSegmentA.intersect(validSegment).asOptional()).isEmpty();
		assertThat(invalidSegmentA.intersect(invalidSegmentB).asOptional()).isEmpty();
		assertThat(invalidSegmentA.intersect(invalidSegmentA).asOptional()).contains(invalidSegmentA.supportVector());

		var invalidOnValid = Lines.segmentFromPoints(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		assertThat(validSegment.intersect(invalidOnValid).asOptional()).contains(Vector2f.ZERO_VECTOR);
	}


	@Test
	void intersectLineSegmentAndLineSegmentParallel()
	{
		var dir = Vector2f.X_AXIS;
		var dirInv = dir.multiplyNew(-1);
		var reference = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, dir);

		var halfLine = Lines.segmentFromOffset(Vector2f.fromY(1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(-0.1), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
		halfLine = Lines.segmentFromOffset(Vector2f.fromY(-1.0), dir);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();

		halfLine = Lines.segmentFromOffset(Vector2f.fromX(0.5), dir);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2f.fromX(0.75));
		halfLine = Lines.segmentFromOffset(Vector2f.fromX(0.5), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).contains(Vector2.fromX(0.25));
		halfLine = Lines.segmentFromOffset(Vector2.fromX(-0.1), dirInv);
		assertThat(reference.intersect(halfLine).asOptional()).isEmpty();
	}


	@Test
	void intersectLineSegmentAndCircle()
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(200, -200), 100);
		ILineSegment line;

		line = Lines.segmentFromPoints(Vector2.fromXY(0, 0), Vector2.fromXY(200, -200));
		Assertions.assertThat(PathIntersectionMath.intersectLineSegmentAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(129.289, -129.289));

		line = Lines.segmentFromPoints(Vector2.fromXY(200, 0), Vector2.fromXY(0, -400));
		Assertions.assertThat(PathIntersectionMath.intersectLineSegmentAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(100, -200), Vector2.fromXY(140, -120));

		line = Lines.segmentFromPoints(Vector2.fromXY(400, -300), Vector2.fromXY(0, -300));
		Assertions.assertThat(PathIntersectionMath.intersectLineSegmentAndCircle(line, circle).asList())
				.containsExactlyInAnyOrder(Vector2.fromXY(200, -300));

		line = Lines.segmentFromPoints(Vector2.fromXY(100, -100), Vector2.fromXY(0, 0));
		Assertions.assertThat(PathIntersectionMath.intersectLineSegmentAndCircle(line, circle).asList()).isEmpty();
	}


	@Test
	void intersectLineSegmentAndCircleInvalid()
	{
		var properSegment = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, Vector2f.fromX(10));
		var invalidSegment = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		var properCircle = Circle.createCircle(Vector2f.fromX(5), 3);
		var invalidCircle = Circle.createCircle(Vector2f.fromX(5), 0);

		assertThat(properSegment.intersect(invalidCircle).asList()).isEmpty();
		assertThat(invalidSegment.intersect(properCircle).asList()).isEmpty();
		assertThat(invalidSegment.intersect(invalidCircle).asList()).isEmpty();

		assertThat(invalidCircle.intersect(properSegment).asList()).isEmpty();
		assertThat(properCircle.intersect(invalidSegment).asList()).isEmpty();
		assertThat(invalidCircle.intersect(invalidSegment).asList()).isEmpty();
	}


	@Test
	void intersectLineSegmentAndArc()
	{
		var arc = Arc.createArc(Vector2f.ZERO_VECTOR, 10, 0, AngleMath.PI);

		var segment = Lines.segmentFromOffset(Vector2f.fromXY(-100, -1), Vector2f.fromX(200));
		assertThat(arc.intersect(segment).asList()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromXY(100, -0.1), Vector2f.fromX(-200));
		assertThat(arc.intersect(segment).asList()).isEmpty();
		segment = Lines.segmentFromOffset(Vector2f.fromY(0.0), Vector2f.fromX(10));
		assertThat(arc.intersect(segment).asList()).containsExactlyInAnyOrder(Vector2f.fromX(10));
		segment = Lines.segmentFromOffset(Vector2f.fromY(0.1), Vector2f.fromX(10));
		assertThat(arc.intersect(segment).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(9.9994, 0.1));
		segment = Lines.segmentFromOffset(Vector2f.fromY(3.5), Vector2f.fromX(10));
		assertThat(arc.intersect(segment).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(9.3675, 3.5));
		segment = Lines.segmentFromOffset(Vector2f.fromXY(-10, 3.5), Vector2f.fromX(20));
		assertThat(arc.intersect(segment).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(9.3675, 3.5),
				Vector2f.fromXY(-9.3675, 3.5));
		segment = Lines.segmentFromOffset(Vector2f.fromY(3.5), Vector2f.fromX(-10));
		assertThat(arc.intersect(segment).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(-9.3675, 3.5));
		segment = Lines.segmentFromOffset(Vector2f.fromXY(-8, 3.5), Vector2f.fromX(16));
		assertThat(arc.intersect(segment).asList()).isEmpty();
	}


	@Test
	void intersectLineSegmentAndArcInvalid()
	{
		var properSegment = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, Vector2f.fromX(10));
		var invalidSegment = Lines.segmentFromOffset(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		var properArc = Arc.createArc(Vector2f.fromX(5), 3, -AngleMath.PI_HALF, AngleMath.PI);
		var invalidArc = Arc.createArc(Vector2f.fromX(5), 0, -AngleMath.PI_HALF, AngleMath.PI);

		assertThat(properSegment.intersect(invalidArc).asList()).isEmpty();
		assertThat(invalidSegment.intersect(properArc).asList()).isEmpty();
		assertThat(invalidSegment.intersect(invalidArc).asList()).isEmpty();

		assertThat(invalidArc.intersect(properSegment).asList()).isEmpty();
		assertThat(properArc.intersect(invalidSegment).asList()).isEmpty();
		assertThat(invalidArc.intersect(invalidSegment).asList()).isEmpty();
	}


	@Test
	void intersectCircleAndCircle()
	{
		var reference = Circle.createCircle(Vector2f.ZERO_VECTOR, 10);

		var circle = Circle.createCircle(Vector2.fromX(10), 3);
		assertThat(reference.intersect(circle).asList()).containsExactlyInAnyOrder(
				Vector2f.fromXY(9.55, 2.966), Vector2f.fromXY(9.55, -2.966));
		circle = Circle.createCircle(Vector2.fromXY(5, 5), 3);
		assertThat(reference.intersect(circle).asList()).containsExactlyInAnyOrder(
				Vector2f.fromXY(6.5046, 7.5954), Vector2f.fromXY(7.5954, 6.5046));
		circle = Circle.createCircle(Vector2.fromX(6.9), 3);
		assertThat(reference.intersect(circle).asList()).isEmpty();
		circle = Circle.createCircle(Vector2.fromX(7), 3);
		assertThat(reference.intersect(circle).asList()).containsExactly(Vector2f.fromX(10));
		circle = Circle.createCircle(Vector2.fromX(7.1), 3);
		assertThat(reference.intersect(circle).asList()).containsExactlyInAnyOrder(Vector2f.fromXY(9.95845, 0.91064),
				Vector2f.fromXY(9.95845, -0.91064));
	}


	@Test
	void intersectCircleAndCircleInvalid()
	{
		var valid = Circle.createCircle(Vector2f.ZERO_VECTOR, 1);

		var invalid1 = Circle.createCircle(Vector2.fromXY(10, 25), 0);
		var invalid2 = Circle.createCircle(Vector2f.ZERO_VECTOR, 0);

		assertThat(valid.intersect(invalid1).asList()).isEmpty();
		assertThat(invalid1.intersect(invalid2).asList()).isEmpty();
		assertThat(invalid1.intersect(invalid1).asList()).isEmpty();
	}


	@Test
	void intersectCircleAndArc()
	{
		var circle = Circle.createCircle(Vector2f.ZERO_VECTOR, 10);
		var intersect = Vector2f.fromXY(9.55, 2.966);

		var arc = Arc.createArc(Vector2.fromX(10), 3, 0, AngleMath.PI);
		assertThat(circle.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);

		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle(), AngleMath.PI_HALF);
		assertThat(circle.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle() + 0.01,
				AngleMath.PI_HALF);
		assertThat(circle.intersect(arc).asList()).isEmpty();

		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle());
		assertThat(circle.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle() - 0.01);
		assertThat(circle.intersect(arc).asList()).isEmpty();
	}


	@Test
	void intersectCircleAndArcInvalid()
	{
		var properCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 10);
		var invalidCircle = Circle.createCircle(Vector2f.ZERO_VECTOR, 0);

		var properArc = Arc.createArc(Vector2f.fromX(10), 2, 0, 1.5 * Math.PI);
		var invalidArc = Arc.createArc(Vector2f.fromX(10), 0, 0, 1.5 * Math.PI);

		assertThat(properCircle.intersect(invalidArc).asList()).isEmpty();
		assertThat(invalidCircle.intersect(properArc).asList()).isEmpty();
		assertThat(invalidCircle.intersect(invalidArc).asList()).isEmpty();

		assertThat(invalidArc.intersect(properCircle).asList()).isEmpty();
		assertThat(properArc.intersect(invalidCircle).asList()).isEmpty();
		assertThat(invalidArc.intersect(invalidCircle).asList()).isEmpty();
	}


	@Test
	void intersectArcAndArc()
	{
		var reference = Arc.createArc(Vector2f.ZERO_VECTOR, 10, 0, AngleMath.PI);
		var intersect = Vector2f.fromXY(9.55, 2.966);

		// Forward intersection in the middle
		var arc = Arc.createArc(Vector2.fromX(10), 3, 0, AngleMath.PI);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);

		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle(), AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle() + 0.01,
				AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle());
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle() - 0.01);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		// Reverse intersection in the middle
		reference = Arc.createArc(Vector2f.ZERO_VECTOR, 10, AngleMath.PI, -AngleMath.PI);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, AngleMath.PI);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);

		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle(), AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle() + 0.01,
				AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle());
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle() - 0.01);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		// Forward intersection exact
		reference = Arc.createArc(Vector2f.ZERO_VECTOR, 10, 0, intersect.subtractNew(Vector2f.ZERO_VECTOR).getAngle());
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, AngleMath.PI);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);

		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle(), AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle() + 0.01,
				AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle());
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle() - 0.01);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		// Reverse intersection exact
		reference = Arc.createArc(Vector2f.ZERO_VECTOR, 10, AngleMath.PI_HALF,
				-(AngleMath.PI_HALF - intersect.subtractNew(Vector2f.ZERO_VECTOR).getAngle()));
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, AngleMath.PI);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);

		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle(), AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, intersect.subtractNew(Vector2.fromX(10)).getAngle() + 0.01,
				AngleMath.PI_HALF);
		assertThat(reference.intersect(arc).asList()).isEmpty();

		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle());
		assertThat(reference.intersect(arc).asList()).containsExactlyInAnyOrder(intersect);
		arc = Arc.createArc(Vector2.fromX(10), 3, 0, intersect.subtractNew(Vector2.fromX(10)).getAngle() - 0.01);
		assertThat(reference.intersect(arc).asList()).isEmpty();
	}


	@Test
	void intersectArcAndArcInvalid()
	{
		var valid = Arc.createArc(Vector2f.ZERO_VECTOR, 1, 0, AngleMath.PI);

		var invalid1 = Arc.createArc(Vector2.fromXY(10, 25), 0, 0, -AngleMath.PI);
		var invalid2 = Arc.createArc(Vector2f.ZERO_VECTOR, 0, 0, AngleMath.PI);

		assertThat(valid.intersect(invalid1).asList()).isEmpty();
		assertThat(invalid1.intersect(invalid2).asList()).isEmpty();
		assertThat(invalid1.intersect(invalid1).asList()).isEmpty();
	}


	@Test
	void randomIntersections()
	{
		var rand = new Random(42);

		for (int i = 0; i < 100000; ++i)
		{
			var intersect = Vector2f.fromXY(rand.nextDouble(), rand.nextDouble());
			var path1 = buildRandomPath(rand, intersect);
			var path2 = Lines.lineFromPoints(intersect, Vector2f.fromXY(rand.nextDouble(), rand.nextDouble()));
			if (path1.isValid() && path2.isValid())
			{
				if (path1 instanceof ILineBase line1 && line1.isParallelTo(path2))
				{
					continue;
				}
				assertThat(path1.intersect(path2).asList()).contains(intersect);
			}
		}
	}


	private IPath buildRandomPath(Random rand, IVector2 intersectionPoint)
	{
		switch (rand.nextInt(5))
		{
			case 0 ->
			{
				// Line
				return Lines.lineFromPoints(Vector2f.fromXY(rand.nextDouble(), rand.nextDouble()), intersectionPoint);
			}
			case 1 ->
			{
				//Half Line
				if (rand.nextDouble() > 0.5)
				{
					return Lines.halfLineFromPoints(Vector2f.fromXY(rand.nextDouble(), rand.nextDouble()),
							intersectionPoint);
				} else
				{
					return Lines.halfLineFromPoints(intersectionPoint,
							Vector2f.fromXY(rand.nextDouble(), rand.nextDouble()));
				}
			}
			case 2 ->
			{
				//Line Segment
				return Lines.segmentFromPoints(Vector2f.fromXY(rand.nextDouble(), rand.nextDouble()), intersectionPoint);
			}
			case 3 ->
			{
				//Circle
				var radius = 0.1 + rand.nextDouble(0.9);
				var center = intersectionPoint.addNew(Vector2f.fromAngleLength(rand.nextDouble(), radius));
				return Circle.createCircle(center, radius);
			}
			default ->
			{
				//Arc
				var radius = 0.1 + rand.nextDouble(0.9);
				var center = intersectionPoint.addNew(Vector2f.fromAngleLength(rand.nextDouble(), radius));
				var angle = intersectionPoint.subtractNew(center).getAngle();
				var cwRot = rand.nextDouble(0.9 * AngleMath.PI);
				var ccwRot = rand.nextDouble(0.9 * AngleMath.PI);
				return Arc.createArc(center, radius, angle + ccwRot, -cwRot - ccwRot);
			}
		}
	}
}