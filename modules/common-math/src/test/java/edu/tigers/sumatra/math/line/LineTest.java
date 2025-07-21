/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Lukas Magel
 */
public class LineTest extends AbstractLineTest
{
	private final static LineConstructor lineConstructor = dV -> Line.fromDirection(Vector2f.ZERO_VECTOR, dV);


	@Test
	public void testFromPoints()
	{
		IVector2 a = Vector2.fromXY(0, 0);
		IVector2 b = Vector2.fromXY(1, 1);
		IVector2 aToB = b.subtractNew(a);

		ILine line = Line.fromPoints(a, b);

		/*
		 * The support vector should be equal to vector a
		 */
		assertThat(line.supportVector()).isEqualTo(a);

		/*
		 * But should not be the same instance
		 */
		assertThat(line.supportVector()).isNotSameAs(a);
		assertThat(line.directionVector().isParallelTo(aToB)).isTrue();

		line = Line.fromPoints(a, a);
		assertThat(line.supportVector()).isEqualTo(a);
		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testFromDirection()
	{
		IVector2 sV = Vector2.fromXY(0, 0);
		IVector2 dV = Vector2.fromXY(1, 0);

		ILine line = Line.fromDirection(sV, dV);

		assertThat(sV)
				.isEqualTo(line.supportVector())
				.isNotSameAs(line.supportVector());
		/*
		 * The line shouldn't use the same vector instance to avoid side effects
		 */
		assertThat(dV).isNotSameAs(line.directionVector());
		assertThat(dV.isParallelTo(line.directionVector())).isTrue();

		line = Line.fromDirection(sV, Vector2f.ZERO_VECTOR);
		assertThat(line.supportVector()).isEqualTo(sV);
		assertThat(line.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testDirectionVectorIsFlipped()
	{
		IVector2 sV = Vector2f.ZERO_VECTOR;
		IVector2 dVPosY = Vector2.fromXY(10, 30).normalize();
		IVector2 dVNegY = dVPosY.multiplyNew(-1.0d).normalize();

		ILine linePosY = Line.fromDirection(sV, dVPosY);
		assertThat(linePosY.directionVector()).isEqualTo(dVPosY);

		ILine lineNegY = Line.fromDirection(sV, dVNegY);
		assertThat(lineNegY.directionVector()).isEqualTo(dVNegY.multiplyNew(-1.0d));

		IVector2 pos = Vector2.fromXY(10, 20);
		IVector2 neg = Vector2.fromXY(42, -24);
		IVector2 dVPos = pos.subtractNew(neg).normalize();

		linePosY = Line.fromPoints(neg, pos);
		assertThat(linePosY.directionVector()).isEqualTo(dVPos);

		lineNegY = Line.fromPoints(pos, neg);
		assertThat(lineNegY.directionVector()).isEqualTo(dVPos);
	}


	@Test
	public void testIsValid()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 zeroVector = Vector2f.ZERO_VECTOR;
		IVector2 nonZeroVector = Vector2.fromXY(Math.PI, Math.E);

		ILine validLine = Line.fromDirection(sV, nonZeroVector);
		assertThat(validLine.isValid()).isTrue();
		validLine = Line.fromPoints(zeroVector, sV);
		assertThat(validLine.isValid()).isTrue();

		ILine invalidLine = Line.fromDirection(sV, zeroVector);
		assertThat(invalidLine.isValid()).isFalse();
		invalidLine = Line.fromPoints(zeroVector, zeroVector);
		assertThat(invalidLine.isValid()).isFalse();
	}


	@Test
	public void testEquals()
	{
		IVector2 sV = Vector2.fromXY(10, 20);

		IVector2 dV = Vector2.fromAngle(Math.PI / 4);
		IVector2 orthogonalDV = dV.turnNew(Math.PI / 2);
		IVector2 oppositeDV = dV.turnNew(Math.PI);

		ILine line = Line.fromDirection(sV, dV);
		assertThat(line)
				.isEqualTo(line)
				.isNotNull()
				.isNotEqualTo(new Object())
				.hasSameHashCodeAs(line);

		ILine other = Line.fromDirection(sV, dV);
		assertThat(line)
				.isEqualTo(other)
				.hasSameHashCodeAs(other);

		other = Line.fromDirection(sV.multiplyNew(-1.0d), dV);
		assertThat(line).isNotEqualTo(other);

		other = Line.fromDirection(sV, orthogonalDV);
		assertThat(line).isNotEqualTo(other);

		other = Line.fromDirection(sV, oppositeDV);
		assertThat(line)
				.isEqualTo(other)
				.hasSameHashCodeAs(other);

		other = Line.fromDirection(sV.addNew(dV), dV);
		assertThat(line)
				.isEqualTo(other)
				.hasSameHashCodeAs(other);
	}


	@Test
	public void testEqualsInvalidLine()
	{
		IVector2 sV1 = Vector2.fromXY(10, 20);
		IVector2 sV2 = Vector2.fromXY(20, 10);

		ILine properLine = Line.fromDirection(sV1, Vector2f.X_AXIS);

		ILine zeroLine1 = Line.fromDirection(sV1, Vector2f.ZERO_VECTOR);
		ILine zeroLine1Copy = Line.fromDirection(sV1, Vector2f.ZERO_VECTOR);

		ILine zeroLine2 = Line.fromDirection(sV2, Vector2f.ZERO_VECTOR);

		assertThat(properLine).isNotEqualTo(zeroLine1);

		assertThat(zeroLine1)
				.isEqualTo(zeroLine1Copy)
				.hasSameHashCodeAs(zeroLine1Copy)
				.isNotEqualTo(zeroLine2);
	}


	@Test
	public void testNewWithoutCopy()
	{
		IVector2 sV = Vector2.fromXY(1, 2);
		IVector2 positiveDV = Vector2.fromAngle(1.5d);
		IVector2 negativeDV = Vector2.fromAngle(-1.5d);

		ILine posLine = Line.createNewWithoutCopy(sV, positiveDV);

		assertThat(posLine.supportVector()).isEqualTo(sV);
		assertThat(posLine.directionVector()).isEqualTo(positiveDV);

		ILine negativeLine = Line.createNewWithoutCopy(sV, negativeDV);

		assertThat(negativeLine.supportVector()).isEqualTo(sV);
		assertThat(negativeLine.directionVector()).isEqualTo(negativeDV.multiplyNew(-1.0d));
	}


	@Test
	public void testCopy()
	{
		ILine original = Line.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		ILine copy = original.copy();

		assertThat(original.supportVector()).isEqualTo(copy.supportVector());
		assertThat(original.supportVector()).isNotSameAs(copy.supportVector());

		assertThat(original.directionVector()).isEqualTo(copy.directionVector());
		assertThat(original.directionVector()).isNotSameAs(copy.directionVector());
	}


	@Test
	public void testDistanceTo()
	{
		IVector2 sV = Vector2.fromXY(0, 0);
		IVector2 dV = Vector2.fromXY(0, 1);
		ILine line = Line.fromDirection(sV, dV);
		ILine invalidLine = Line.fromDirection(sV, Vector2f.ZERO_VECTOR);

		for (int i = -10; i <= 10; i += 1)
		{
			assertThat(line.distanceTo(Vector2.fromXY(1, i))).isCloseTo(1, within(ACCURACY));
			assertThat(line.distanceTo(Vector2.fromXY(0, i))).isCloseTo(0, within(ACCURACY));
			assertThat(line.distanceTo(Vector2.fromXY(i, 0))).isCloseTo(Math.abs(i), within(ACCURACY));

			assertThat(invalidLine.distanceTo(Vector2.fromXY(1, i))).isCloseTo(Math.sqrt(1 + i * i), within(ACCURACY));
		}
	}


	@Test
	public void testGetYIntercept()
	{
		for (int degAngle = -90; degAngle <= 90; degAngle += 10)
		{
			double angle = Math.toRadians(degAngle);
			IVector2 sV = Vector2.fromXY(1, 0);
			IVector2 dV = Vector2.fromAngle(angle);

			ILine line = Line.fromDirection(sV, dV);
			if (Math.abs(degAngle) == 90)
			{
				assertThat(line.getYIntercept()).isNotPresent();
			} else
			{
				double expected = SumatraMath.tan(-angle);
				assertThat(line.getYIntercept()).isPresent();
				assertThat(line.getYIntercept().get()).isCloseTo(expected, within(ACCURACY));
			}
		}
	}


	@Test
	public void testGetYInterceptForInvalidLine()
	{
		ILine invalidLine = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		assertThat(invalidLine.getYIntercept()).isNotPresent();
	}


	@Test
	public void testGetXYValue()
	{
		for (int degAngle = 0; degAngle <= 360; degAngle += 10)
		{
			double angle = Math.toRadians(degAngle);
			ILine line = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromAngle(angle));

			Optional<Double> xValue = line.getXValue(1);
			if (degAngle % 180 == 0)
			{
				// Line is horizontal
				assertThat(xValue).isNotPresent();
			} else
			{
				double expected = 1 / SumatraMath.tan(angle);
				assertThat(xValue).isPresent();
				assertThat(xValue.get()).isCloseTo(expected, within(ACCURACY));
			}

			Optional<Double> yValue = line.getYValue(1);
			if ((degAngle + 90) % 180 == 0)
			{
				// Line is vertical
				assertThat(yValue).isNotPresent();
			} else
			{
				double expected = SumatraMath.tan(angle);
				assertThat(yValue).isPresent();
				assertThat(yValue.get()).isCloseTo(expected, within(ACCURACY));
			}
		}
	}


	@Test
	public void testGetXYValueForInvalidLine()
	{
		ILine zeroLine = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		assertThat(zeroLine.getXValue(1)).isNotPresent();
		assertThat(zeroLine.getYValue(1)).isNotPresent();
	}


	@Test
	public void testGetOrthogonalLine()
	{
		for (int degAngle = 0; degAngle <= 360; degAngle += 10)
		{
			double radAngle = Math.toRadians(degAngle);

			IVector2 sV = Vector2.fromXY(degAngle, -degAngle);
			IVector2 dV = Vector2.fromAngle(radAngle);

			ILine line = Line.fromDirection(sV, dV);

			IVector2 turnedDV = dV.turnNew(Math.PI / 2);
			ILine turnedLine = line.getOrthogonalLine();

			assertThat(turnedLine.supportVector()).isEqualTo(sV);
			assertThat(turnedLine.directionVector().isParallelTo(turnedDV)).isTrue();
		}
	}


	@Test
	public void testGetOrthogonalLineForInvalidLine()
	{
		ILine zeroLine = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2f.ZERO_VECTOR);
		ILine rotatedZeroLine = zeroLine.getOrthogonalLine();

		assertThat(rotatedZeroLine.directionVector()).isEqualTo(Vector2f.ZERO_VECTOR);
	}


