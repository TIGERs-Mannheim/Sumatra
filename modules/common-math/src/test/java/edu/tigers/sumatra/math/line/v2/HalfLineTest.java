/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Lukas Magel
 */
public class HalfLineTest extends AbstractLineTest
{
	private final static LineConstructor lineConstructor = dV -> HalfLine.fromDirection(Vector2f.ZERO_VECTOR, dV);


	@Test
	public void testFromDirection()
	{
		IVector2 supportVector = Vector2.fromXY(1, 1);
		IVector2 directionVector = Vector2.fromXY(4, 2);

		IHalfLine halfLine = HalfLine.fromDirection(supportVector, directionVector);
		assertThat(halfLine.supportVector()).isEqualTo(supportVector);
		assertThat(halfLine.supportVector() != supportVector).isTrue();
		assertThat(halfLine.directionVector().isParallelTo(directionVector)).isTrue();

		IHalfLine zeroLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.supportVector()).isEqualTo(Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIsValid()
	{
		IVector2 sV = Vector2.fromXY(1, 2);
		IVector2 dV = Vector2.fromAngle(1.5d);

		IHalfLine validLine = HalfLine.fromDirection(sV, dV);
		assertThat(validLine.isValid()).isTrue();

		IHalfLine invalidLine = HalfLine.fromDirection(sV, Vector2f.ZERO_VECTOR);
		assertThat(invalidLine.isValid()).isFalse();
	}


	@Test
	public void testCopy()
	{
		IHalfLine original = HalfLine.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		IHalfLine copy = original.copy();

		assertThat(original.supportVector()).isEqualTo(copy.supportVector());
		assertThat(original.supportVector() == copy.supportVector()).isFalse();

		assertThat(original.directionVector()).isEqualTo(copy.directionVector());
		assertThat(original.directionVector() == copy.directionVector()).isFalse();
	}


	@Test
	public void testClosestPointOnLine()
	{
		IVector2 point = Vector2f.fromXY(0, 0);

		IVector2 lineSV = Vector2f.fromXY(1, 0);
		for (int degAngle = 0; degAngle <= 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 lineDV = Vector2.fromAngle(radAngle);
			IHalfLine halfLine = HalfLine.fromDirection(lineSV, lineDV);
			IHalfLine invalidLine = HalfLine.fromDirection(lineSV, Vector2f.ZERO_VECTOR);

			IVector2 expectedClosestPoint;
			double expectedDistance;
			if (degAngle <= 90 || degAngle >= 270)
			{
				expectedClosestPoint = lineSV;
				expectedDistance = point.distanceTo(lineSV);
			} else
			{
				double radAngleAtOrigin = radAngle - Math.PI / 2;
				double distanceLinePoint = SumatraMath.cos(radAngleAtOrigin);

				expectedClosestPoint = Vector2f.X_AXIS.turnNew(radAngleAtOrigin).scaleTo(distanceLinePoint);
				expectedDistance = Math.abs(distanceLinePoint);
			}

			IVector2 actualClosestPoint = halfLine.closestPointOnLine(point);
			double actualDistance = halfLine.distanceTo(point);
			assertThat(actualClosestPoint).isEqualTo(expectedClosestPoint);
			assertThat(actualDistance).isCloseTo(expectedDistance, within(ACCURACY));

			assertThat(invalidLine.closestPointOnLine(point)).isEqualTo(lineSV);
		}
	}


	@Test
	public void testEquals()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 dV = Vector2.fromXY(Math.PI, Math.E);

		IHalfLine halfLine = HalfLine.fromDirection(sV, dV);

		assertThat(halfLine).isEqualTo(halfLine);
		assertThat(halfLine.hashCode()).isEqualTo(halfLine.hashCode());

		IHalfLine other = HalfLine.fromDirection(sV, dV.multiplyNew(-1.0d));
		assertThat(halfLine).isNotEqualTo(other);
	}


	@Test
	public void testEqualsInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 dV = Vector2.fromAngle(1.5d);

