/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.11.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.AIRectangle;


/**
 * This is a test class for the class {@link AIRectangle}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class RectangleTest implements I2DShapeTest
{

	@SuppressWarnings("unused")
	@Override
	@Test
	public void testConstructor()
	{
		Vector2 refPoint = new Vector2(4, 4);
		
		try
		{
			
			AIRectangle rect = new AIRectangle(1, refPoint, 0, 0);
			fail("Exception was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			AIRectangle rect2 = new AIRectangle(1, refPoint, -1, -1);
			fail("Exception was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			AIRectangle rect2 = new AIRectangle(1, refPoint, 2, -1);
			fail("Exception was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			AIRectangle rect2 = new AIRectangle(1, refPoint, -2, 7);
			fail("Exception was not catched!");
			
		} catch (IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
	}
	

	@Test
	public void combineTest()
	{
		// Vector2 refPointA = new Vector2(20, 35);
		// Rectangle rectA = new Rectangle(1, refPointA, 100, 200);
		//
		// Vector2 refPointB = new Vector2();
		//
		// Rectangle rectB = new Rectangle(2, refPointB, 10, 20);
		//
		
	}
	
	@Override
	@Test
	public void testIsPointInShape()
	{
		Vector2 ref = new Vector2(0, 0);
		float h = 1;
		float l = 1;
		Rectangle r = new Rectangle(ref, h, l);
		assertFalse(r.isPointInShape(new Vector2(0.5f, 0.5f)));
		
		// test corners
		assertTrue(r.isPointInShape(new Vector2(0, 0)));
		assertTrue(r.isPointInShape(new Vector2(1, 0)));
		assertTrue(r.isPointInShape(new Vector2(0, -1)));
		assertTrue(r.isPointInShape(new Vector2(1, -1)));
	}
	

	@SuppressWarnings("deprecation")
	@Override
	@Test
	public void testIsLineIntersectingShape()
	{
		Vector2 refPoint = new Vector2(0, 0);
		Rectangle rect = new Rectangle(refPoint, 3, 4);
		
		Line l = new Line(new Vector2(0,0), new Vector2(0,1));
		Vector2 rechts = new Vector2(1,0.1f);
		Vector2 links = new Vector2(-1,0.1f);
		
		float f = (links.subtractNew(l.supportVector()).scalarProduct(l.getOrthogonalLine().directionVector()));
		System.out.println("links: "+f);
		
		f = (rechts.subtractNew(l.supportVector()).scalarProduct(l.getOrthogonalLine().directionVector()));
		System.out.println("rechts: "+f);
		
		
		Line line = new Line(1, -4);
		assertTrue(rect.isLineIntersectingShape(line));
		
		Line line2 = new Line(1, -3);
		
		assertTrue(rect.isLineIntersectingShape(line2));
		
		Line line3 = new Line(1, -2);
		
		assertTrue(rect.isLineIntersectingShape(line3));
	}
	

	@Test
	public void getPointinShape()
	{
		Rectangle testRect = new AIRectangle(55, new Vector2(-10, 0), 100, 100);
		
		for (int i = 0; i <= testRect.xExtend() * testRect.yExtend(); i++)
		{
			Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
			

			assertFalse(AIMath.hasDigitsAfterDecimalPoint(point.x()));
			assertFalse(AIMath.hasDigitsAfterDecimalPoint(point.y()));
		}
		
		testRect = new Rectangle(new Vector2(-25, 31), 430, 230);
		
		for (int i = 0; i <= testRect.xExtend() * testRect.yExtend(); i++)
		{
			Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
			
			assertFalse(AIMath.hasDigitsAfterDecimalPoint(point.x()));
			assertFalse(AIMath.hasDigitsAfterDecimalPoint(point.y()));
		}
		

		testRect = new Rectangle(new Vector2(-25, 31), 430.43f, 230.21f);
		
		for (int i = 0; i <= testRect.xExtend() * testRect.yExtend(); i++)
		{
			Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
		}
		
	}



	@Override
	@Test
	public void testGetArea()
	{
		fail();
		
	}



	@Override
	@Test
	public void testNearestPointOutside()
	{
		fail();
		
	}
}