	@Test
	public void testIsPointOnLine()
	{
		ILine line = Line.fromDirection(Vector2f.ZERO_VECTOR, Vector2.fromXY(3, 0));

		IVector2 point = Vector2.fromXY(Double.MIN_VALUE / 2.0d, 0);
		assertThat(line.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(-IPath.LINE_MARGIN * 4, 0);
		assertThat(line.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(1, IPath.LINE_MARGIN * 4);
		assertThat(line.isPointOnPath(point)).isFalse();

		point = Vector2.fromXY(1, 0);
		assertThat(line.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(1, IPath.LINE_MARGIN / 4);
		assertThat(line.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(3 + IPath.LINE_MARGIN * 4, 0);
		assertThat(line.isPointOnPath(point)).isTrue();

		point = Vector2.fromXY(Double.MAX_VALUE / 2.0d, 0);
		assertThat(line.isPointOnPath(point)).isTrue();
	}


	@Test
	public void testIsPointOnInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 56);
		ILine invalidLine = Line.fromDirection(sV, Vector2f.ZERO_VECTOR);

		assertThat(invalidLine.isPointOnPath(Vector2f.ZERO_VECTOR)).isFalse();
		assertThat(invalidLine.isPointOnPath(Vector2.fromXY(1, 2))).isFalse();
		assertThat(invalidLine.isPointOnPath(sV)).isTrue();
	}


	@Test
	public void testClosestPointOnLine()
	{
		IVector2 sV = Vector2.fromXY(0, 1);
		IVector2 dV = Vector2.fromXY(1, 1);
		ILine line = Line.fromDirection(sV, dV);
		ILine invalidLine = Line.fromDirection(sV, Vector2f.ZERO_VECTOR);


		IVector2 curPoint = Vector2.fromXY(0, 0);
		IVector2 step = Vector2.fromXY(0.1d, 0.1d);

		for (int i = 0; i <= 100; i++)
		{
			curPoint = curPoint.addNew(step);
			IVector2 leadPoint = line.closestPointOnPath(curPoint);
			IVector2 leadPointForInvalidLine = invalidLine.closestPointOnPath(curPoint);

			IVector2 curPointToLead = leadPoint.subtractNew(curPoint);
			assertThat(curPointToLead.isParallelTo(dV.getNormalVector())).isTrue();
			/*
			 * curPoint and the line run parallel and always have a distance of Math.sqrt(2) / 2 mm
			 * since the lines are 1 mm apart in y direction
			 */
			assertThat(curPointToLead.getLength()).isCloseTo(Math.sqrt(2) / 2, within(ACCURACY));
			assertThat(leadPointForInvalidLine).isEqualTo(sV);
		}
	}


	@Test
	public void testToLine()
	{
		IVector2 sV = Vector2.fromXY(4, 2);
		IVector2 dV = Vector2.fromXY(1, 3);

		ILine line = Line.fromDirection(sV, dV);
		ILine copy = line.toLine();

		assertThat(line.supportVector()).isEqualTo(copy.supportVector());
		assertThat(line.directionVector()).isEqualTo(copy.directionVector());
	}


	@Test
	public void testGetSlope()
	{
		doTestGetSlope(lineConstructor);
	}


	@Test
	public void testIsParallelTo()
	{
		doTestIsParallelTo(lineConstructor);
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
	public void testClosestPointOnPath()
	{
		var line = Lines.lineFromDirection(Vector2.zero(), Vector2.fromX(1));

		assertThat(line.closestPointOnPath(Vector2.fromXY(2, 1))).isEqualTo(Vector2.fromX(2));
		assertThat(line.closestPointOnPath(Vector2.fromXY(2, 0))).isEqualTo(Vector2.fromX(2));
		assertThat(line.closestPointOnPath(Vector2.fromXY(2, -1))).isEqualTo(Vector2.fromX(2));
		assertThat(line.closestPointOnPath(Vector2.fromXY(358382, -1))).isEqualTo(Vector2.fromX(358382));
	}


	@Test
	public void testIsPointOnPath()
	{
		var line = Lines.lineFromDirection(Vector2.zero(), Vector2.fromX(1));

		assertThat(line.isPointOnPath(Vector2.fromXY(2, 1))).isFalse();
		assertThat(line.isPointOnPath(Vector2.fromXY(2, 0.0001))).isFalse();
		assertThat(line.isPointOnPath(Vector2.fromXY(2, 0))).isTrue();
		assertThat(line.isPointOnPath(Vector2.fromXY(2, -0.0001))).isFalse();
		assertThat(line.isPointOnPath(Vector2.fromXY(2, -1))).isFalse();
		assertThat(line.isPointOnPath(Vector2.fromXY(358382, -1))).isFalse();
	}

}