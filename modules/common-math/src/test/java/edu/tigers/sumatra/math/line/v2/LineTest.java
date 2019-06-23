/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import static edu.tigers.sumatra.Present.isNotPresent;
import static edu.tigers.sumatra.Present.isPresentAnd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Lukas Magel
 */
public class LineTest extends AbstractLineTest
{
	private final static LineConstructor lineConstructor = dV -> Line.fromDirection(Vector2.ZERO_VECTOR, dV);
	
	
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
		assertEquals(line.supportVector(), a);
		
		/*
		 * But should not be the same instance
		 */
		assertFalse(line.supportVector() == a);
		assertTrue(line.directionVector().isParallelTo(aToB));
		
		line = Line.fromPoints(a, a);
		assertThat(line.supportVector(), is(a));
		assertThat(line.directionVector(), is(Vector2.ZERO_VECTOR));
	}
	
	
	@Test
	public void testFromDirection()
	{
		IVector2 sV = Vector2.fromXY(0, 0);
		IVector2 dV = Vector2.fromXY(1, 0);
		
		ILine line = Line.fromDirection(sV, dV);
		
		assertEquals(sV, line.supportVector());
		/*
		 * The line shouldn't use the same vector instance to avoid side effects
		 */
		assertFalse(sV == line.supportVector());
		assertFalse(dV == line.directionVector());
		assertTrue(dV.isParallelTo(line.directionVector()));
		
		line = Line.fromDirection(sV, Vector2.ZERO_VECTOR);
		assertThat(line.supportVector(), is(sV));
		assertThat(line.directionVector(), is(Vector2.ZERO_VECTOR));
	}
	
	
	@Test
	public void testDirectionVectorIsFlipped()
	{
		IVector2 sV = Vector2.ZERO_VECTOR;
		IVector2 dVPosY = Vector2.fromXY(10, 30).normalize();
		IVector2 dVNegY = dVPosY.multiplyNew(-1.0d).normalize();
		
		ILine linePosY = Line.fromDirection(sV, dVPosY);
		assertThat(linePosY.directionVector(), is(dVPosY));
		
		ILine lineNegY = Line.fromDirection(sV, dVNegY);
		assertThat(lineNegY.directionVector(), is(dVNegY.multiplyNew(-1.0d)));
		
		IVector2 pos = Vector2.fromXY(10, 20);
		IVector2 neg = Vector2.fromXY(42, -24);
		IVector2 dVPos = pos.subtractNew(neg).normalize();
		
		linePosY = Line.fromPoints(neg, pos);
		assertThat(linePosY.directionVector(), is(dVPos));
		
		lineNegY = Line.fromPoints(pos, neg);
		assertThat(lineNegY.directionVector(), is(dVPos));
	}
	
	
	@Test
	public void testIsValid()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 zeroVector = Vector2.ZERO_VECTOR;
		IVector2 nonZeroVector = Vector2.fromXY(Math.PI, Math.E);
		
		ILine validLine = Line.fromDirection(sV, nonZeroVector);
		assertThat(validLine.isValid(), is(true));
		validLine = Line.fromPoints(zeroVector, sV);
		assertThat(validLine.isValid(), is(true));
		
		ILine invalidLine = Line.fromDirection(sV, zeroVector);
		assertThat(invalidLine.isValid(), is(false));
		invalidLine = Line.fromPoints(zeroVector, zeroVector);
		assertThat(invalidLine.isValid(), is(false));
	}
	
	
	@Test
	public void testEquals()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		
		IVector2 dV = Vector2.fromAngle(Math.PI / 4);
		IVector2 orthogonalDV = dV.turnNew(Math.PI / 2);
		IVector2 oppositeDV = dV.turnNew(Math.PI);
		
		ILine line = Line.fromDirection(sV, dV);
		assertThat(line, is(line));
		assertNotSame(line, null);
		assertThat(line, not(new Object()));
		assertThat(line.hashCode(), is(line.hashCode()));
		
		ILine other = Line.fromDirection(sV, dV);
		assertThat(line, is(other));
		assertThat(line.hashCode(), is(other.hashCode()));
		
		other = Line.fromDirection(sV.multiplyNew(-1.0d), dV);
		assertThat(line, not(other));
		
		other = Line.fromDirection(sV, orthogonalDV);
		assertThat(line, not(other));
		
		other = Line.fromDirection(sV, oppositeDV);
		assertThat(line, is(other));
		assertThat(line.hashCode(), is(other.hashCode()));
		
		other = Line.fromDirection(sV.addNew(dV), dV);
		assertThat(line, is(other));
		assertThat(line.hashCode(), is(other.hashCode()));
	}
	
	
	@Test
	public void testEqualsInvalidLine()
	{
		IVector2 sV1 = Vector2.fromXY(10, 20);
		IVector2 sV2 = Vector2.fromXY(20, 10);
		
		ILine properLine = Line.fromDirection(sV1, Vector2.X_AXIS);
		
		ILine zeroLine1 = Line.fromDirection(sV1, Vector2.ZERO_VECTOR);
		ILine zeroLine1Copy = Line.fromDirection(sV1, Vector2.ZERO_VECTOR);
		
		ILine zeroLine2 = Line.fromDirection(sV2, Vector2.ZERO_VECTOR);
		
		assertThat(properLine, not(zeroLine1));
		
		assertThat(zeroLine1, is(zeroLine1Copy));
		assertThat(zeroLine1.hashCode(), is(zeroLine1Copy.hashCode()));
		
		assertThat(zeroLine1, not(zeroLine2));
	}
	
	
	@Test
	public void testNewWithoutCopy()
	{
		IVector2 sV = Vector2.fromXY(1, 2);
		IVector2 positiveDV = Vector2.fromAngle(1.5d);
		IVector2 negativeDV = Vector2.fromAngle(-1.5d);
		
		ILine posLine = Line.createNewWithoutCopy(sV, positiveDV);
		
		assertThat(posLine.supportVector(), is(sV));
		assertThat(posLine.directionVector(), is(positiveDV));
		assertThat(posLine.directionVector() == positiveDV, is(true));
		
		ILine negativeLine = Line.createNewWithoutCopy(sV, negativeDV);
		
		assertThat(negativeLine.supportVector(), is(sV));
		assertThat(negativeLine.directionVector(), is(negativeDV.multiplyNew(-1.0d)));
		assertThat(negativeLine.directionVector() == negativeDV, is(false));
	}
	
	
	@Test
	public void testCopy()
	{
		ILine original = Line.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		ILine copy = original.copy();
		
		assertEquals(original.supportVector(), copy.supportVector());
		assertFalse(original.supportVector() == copy.supportVector());
		
		assertEquals(original.directionVector(), copy.directionVector());
		assertFalse(original.directionVector() == copy.directionVector());
	}
	
	
	@Test
	public void testDistanceTo()
	{
		IVector2 sV = Vector2.fromXY(0, 0);
		IVector2 dV = Vector2.fromXY(0, 1);
		ILine line = Line.fromDirection(sV, dV);
		ILine invalidLine = Line.fromDirection(sV, Vector2.ZERO_VECTOR);
		
		for (int i = -10; i <= 10; i += 1)
		{
			assertThat(line.distanceTo(Vector2.fromXY(1, i)), closeTo(1, ACCURACY));
			assertThat(line.distanceTo(Vector2.fromXY(0, i)), closeTo(0, ACCURACY));
			assertThat(line.distanceTo(Vector2.fromXY(i, 0)), closeTo(Math.abs(i), ACCURACY));
			
			assertThat(invalidLine.distanceTo(Vector2.fromXY(1, i)), closeTo(Math.sqrt(1 + i * i), ACCURACY));
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
				assertThat(line.getYIntercept(), isNotPresent());
			} else
			{
				double expected = Math.tan(-angle);
				assertThat(line.getYIntercept(), isPresentAnd(closeTo(expected, ACCURACY)));
			}
		}
	}
	
	
	@Test
	public void testGetYInterceptForInvalidLine()
	{
		ILine invalidLine = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		assertThat(invalidLine.getYIntercept(), isNotPresent());
	}
	
	
	@Test
	public void testGetXYValue()
	{
		for (int degAngle = 0; degAngle <= 360; degAngle += 10)
		{
			double angle = Math.toRadians(degAngle);
			ILine line = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.fromAngle(angle));
			
			if (degAngle % 180 == 0)
			{
				// Line is horizontal
				assertThat(line.getXValue(1), isNotPresent());
			} else
			{
				double expected = 1 / Math.tan(angle);
				assertThat(line.getXValue(1), isPresentAnd(closeTo(expected, ACCURACY)));
			}
			
			if ((degAngle + 90) % 180 == 0)
			{
				// Line is vertical
				assertThat(line.getYValue(1), isNotPresent());
			} else
			{
				double expected = Math.tan(angle);
				assertThat(line.getYValue(1), isPresentAnd(closeTo(expected, ACCURACY)));
			}
		}
	}
	
	
	@Test
	public void testGetXYValueForInvalidLine()
	{
		ILine zeroLine = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		assertThat(zeroLine.getXValue(1), isNotPresent());
		assertThat(zeroLine.getYValue(1), isNotPresent());
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
			
			assertThat(turnedLine.supportVector(), is(sV));
			assertThat(turnedLine.directionVector().isParallelTo(turnedDV), is(true));
		}
	}
	
	
	@Test
	public void testGetOrthogonalLineForInvalidLine()
	{
		ILine zeroLine = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		ILine rotatedZeroLine = zeroLine.getOrthogonalLine();
		
		assertThat(rotatedZeroLine.directionVector(), is(Vector2.ZERO_VECTOR));
	}
	
	
	@Test
	public void testIsPointOnLine()
	{
		ILine line = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.fromXY(3, 0));
		
		IVector2 point = Vector2.fromXY(Double.MIN_VALUE / 2.0d, 0);
		assertThat(line.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(line.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN * 4);
		assertThat(line.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(1, 0);
		assertThat(line.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN / 4);
		assertThat(line.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(3 + ALine.LINE_MARGIN * 4, 0);
		assertThat(line.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(Double.MAX_VALUE / 2.0d, 0);
		assertThat(line.isPointOnLine(point), is(true));
	}
	
	
	@Test
	public void testIsPointOnInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 56);
		ILine invalidLine = Line.fromDirection(sV, Vector2.ZERO_VECTOR);
		
		assertThat(invalidLine.isPointOnLine(Vector2.ZERO_VECTOR), is(false));
		assertThat(invalidLine.isPointOnLine(Vector2.fromXY(1, 2)), is(false));
		assertThat(invalidLine.isPointOnLine(sV), is(true));
	}
	
	
	@Test
	public void testClosestPointOnLine()
	{
		IVector2 sV = Vector2.fromXY(0, 1);
		IVector2 dV = Vector2.fromXY(1, 1);
		ILine line = Line.fromDirection(sV, dV);
		ILine invalidLine = Line.fromDirection(sV, Vector2.ZERO_VECTOR);
		
		
		IVector2 curPoint = Vector2.fromXY(0, 0);
		IVector2 step = Vector2.fromXY(0.1d, 0.1d);
		
		for (int i = 0; i <= 100; i++)
		{
			curPoint = curPoint.addNew(step);
			IVector2 leadPoint = line.closestPointOnLine(curPoint);
			IVector2 leadPointForInvalidLine = invalidLine.closestPointOnLine(curPoint);
			
			IVector2 curPointToLead = leadPoint.subtractNew(curPoint);
			assertThat(curPointToLead.isParallelTo(dV.getNormalVector()), is(true));
			/*
			 * curPoint and the line run parallel and always have a distance of Math.sqrt(2) / 2 mm
			 * since the lines are 1 mm apart in y direction
			 */
			assertThat(curPointToLead.getLength(), closeTo(Math.sqrt(2) / 2, ACCURACY));
			assertThat(leadPointForInvalidLine, is(sV));
		}
	}
	
	
	@Test
	public void testToLine()
	{
		IVector2 sV = Vector2.fromXY(4, 2);
		IVector2 dV = Vector2.fromXY(1, 3);
		
		ILine line = Line.fromDirection(sV, dV);
		ILine copy = line.toLine();
		
		assertThat(line.supportVector(), is(copy.supportVector()));
		assertThat(line.directionVector(), is(copy.directionVector()));
	}
	
	
	@Test
	public void testIntersectLine()
	{
		IVector2 directionVector = Vector2.X_AXIS;
		ILine line = Line.fromDirection(Vector2.ZERO_VECTOR, directionVector);
		
		for (int degAngle = 0; degAngle <= 180; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			IVector2 curDirectionVector = directionVector.turnNew(radAngle);
			ILine intersectionLine = Line.fromDirection(Vector2.fromXY(0, 1), curDirectionVector);
			
			Optional<IVector2> intersection = line.intersectLine(intersectionLine);
			Optional<IVector2> inverseIntersection = intersectionLine.intersectLine(line);
			assertThat(intersection, is(inverseIntersection));
			
			if (degAngle % 180 == 0)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				double xVal = 1 / Math.tan(-radAngle);
				assertThat(intersection, isPresentAnd(is(Vector2.fromXY(xVal, 0))));
			}
		}
	}
	
	
	@Test
	public void testIntersectLineWithInvalid()
	{
		ILine validLine = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		
		ILine invalidLineA = Line.fromDirection(Vector2.fromXY(10, 1), Vector2.ZERO_VECTOR);
		ILine invalidLineB = Line.fromDirection(Vector2.fromXY(1, 12), Vector2.ZERO_VECTOR);
		
		Optional<IVector2> intersection = invalidLineA.intersectLine(validLine);
		assertThat(intersection, isNotPresent());
		
		intersection = invalidLineA.intersectLine(invalidLineB);
		assertThat(intersection, isNotPresent());
		
		intersection = invalidLineA.intersectLine(invalidLineA);
		assertThat(intersection, isNotPresent());
	}
	
	
	@Test
	public void testIntersectHalfLine()
	{
		IVector2 directionVector = Vector2.X_AXIS;
		IHalfLine halfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, directionVector);
		
		for (int degAngle = 0; degAngle < 180; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			ILine line = Line.fromDirection(Vector2.fromXY(0, 1), directionVector.turnToNew(radAngle));
			
			Optional<IVector2> intersection = line.intersectHalfLine(halfLine);
			Optional<IVector2> inverseIntersection = halfLine.intersectLine(line);
			assertThat(intersection, is(inverseIntersection));
			
			if (degAngle < 90)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				double xVal = Math.tan(radAngle - Math.PI / 2);
				assertThat(intersection, isPresentAnd(is(Vector2.fromXY(xVal, 0))));
			}
		}
		
		ILine zeroLine = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		
		Optional<IVector2> intersection = zeroLine.intersectHalfLine(halfLine);
		assertThat(intersection, isNotPresent());
	}
	
	
	@Test
	public void testIntersectHalfLineWithInvalid()
	{
		IHalfLine validHalfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		IHalfLine invalidHalfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		
		ILine validLine = Line.fromDirection(Vector2.fromXY(10, 1), Vector2.Y_AXIS);
		ILine invalidLine = Line.fromDirection(Vector2.fromXY(1, 12), Vector2.ZERO_VECTOR);
		
		assertThat(validLine.intersectHalfLine(invalidHalfLine), isNotPresent());
		assertThat(invalidLine.intersectHalfLine(validHalfLine), isNotPresent());
		assertThat(invalidHalfLine.intersectHalfLine(invalidHalfLine), isNotPresent());
		
		assertThat(validHalfLine.intersectLine(invalidLine), isNotPresent());
		assertThat(invalidHalfLine.intersectLine(validLine), isNotPresent());
		assertThat(invalidLine.intersectLine(invalidLine), isNotPresent());
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
}