		IHalfLine properLine = HalfLine.fromDirection(sV, dV);
		IHalfLine invalidLineA = HalfLine.fromDirection(sV, Vector2f.ZERO_VECTOR);
		IHalfLine invalidLineACopy = HalfLine.fromDirection(sV, Vector2f.ZERO_VECTOR);
		IHalfLine invalidLineB = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(properLine).isNotEqualTo(invalidLineA);
		assertThat(invalidLineA).isNotEqualTo(invalidLineB);
		assertThat(invalidLineA).isEqualTo(invalidLineACopy);
	}


	@Test
	public void testEqualsContract()
	{
		EqualsVerifier.forClass(HalfLine.class)
				.suppress(Warning.NULL_FIELDS)
				.withIgnoredFields("isValid")
				.withPrefabValues(IVector2.class, Vector2.fromXY(Math.PI, Math.E), Vector2.fromXY(-21, 42))
				.verify();
	}


	@Test
	public void testIsPointInFront()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		for (int degAngle = 0; degAngle <= 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 point = Vector2f.X_AXIS.turnToNew(radAngle);
			assertThat(halfLine.isPointInFront(point)).isEqualTo(degAngle <= 90 || degAngle > 270);
			assertThat(invalidLine.isPointInFront(point)).isFalse();
		}
	}


	@Test
	public void testToLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.Y_AXIS);
		ILine line = halfLine.toLine();

		assertThat(halfLine.supportVector()).isEqualTo(line.supportVector());

		assertThat(halfLine.directionVector().isParallelTo(line.directionVector())).isTrue();
		assertThat(halfLine.directionVector().getLength()).isCloseTo(1.0, within(ACCURACY));
	}


	@Test
	public void testToLineForInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 15);
		IVector2 dV = Vector2f.ZERO_VECTOR;

		IHalfLine invalidHalfLine = HalfLine.fromDirection(sV, dV);
		ILine line = invalidHalfLine.toLine();

		assertThat(line.supportVector()).isEqualTo(sV);
		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIntersectHalfLine()
	{
		IHalfLine lineA = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		IVector2 sV = Vector2.fromXY(0, 1);
		IVector2 dV = Vector2f.X_AXIS;
		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);

			IHalfLine lineB = HalfLine.fromDirection(sV, dV.turnToNew(radAngle));

			Optional<IVector2> intersection = lineA.intersectHalfLine(lineB);
			Optional<IVector2> inverseIntersection = lineB.intersectHalfLine(lineA);
			assertThat(intersection).isEqualTo(inverseIntersection);

			if (degAngle < 270)
			{
				assertThat(intersection).isNotPresent();
			} else
			{
				IVector2 expected = Vector2.fromXY(SumatraMath.tan(radAngle - Math.PI * 3 / 2), 0);
				assertThat(intersection).isPresent();
				assertThat(intersection.get()).isEqualTo(expected);
			}
		}
	}


	@Test
	public void testIntersectHalfLineWithInvalid()
	{
		IHalfLine validLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.X_AXIS);

		IHalfLine invalidLineA = HalfLine.fromDirection(Vector2.fromXY(10, 25), Vector2f.ZERO_VECTOR);
		IHalfLine invalidLineB = HalfLine.fromDirection(Vector2.fromXY(20, 11), Vector2f.ZERO_VECTOR);
		Optional<IVector2> intersection = validLine.intersectHalfLine(invalidLineA);
		assertThat(intersection).isNotPresent();

		intersection = invalidLineA.intersectHalfLine(invalidLineB);
		assertThat(intersection).isNotPresent();

		intersection = invalidLineA.intersectHalfLine(invalidLineA);
		assertThat(intersection).isNotPresent();
	}


	@Test
	public void testIsPointOnLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(3, 0));

		IVector2 point = Vector2.fromXY(-10, 0);
		assertThat(halfLine.isPointOnLine(point)).isFalse();

		point = Vector2.fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnLine(point)).isFalse();

		point = Vector2.fromXY(1, ALine.LINE_MARGIN * 4);
		assertThat(halfLine.isPointOnLine(point)).isFalse();

		point = Vector2.fromXY(1, 0);
		assertThat(halfLine.isPointOnLine(point)).isTrue();

		point = Vector2.fromXY(1, ALine.LINE_MARGIN / 4);
		assertThat(halfLine.isPointOnLine(point)).isTrue();

		point = Vector2.fromXY(3 + ALine.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnLine(point)).isTrue();

		point = Vector2.fromXY(Double.MAX_VALUE / 2.0d, 0);
		assertThat(halfLine.isPointOnLine(point)).isTrue();


	}


	@Test
	public void testIsPointOnInvalidLine()
	{
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(invalidLine.isPointOnLine(Vector2f.ZERO_VECTOR)).isTrue();
		assertThat(invalidLine.isPointOnLine(Vector2.fromXY(1, 2))).isFalse();
		assertThat(invalidLine.isPointOnLine(Vector2.fromXY(2, 3))).isFalse();
	}


	@Test
	public void testGetSlope()
	{
		doTestGetSlope(lineConstructor);
	}


	@Test
	public void testDistanceTo()
	{
		IVector2 sV = Vector2f.ZERO_VECTOR;
		IVector2 dV = Vector2f.Y_AXIS;
		IHalfLine line = HalfLine.fromDirection(sV, dV);
		IHalfLine invalidLine = HalfLine.fromDirection(sV, Vector2f.ZERO_VECTOR);

		for (int i = -10; i <= 10; i += 1)
		{
			assertThat(line.distanceTo(Vector2.fromXY(i, 0))).isCloseTo(Math.abs(i), within(ACCURACY));

			IVector2 identical = Vector2.fromXY(0, i);
			IVector2 parallel = Vector2.fromXY(1, i);
			if (i < 0)
			{
				assertThat(line.distanceTo(identical)).isCloseTo(Math.abs(i), within(ACCURACY));
				assertThat(line.distanceTo(parallel)).isCloseTo(SumatraMath.sqrt(1 + i * i), within(ACCURACY));
			} else
			{
				assertThat(line.distanceTo(identical)).isCloseTo(0, within(ACCURACY));
				assertThat(line.distanceTo(parallel)).isCloseTo(1, within(ACCURACY));
			}

			assertThat(invalidLine.distanceTo(parallel)).isCloseTo(Math.sqrt(1 + i * i), within(ACCURACY));
		}
	}


	@Test
	public void testGetAngle()
	{
		doTestGetAngle(lineConstructor);
	}


	@Test
	public void testOrientation()
	{
		doTestOrientation(lineConstructor);
	}


	@Test
	public void testIsParallelTo()
	{
		doTestIsParallelTo(lineConstructor);
	}
}
