/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.IPathComplianceChecker;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

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
		assertThat(halfLine.supportVector()).isNotSameAs(supportVector);
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
		assertThat(original.supportVector()).isNotSameAs(copy.supportVector());

		assertThat(original.directionVector()).isEqualTo(copy.directionVector());
		assertThat(original.directionVector()).isNotSameAs(copy.directionVector());
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

			IVector2 actualClosestPoint = halfLine.closestPointOnPath(point);
			double actualDistance = halfLine.distanceTo(point);
			assertThat(actualClosestPoint).isEqualTo(expectedClosestPoint);
			assertThat(actualDistance).isCloseTo(expectedDistance, within(ACCURACY));

			assertThat(invalidLine.closestPointOnPath(point)).isEqualTo(lineSV);
		}
	}


	@Test
	public void testEquals()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 dV = Vector2.fromXY(Math.PI, Math.E);

		IHalfLine halfLine = HalfLine.fromDirection(sV, dV);

		assertThat(halfLine)
				.isEqualTo(halfLine)
				.hasSameHashCodeAs(halfLine);

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

		assertThat(invalidLineA)
				.isNotEqualTo(properLine)
				.isNotEqualTo(invalidLineB)
				.isEqualTo(invalidLineACopy);
	}


	@Test
	public void testEqualsContract()
	{
		EqualsVerifier.forClass(HalfLine.class)
				.suppress(Warning.NULL_FIELDS)
				.withIgnoredFields("valid")
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
	public void testIsPointOnLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(3, 0));

		IVector2 point = Vector2.fromXY(-10, 0);
		assertThat(halfLine.isPointOnPath(point)).isFalse();

		point = Vector2.fromXY(-IPath.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnPath(point)).isFalse();

		point = Vector2.fromXY(1, IPath.LINE_MARGIN * 4);
		assertThat(halfLine.isPointOnPath(point)).isFalse();

		point = Vector2.fromXY(1, 0);
		assertThat(halfLine.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(1, IPath.LINE_MARGIN / 4);
		assertThat(halfLine.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(3 + IPath.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(Double.MAX_VALUE / 2.0d, 0);
		assertThat(halfLine.isPointOnPath(point)).isTrue();


	}


	@Test
	public void testIsPointOnInvalidLine()
	{
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);

		assertThat(invalidLine.isPointOnPath(Vector2f.ZERO_VECTOR)).isTrue();
		assertThat(invalidLine.isPointOnPath(Vector2.fromXY(1, 2))).isFalse();
		assertThat(invalidLine.isPointOnPath(Vector2.fromXY(2, 3))).isFalse();
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


	@Test
	public void testClosestPointOnPath()
	{
		var halfLine = Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromX(1));

		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(-0.0001, 0))).isEqualTo(Vector2.zero());
		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(0, 0))).isEqualTo(Vector2.zero());
		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(2, 1))).isEqualTo(Vector2.fromX(2));
		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(2, 0))).isEqualTo(Vector2.fromX(2));
		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(2, -1))).isEqualTo(Vector2.fromX(2));
		assertThat(halfLine.closestPointOnPath(Vector2.fromXY(358382, -1))).isEqualTo(Vector2.fromX(358382));
	}


	@Test
	public void testIsPointOnPath()
	{
		var halfLine = Lines.halfLineFromDirection(Vector2.zero(), Vector2.fromX(1));

		assertThat(halfLine.isPointOnPath(Vector2.fromXY(-0.0001, 0))).isFalse();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(0, 0))).isTrue();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(2, 1))).isFalse();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(2, 0.0001))).isFalse();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(2, 0))).isTrue();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(2, -0.0001))).isFalse();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(2, -1))).isFalse();
		assertThat(halfLine.isPointOnPath(Vector2.fromXY(358382, -1))).isFalse();
	}


	@Test
	public void testCompliance()
	{
		var lines = List.of(
				Lines.halfLineFromPoints(Vector2.fromXY(1, 2), Vector2.fromXY(6, 3)),
				Lines.halfLineFromPoints(Vector2.fromXY(1, 2), Vector2.fromXY(3, 6)),
				Lines.halfLineFromPoints(Vector2.zero(), Vector2.fromXY(1, 0)),
				Lines.halfLineFromPoints(Vector2.zero(), Vector2.fromXY(-1, 0)),
				Lines.halfLineFromPoints(Vector2.zero(), Vector2.fromXY(0, 1)),
				Lines.halfLineFromPoints(Vector2.zero(), Vector2.fromXY(0, -1))
		);
		for (var line : lines)
		{
			IPathComplianceChecker.checkCompliance(line);
		}
	}


}
