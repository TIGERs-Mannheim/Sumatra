/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
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

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;


/**
 * @author Lukas Magel
 */
public class HalfLineTest extends AbstractLineTest
{
	private final static LineConstructor lineConstructor = dV -> HalfLine.fromDirection(Vector2.ZERO_VECTOR, dV);
	
	
	@Test
	public void testFromDirection()
	{
		IVector2 supportVector = Vector2.fromXY(1, 1);
		IVector2 directionVector = Vector2.fromXY(4, 2);
		
		IHalfLine halfLine = HalfLine.fromDirection(supportVector, directionVector);
		assertThat(halfLine.supportVector(), is(supportVector));
		assertThat(halfLine.supportVector() != supportVector, is(true));
		assertThat(halfLine.directionVector().isParallelTo(directionVector), is(true));
		
		IHalfLine zeroLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		assertThat(zeroLine.supportVector(), is(Vector2.ZERO_VECTOR));
		assertThat(zeroLine.directionVector(), is(Vector2.ZERO_VECTOR));
	}
	
	
	@Test
	public void testIsValid()
	{
		IVector2 sV = Vector2.fromXY(1, 2);
		IVector2 dV = Vector2.fromAngle(1.5d);
		
		IHalfLine validLine = HalfLine.fromDirection(sV, dV);
		assertThat(validLine.isValid(), is(true));
		
		IHalfLine invalidLine = HalfLine.fromDirection(sV, Vector2.ZERO_VECTOR);
		assertThat(invalidLine.isValid(), is(false));
	}
	
	
	@Test
	public void testCopy()
	{
		IHalfLine original = HalfLine.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		IHalfLine copy = original.copy();
		
		assertThat(original.supportVector(), is(copy.supportVector()));
		assertThat(original.supportVector() == copy.supportVector(), is(false));
		
		assertThat(original.directionVector(), is(copy.directionVector()));
		assertThat(original.directionVector() == copy.directionVector(), is(false));
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
			IHalfLine invalidLine = HalfLine.fromDirection(lineSV, Vector2.ZERO_VECTOR);
			
			IVector2 expectedClosestPoint;
			double expectedDistance;
			if (degAngle <= 90 || degAngle >= 270)
			{
				expectedClosestPoint = lineSV;
				expectedDistance = point.distanceTo(lineSV);
			} else
			{
				double radAngleAtOrigin = radAngle - Math.PI / 2;
				double distanceLinePoint = Math.cos(radAngleAtOrigin);
				
				expectedClosestPoint = Vector2.X_AXIS.turnNew(radAngleAtOrigin).scaleTo(distanceLinePoint);
				expectedDistance = Math.abs(distanceLinePoint);
			}
			
			IVector2 actualClosestPoint = halfLine.closestPointOnLine(point);
			double actualDistance = halfLine.distanceTo(point);
			assertThat(actualClosestPoint, is(expectedClosestPoint));
			assertThat(actualDistance, closeTo(expectedDistance, ACCURACY));
			
			assertThat(invalidLine.closestPointOnLine(point), is(lineSV));
		}
	}
	
	
	@Test
	public void testEquals()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 dV = Vector2.fromXY(Math.PI, Math.E);
		
		IHalfLine halfLine = HalfLine.fromDirection(sV, dV);
		
		assertThat(halfLine, is(halfLine));
		assertThat(halfLine.hashCode(), is(halfLine.hashCode()));
		
		IHalfLine other = HalfLine.fromDirection(sV, dV.multiplyNew(-1.0d));
		assertThat(halfLine, not(other));
	}
	
	
	@Test
	public void testEqualsInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 20);
		IVector2 dV = Vector2.fromAngle(1.5d);
		
		IHalfLine properLine = HalfLine.fromDirection(sV, dV);
		IHalfLine invalidLineA = HalfLine.fromDirection(sV, Vector2.ZERO_VECTOR);
		IHalfLine invalidLineACopy = HalfLine.fromDirection(sV, Vector2.ZERO_VECTOR);
		IHalfLine invalidLineB = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		
		assertThat(properLine, not(invalidLineA));
		assertThat(invalidLineA, not(invalidLineB));
		assertThat(invalidLineA, is(invalidLineACopy));
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
		IHalfLine halfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		
		for (int degAngle = 0; degAngle <= 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			
			IVector2 point = Vector2.X_AXIS.turnToNew(radAngle);
			assertThat(halfLine.isPointInFront(point), is(degAngle <= 90 || degAngle > 270));
			assertThat(invalidLine.isPointInFront(point), is(false));
		}
	}
	
	
	@Test
	public void testToLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.Y_AXIS);
		ILine line = halfLine.toLine();
		
		assertThat(halfLine.supportVector(), is(line.supportVector()));
		
		assertThat(halfLine.directionVector().isParallelTo(line.directionVector()), is(true));
		assertThat(halfLine.directionVector().getLength(), closeTo(1.0d, ACCURACY));
	}
	
	
	@Test
	public void testToLineForInvalidLine()
	{
		IVector2 sV = Vector2.fromXY(10, 15);
		IVector2 dV = Vector2.ZERO_VECTOR;
		
		IHalfLine invalidHalfLine = HalfLine.fromDirection(sV, dV);
		ILine line = invalidHalfLine.toLine();
		
		assertThat(line.supportVector(), is(sV));
		assertThat(line.directionVector(), is(Vector2.ZERO_VECTOR));
	}
	
	
	@Test
	public void testIntersectHalfLine()
	{
		IHalfLine lineA = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		
		IVector2 sV = Vector2.fromXY(0, 1);
		IVector2 dV = Vector2.X_AXIS;
		for (int degAngle = 0; degAngle < 360; degAngle++)
		{
			double radAngle = Math.toRadians(degAngle);
			
			IHalfLine lineB = HalfLine.fromDirection(sV, dV.turnToNew(radAngle));
			
			Optional<IVector2> intersection = lineA.intersectHalfLine(lineB);
			Optional<IVector2> inverseIntersection = lineB.intersectHalfLine(lineA);
			assertThat(intersection, is(inverseIntersection));
			
			if (degAngle < 270)
			{
				assertThat(intersection, isNotPresent());
			} else
			{
				IVector2 expected = Vector2.fromXY(Math.tan(radAngle - Math.PI * 3 / 2), 0);
				assertThat(intersection, isPresentAnd(is(expected)));
			}
		}
	}
	
	
	@Test
	public void testIntersectHalfLineWithInvalid()
	{
		IHalfLine validLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		
		IHalfLine invalidLineA = HalfLine.fromDirection(Vector2.fromXY(10, 25), Vector2.ZERO_VECTOR);
		IHalfLine invalidLineB = HalfLine.fromDirection(Vector2.fromXY(20, 11), Vector2.ZERO_VECTOR);
		Optional<IVector2> intersection = validLine.intersectHalfLine(invalidLineA);
		assertThat(intersection, isNotPresent());
		
		intersection = invalidLineA.intersectHalfLine(invalidLineB);
		assertThat(intersection, isNotPresent());
		
		intersection = invalidLineA.intersectHalfLine(invalidLineA);
		assertThat(intersection, isNotPresent());
	}
	
	
	@Test
	public void testIsPointOnLine()
	{
		IHalfLine halfLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.fromXY(3, 0));
		
		IVector2 point = Vector2.fromXY(-10, 0);
		assertThat(halfLine.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(-ALine.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN * 4);
		assertThat(halfLine.isPointOnLine(point), is(false));
		
		point = Vector2.fromXY(1, 0);
		assertThat(halfLine.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(1, ALine.LINE_MARGIN / 4);
		assertThat(halfLine.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(3 + ALine.LINE_MARGIN * 4, 0);
		assertThat(halfLine.isPointOnLine(point), is(true));
		
		point = Vector2.fromXY(Double.MAX_VALUE / 2.0d, 0);
		assertThat(halfLine.isPointOnLine(point), is(true));
		
		
	}
	
	
	@Test
	public void testIsPointOnInvalidLine()
	{
		IHalfLine invalidLine = HalfLine.fromDirection(Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR);
		
		assertThat(invalidLine.isPointOnLine(Vector2.ZERO_VECTOR), is(true));
		assertThat(invalidLine.isPointOnLine(Vector2.fromXY(1, 2)), is(false));
		assertThat(invalidLine.isPointOnLine(Vector2.fromXY(2, 3)), is(false));
	}
	
	
	@Test
	public void testGetSlope()
	{
		doTestGetSlope(lineConstructor);
	}
	
	
	@Test
	public void testDistanceTo()
	{
		IVector2 sV = Vector2.ZERO_VECTOR;
		IVector2 dV = Vector2.Y_AXIS;
		IHalfLine line = HalfLine.fromDirection(sV, dV);
		IHalfLine invalidLine = HalfLine.fromDirection(sV, Vector2.ZERO_VECTOR);
		
		for (int i = -10; i <= 10; i += 1)
		{
			assertThat(line.distanceTo(Vector2.fromXY(i, 0)), closeTo(Math.abs(i), ACCURACY));
			
			IVector2 identical = Vector2.fromXY(0, i);
			IVector2 parallel = Vector2.fromXY(1, i);
			if (i < 0)
			{
				assertThat(line.distanceTo(identical), closeTo(Math.abs(i), ACCURACY));
				assertThat(line.distanceTo(parallel), closeTo(Math.sqrt(1 + i * i), ACCURACY));
			} else
			{
				assertThat(line.distanceTo(identical), closeTo(0, ACCURACY));
				assertThat(line.distanceTo(parallel), closeTo(1, ACCURACY));
			}
			
			assertThat(invalidLine.distanceTo(parallel), closeTo(Math.sqrt(1 + i * i), ACCURACY));
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
