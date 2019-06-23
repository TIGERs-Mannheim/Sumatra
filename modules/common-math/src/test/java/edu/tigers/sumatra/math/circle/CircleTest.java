/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Test method for {@link Circle}.
 * 
 * @author Malte
 */
public class CircleTest
{
	
	
	private static final double ACCURACY = 1e-3;
	
	
	/**
	 * Testmethod for Circle#isIntersectingWithLine.
	 * 
	 * @author Dion
	 */
	@Test
	public void testIsLineIntersectingShape()
	{
		// Test true
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		Line line = Line.fromDirection(Vector2.fromXY(0, 0), Vector2.fromXY(1, 1));
		Assert.assertEquals(true, circle.isIntersectingWithLine(line));
		
		// Test true2
		Line line3 = Line.fromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		Assert.assertEquals(true, circle.isIntersectingWithLine(line3));
		
		// Test false
		Line line2 = Line.fromDirection(Vector2.zero(), Vector2.fromXY(-1, 1));
		Assert.assertEquals(false, circle.isIntersectingWithLine(line2));
	}
	
	
	/**
	 * Testmethod for Circle#LineIntersections.
	 * 
	 * @author Dion
	 */
	@Test
	public void LineIntersections()
	{
		// Test 1
		ICircle circle = Circle.createCircle(Vector2.fromXY(1, 1), 1);
		Line line = Line.fromDirection(Vector2.zero(), Vector2.fromXY(-1, 1));
		if (circle.lineIntersections(line).size() == 0)
		{
			Assert.assertTrue(true);
		} else
		{
			Assert.assertTrue(false);
		}
		
		// Test 2
		Line line2 = Line.fromDirection(Vector2.fromXY(-1, 1), Vector2.fromXY(1, 0));
		List<IVector2> result = circle.lineIntersections(line2);
		if ((result.get(0).x() == 2) && (result.get(0).y() == 1) && (result.get(1).x() == 0) && (result.get(1).y() == 1))
		{
			Assert.assertTrue(true);
		} else
		{
			Assert.assertTrue(false);
		}
	}
	
	
	/**
	 * Testmethod for Circle#nearestPointOutsideCircle.
	 * 
	 * @author Dion
	 */
	@Test
	public void testNearestPointOutside()
	{
		// Test true
		ICircle circle = Circle.createCircle(Vector2.fromXY(-2, 4), 3);
		Vector2 point = Vector2.fromXY(-1, 4);
		Assert.assertEquals(circle.nearestPointOutside(point), Vector2.fromXY(1, 4));
		
		// Test false
		Vector2 point3 = Vector2.fromXY(1, 2);
		Assert.assertEquals(circle.nearestPointOutside(point3), Vector2.fromXY(1, 2));
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@Test
	public void testTangentialIntersections()
	{
		ICircle circle = Circle.createCircle(Vector2.fromXY(5, 7), 6);
		IVector2 externalPoint = Vector2.fromXY(1, 1);
		List<IVector2> res = circle.tangentialIntersections(externalPoint);
		for (IVector2 p : res)
		{
			if (p.isCloseTo(Vector2.fromXY(5, 1), ACCURACY))
			{
				return;
			}
		}
		Assert.fail();
	}
	
	
	/**
	 * @author AndreR
	 */
	@Test
	public void testCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 1);
		
		ICircle circle;
		
		try
		{
			circle = Circle.from3Points(P1, P2, P3);
		} catch (MathException err)
		{
			Assert.fail();
			return;
		}
		
		Assert.assertEquals(1.0, circle.radius(), Double.MIN_VALUE);
		Assert.assertEquals(1.0, circle.center().x(), Double.MIN_VALUE);
		Assert.assertEquals(1.0, circle.center().y(), Double.MIN_VALUE);
	}
	
	
	/**
	 * @author AndreR
	 */
	@Test
	public void testInvalidCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 0);
		IVector2 P2 = Vector2.fromXY(1, 0);
		IVector2 P3 = Vector2.fromXY(2, 0);
		
		try
		{
			Circle.from3Points(P1, P2, P3);
		} catch (MathException err)
		{
			return;
		}
		
		Assert.fail();
	}
	
	
	/**
	 * @author AndreR
	 */
	@Test
	public void testSmallCircleFrom3Points()
	{
		IVector2 P1 = Vector2.fromXY(0, 1e-9f);
		IVector2 P2 = Vector2.fromXY(1e-9f, 0);
		IVector2 P3 = Vector2.fromXY(2e-9f, 1e-9f);
		
		ICircle circle;
		
		try
		{
			circle = Circle.from3Points(P1, P2, P3);
		} catch (MathException err)
		{
			Assert.fail();
			return;
		}
		
		Assert.assertEquals(1e-9f, circle.radius(), Double.MIN_VALUE);
		Assert.assertEquals(1e-9f, circle.center().x(), Double.MIN_VALUE);
		Assert.assertEquals(1e-9f, circle.center().y(), Double.MIN_VALUE);
	}
}
