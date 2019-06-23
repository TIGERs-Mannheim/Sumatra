/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.circle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.I2DShapeTest;


/**
 * Test method for {@link Circle}.
 * 
 * @author Malte
 */
public class CircleTest implements I2DShapeTest
{
	
	/**
	 * Testmethod for Circle#isLineIntersectingShape.
	 * 
	 * @author Dion
	 */
	@Override
	@Test
	public void testIsLineIntersectingShape()
	{
		// Test true
		Circle circle = new Circle(new Vector2(1, 1), 1);
		Line line = new Line(new Vector2(0, 0), new Vector2(1, 1));
		assertEquals(true, circle.isLineIntersectingShape(line));
		
		// Test true2
		Line line3 = new Line(new Vector2(-1, 1), new Vector2(1, 0));
		assertEquals(true, circle.isLineIntersectingShape(line3));
		
		// Test false
		Line line2 = new Line(new Vector2(), new Vector2(-1, 1));
		assertEquals(false, circle.isLineIntersectingShape(line2));
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
		Circle circle = new Circle(new Vector2(1, 1), 1);
		Line line = new Line(new Vector2(), new Vector2(-1, 1));
		if (circle.lineIntersections(line).size() == 0)
		{
			assertTrue(true);
		} else
		{
			assertTrue(false);
		}
		
		// Test 2
		Line line2 = new Line(new Vector2(-1, 1), new Vector2(1, 0));
		List<IVector2> result = circle.lineIntersections(line2);
		if ((result.get(0).x() == 2) && (result.get(0).y() == 1) && (result.get(1).x() == 0) && (result.get(1).y() == 1))
		{
			assertTrue(true);
		} else
		{
			assertTrue(false);
		}
	}
	
	
	/**
	 * Testmethod for Circle#nearestPointOutside.
	 * 
	 * @author Dion
	 */
	@Override
	@Test
	public void testNearestPointOutside()
	{
		// Test true
		Circle circle = new Circle(new Vector2(-2, 4), 3);
		Vector2 point = new Vector2(-1, 4);
		assertEquals(circle.nearestPointOutside(point), new Vector2(1, 4));
		
		// Test false
		Vector2 point3 = new Vector2(1, 2);
		assertEquals(circle.nearestPointOutside(point3), new Vector2(1, 2));
	}
	
	
	@Override
	@Test
	@Ignore
	public void testConstructor()
	{
		fail("Not implemented");
	}
	
	
	@Override
	@Test
	@Ignore
	public void testGetArea()
	{
		fail("Not implemented");
	}
	
	
	@Override
	@Test
	@Ignore
	public void testIsPointInShape()
	{
		fail("Not implemented");
	}
	
}
