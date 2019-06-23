/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.11.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.shapes.rectangle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.shapes.I2DShapeTest;


/**
 * This is a test class for the class {@link AIRectangle}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class RectangleTest implements I2DShapeTest
{
	/**
	 */
	@Override
	@Test
	@Ignore
	public void testConstructor()
	{
		// Vector2 refPoint = new Vector2(4, 4);
		
		fail("Incomplete");
		try
		{
			
			// AIRectangle rect = new AIRectangle(1, refPoint, 0, 0);
			fail("Exception was not catched!");
			
		} catch (final IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			// AIRectangle rect2 = new AIRectangle(1, refPoint, -1, -1);
			fail("Exception was not catched!");
			
		} catch (final IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			// AIRectangle rect2 = new AIRectangle(1, refPoint, 2, -1);
			fail("Exception was not catched!");
			
		} catch (final IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
		try
		{
			// AIRectangle rect2 = new AIRectangle(1, refPoint, -2, 7);
			fail("Exception was not catched!");
			
		} catch (final IllegalArgumentException err)
		{
			err.printStackTrace();
		}
		
	}
	
	
	/**
	 */
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
		final Vector2 ref = new Vector2(0, 0);
		final float h = 1;
		final float l = 1;
		final Rectangle r = new Rectangle(ref, h, l);
		assertFalse(r.isPointInShape(new Vector2(0.5f, 0.5f)));
		
		// test corners
		assertTrue(r.isPointInShape(new Vector2(0, 0)));
		assertTrue(r.isPointInShape(new Vector2(1, 0)));
		assertTrue(r.isPointInShape(new Vector2(0, -1)));
		assertTrue(r.isPointInShape(new Vector2(1, -1)));
	}
	
	
	@Override
	@Test
	public void testIsLineIntersectingShape()
	{
		// Vector2 refPoint = new Vector2(0, 0);
		// Rectangle rect = new Rectangle(refPoint, 3, 4);
		//
		// Line l = new Line(new Vector2(0,0), new Vector2(0,1));
		// Vector2 rechts = new Vector2(1,0.1f);
		// Vector2 links = new Vector2(-1,0.1f);
		//
		// float f = (links.subtractNew(l.supportVector()).scalarProduct(l.getOrthogonalLine().directionVector()));
		// System.out.println("links: "+f);
		//
		// f = (rechts.subtractNew(l.supportVector()).scalarProduct(l.getOrthogonalLine().directionVector()));
		// System.out.println("rechts: "+f);
		//
		//
		// Line line = new Line(1, -4);
		// assertTrue(rect.isLineIntersectingShape(line));
		//
		// Line line2 = new Line(1, -3);
		//
		// assertTrue(rect.isLineIntersectingShape(line2));
		//
		// Line line3 = new Line(1, -2);
		//
		// assertTrue(rect.isLineIntersectingShape(line3));
		
		final Rectangle rect1 = new Rectangle(new Vector2(0, 1), 1, 1);
		final Rectangle rect2 = new Rectangle(new Vector2(1, 2), 1, 1);
		final Rectangle rect3 = new Rectangle(new Vector2(2, 3), 1, 1);
		final Rectangle rect4 = new Rectangle(new Vector2(-1.5f, -0.5f), 1, 1);
		
		final Vector2 startPoint = new Vector2(0, 0);
		final Vector2 dir1 = new Vector2(1, 1);
		final Vector2 dir2 = new Vector2(-1, -1);
		
		assertTrue(rect1.isLineSegmentIntersectingRectangle(startPoint, dir1));
		assertTrue(rect2.isLineSegmentIntersectingRectangle(startPoint, dir1));
		assertFalse(rect3.isLineSegmentIntersectingRectangle(startPoint, dir1));
		assertFalse(rect4.isLineSegmentIntersectingRectangle(startPoint, dir1));
		
		assertTrue(rect1.isLineSegmentIntersectingRectangle(startPoint, dir2));
		assertFalse(rect2.isLineSegmentIntersectingRectangle(startPoint, dir2));
		assertFalse(rect3.isLineSegmentIntersectingRectangle(startPoint, dir2));
		assertTrue(rect4.isLineSegmentIntersectingRectangle(startPoint, dir2));
	}
	
	
	/**
	 */
	@Test
	public void testLineIntersection()
	{
		final Rectangle rect = new Rectangle(new Vector2f(1, 2), 2, 1);
		
		// Test horizontal lines
		Line line = new Line(new Vector2f(0, 0), new Vector2f(1, 0));
		assertEquals(0, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(0, 1), new Vector2f(1, 0));
		assertEquals(3, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(0, (float) 1.5), new Vector2f(1, 0));
		assertEquals(2, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(0, 2), new Vector2f(1, 0));
		assertEquals(3, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(0, 3), new Vector2f(1, 0));
		assertEquals(0, rect.lineIntersection(line).size());
		
		// Test vertical lines
		line = new Line(new Vector2f(1, 0), new Vector2f(0, 1));
		assertEquals(3, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(2, 0), new Vector2f(0, 1));
		assertEquals(2, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(3, 0), new Vector2f(0, 1));
		assertEquals(3, rect.lineIntersection(line).size());
		line = new Line(new Vector2f(4, 0), new Vector2f(0, 1));
		assertEquals(0, rect.lineIntersection(line).size());
		
		// Test diagonal lines
		line = new Line(new Vector2f(1, 0), new Vector2f(1, 1));
		assertEquals(2, rect.lineIntersection(line).size());
		assertEquals(new Vector2f(3, 2), rect.lineIntersection(line).get(0));
		assertEquals(new Vector2f(2, 1), rect.lineIntersection(line).get(1));
		line = new Line(new Vector2f(0, (float) 0.5), new Vector2f(1, 1));
		assertEquals(2, rect.lineIntersection(line).size());
		assertEquals(new Vector2f((float) 1.5, 2), rect.lineIntersection(line).get(0));
		assertEquals(new Vector2f(1, (float) 1.5), rect.lineIntersection(line).get(1));
		
	}
	
	
	/**
	 */
	@Test
	public void getPointinShape()
	{
		Rectanglef testRect = new AIRectangle(55, new Vector2(-10, 0), 100, 100);
		
		for (int i = 0; i <= (testRect.xExtend() * testRect.yExtend()); i++)
		{
			final Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
			
			
			assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(point.x()));
			assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(point.y()));
		}
		
		testRect = new Rectanglef(new Vector2(-25, 31), 430, 230);
		
		for (int i = 0; i <= (testRect.xExtend() * testRect.yExtend()); i++)
		{
			final Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
			
			assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(point.x()));
			assertFalse(SumatraMath.hasDigitsAfterDecimalPoint(point.y()));
		}
		
		
		testRect = new Rectanglef(new Vector2(-25, 31), 430.43f, 230.21f);
		
		for (int i = 0; i <= (testRect.xExtend() * testRect.yExtend()); i++)
		{
			final Vector2 point = testRect.getRandomPointInShape();
			
			assertEquals(testRect.topLeft().x(), point.x(), testRect.xExtend());
			assertEquals(testRect.topLeft().y(), point.y(), testRect.yExtend());
		}
		
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
	public void testNearestPointOutside()
	{
		fail("Not implemented");
		
	}
}
