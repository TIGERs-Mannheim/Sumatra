/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2010
 * Author(s): daniel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai;

import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.angleBetweenXAxisAndLine;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.calculateBisector;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.distancePL;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.distancePP;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.faculty;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.getNextPointOnCircle;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.intersectionPoint;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.isLineInterceptingCircle;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.leadPointOnLine;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.normalizeAngle;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.sign;
import static edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath.yInterceptOfLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Test for AIMath functions
 * you may extend it as appropriate
 * @author DanielW
 * 
 */
public class AIMathTest
{
	private static final float	ACCURACY	= 0.001f;
	
	
	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#angleBetweenXAxisAndLine}
	 * 
	 */
	@Test
	public void testAngleBetweenXAxisAndLine()
	{
		assertEquals(Math.PI / 4, angleBetweenXAxisAndLine(new Vector2(0f, 0f), new Vector2(1f, 1f)), 0.01); // falsch!?
		assertEquals(-Math.PI / 4, angleBetweenXAxisAndLine(new Vector2(0f, 0f), new Vector2(1f, -1f)), 0.01); // falsch!?
		assertEquals(0, angleBetweenXAxisAndLine(new Vector2(0f, 0f), new Vector2(1f, 0f)), 0.01);
		assertEquals(Math.PI, angleBetweenXAxisAndLine(new Vector2(0f, 0f), new Vector2(-1f, 0f)), 0.01);
		
		boolean tmp = false;
		try
		{
			angleBetweenXAxisAndLine(new Line(new Vector2(5, 5), new Vector2(0, 0)));
		} catch (IllegalArgumentException e)
		{
			tmp = true;
		}
		assertEquals(true, tmp);
	}
	

	/**
	 * Test method for {@link AIMath#getNextPointOnCircle}
	 * 
	 */
	@Test
	public void testGetNextPointOnCircle()
	{
		Vector2 v = getNextPointOnCircle(new Vector2(0f, 1f), new Vector2(0f, 0f), AIMath.PI_HALF);
		assertEquals(-1, v.x, ACCURACY);
		assertEquals(0, v.y, ACCURACY);
		
		Vector2 v2 = getNextPointOnCircle(new Vector2(0f, 1f), new Vector2(0f, 0f), -AIMath.PI_HALF);
		assertEquals(1, v2.x, ACCURACY);
		assertEquals(0, v2.y, ACCURACY);
		
		Vector2 v3 = getNextPointOnCircle(new Vector2(0f, 1f), new Vector2(1f, 1f), -AIMath.PI_HALF);
		assertEquals(1, v3.x, ACCURACY);
		assertEquals(2, v3.y, ACCURACY);
	}
	

	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#distancePP}
	 * 
	 */
	@Test
	public void testDistancePP()
	{
		Vector2 point1 = new Vector2(1, 2);
		Vector2 point2 = new Vector2(5, 4);
		
		float result2 = distancePP(point1, point2);
		assertEquals(4.47213, result2, ACCURACY);
		
		// other tests by malte
		assertEquals((0), distancePP(new Vector2(0, 0), new Vector2(0, 0)), ACCURACY);
		assertEquals((3), distancePP(new Vector2(-12, 2), new Vector2(-12, 5)), ACCURACY);
		assertEquals((Math.sqrt(2)), distancePP(new Vector2(0, 0), new Vector2(1, 1)), ACCURACY);
	}
	

	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#faculty(int)}
	 * @author Malte
	 */
	@Test
	public void testFaculty()
	{
		assertEquals((120), faculty(5));
		assertEquals((-1), faculty(-5));
		// This may change if you raise max_faculty
		assertEquals((-1), faculty(50));
	}
	

	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#leadPointOnLine}
	 * @author Malte
	 */
	@Test
	public void testLeadPointOnLine()
	{
		// normal case
		Vector2 pointA = new Vector2(4, 1);
		Vector2 line1A = new Vector2(1, 2);
		Vector2 line2A = new Vector2(5, 4);
		
		Vector2 resultA = leadPointOnLine(pointA, line1A, line2A);
		assertEquals(3, resultA.x, ACCURACY);
		assertEquals(3, resultA.y, ACCURACY);
		
		// special case 1. line is orthogonal to x-axis
		Vector2 pointB = new Vector2(-1, -1);
		Vector2 line1B = new Vector2(-2, 0);
		Vector2 line2B = new Vector2(3, 0);
		Vector2 result1B = new Vector2(-1, 0);
		
		Vector2 result2B = leadPointOnLine(pointB, line1B, line2B);
		assertEquals(result1B.x, result2B.x, ACCURACY);
		assertEquals(result1B.y, result2B.y, ACCURACY);
		
		// special case 2. line is orthogonal to y-axis
		Vector2 pointC = new Vector2(-3, 3);
		Vector2 line1C = new Vector2(2, -2);
		Vector2 line2C = new Vector2(2, 4);
		
		Vector2 resultC = leadPointOnLine(pointC, line1C, line2C);
		assertEquals(2, resultC.x, ACCURACY);
		assertEquals(3, resultC.y, ACCURACY);
	}
	

	/**
	 * Test method for {@link AIMath#calculateBisector}
	 * 
	 * @author Malte
	 */
	@Test
	public void testCalculateBisector()
	{
		Vector2 result = calculateBisector(new Vector2(0, 0), new Vector2(0, 2), new Vector2(2, 0));
		assertEquals(result.x, 1, ACCURACY);
		assertEquals(result.y, 1, ACCURACY);
		
		result = calculateBisector(new Vector2(-1, -1), new Vector2(-1, 0), new Vector2(0, -1));
		assertEquals(result.x, -0.5, ACCURACY);
		assertEquals(result.y, -0.5, ACCURACY);
	}
	

	/**
	 * Test method for {@link AIMath#intersectionPoint}
	 * 
	 * @author Malte
	 */
	@Test
	public void testIntersectionPoint()
	{
		Vector2 p1 = new Vector2(0, 0);
		Vector2 v1 = new Vector2(1, 1);
		Vector2 p2 = new Vector2(0, 1);
		Vector2 v2 = new Vector2(1, -1);
		Vector2 result;
		try
		{
			result = intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err2)
		{
			result = new Vector2(AIConfig.INIT_VECTOR);
		}
		assertEquals(result.x, 0.5, ACCURACY);
		assertEquals(result.y, 0.5, ACCURACY);
		
		p1 = new Vector2(4, 4);
		v1 = new Vector2(453, 13);
		p2 = new Vector2(4, 4);
		v2 = new Vector2(-45, 18);
		try
		{
			result = intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err1)
		{
			result = new Vector2(AIConfig.INIT_VECTOR);
		}
		assertEquals(result.x, 4, ACCURACY);
		assertEquals(result.y, 4, ACCURACY);
		
		// parallel lines
		p1 = new Vector2(0, 0);
		v1 = new Vector2(0, 1);
		p2 = new Vector2(4, 4);
		v2 = new Vector2(0, 1);
		try
		{
			result = intersectionPoint(p1, v1, p2, v2);
			fail();
		} catch (MathException err)
		{
			
		}
		
		// equal lines
		p1 = new Vector2(-1, 0);
		v1 = new Vector2(1, 0);
		p2 = new Vector2(5, 0);
		v2 = new Vector2(1, 0);
		try
		{
			result = intersectionPoint(p1, v1, p2, v2);
			fail();
		} catch (MathException err)
		{
			
		}
		
		
		// y-axis
		p1 = new Vector2(-1, 0);
		v1 = new Vector2(0, -1);
		p2 = new Vector2(3, 0);
		v2 = new Vector2(-1, 1);
		try
		{
			result = intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err)
		{
			result = new Vector2(AIConfig.INIT_VECTOR);
		}
		assertEquals(-1, result.x, ACCURACY);
		assertEquals(4, result.y, ACCURACY);
		
		
		// Tests from Malte:
		Line l1 = new Line(new Vector2(0,-1), new Vector2(1,1));
		Line l2 = new Line(new Vector2(2,0), new Vector2(2,-3));
		try
		{
			System.out.println(AIMath.intersectionPoint(l1, l2));
		} catch (MathException err)
		{
			System.out.println("Fail!");
		}
		
		l1 = new Line(new Vector2(0,0), AVector2.Y_AXIS);
		l2 = new Line(new Vector2(2,0), AVector2.Y_AXIS);
		try
		{
			System.out.println(AIMath.intersectionPoint(l1, l2));
			fail();
		} catch (MathException err)
		{
			System.out.println("nice");
		}


	}
	

	/**
	 * Test method for {@link AIMath#normalizeAngle}
	 * @author Malte
	 */
	@Test
	public void testNormalizeAngle()
	{
		assertEquals(normalizeAngle(4.6f * AIMath.PI), 0.6f * AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(-4.6f * AIMath.PI), -0.6f * AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(3.6f * AIMath.PI), -0.4f * AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(-3.6f * AIMath.PI), 0.4f * AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(5.001f * AIMath.PI), -0.999f * AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(5f * AIMath.PI), AIMath.PI, ACCURACY);
		assertEquals(normalizeAngle(4f * AIMath.PI), 0, ACCURACY);
	}
	

	/**
	 * Test method for {@link AIMath#getSign}
	 * @author Malte
	 */
	@Test
	public void testGetSign()
	{
		assertEquals(sign(5), 1, ACCURACY);
		assertEquals(sign(-5), -1, ACCURACY);
		assertEquals(sign(0), 1, ACCURACY);
	}
	

	/**
	 * Test method for {@link AIMath#isLineInterceptingCircle}
	 * @author Malte
	 */
	@Test
	public void testIsLineInterceptingCircle()
	{
		Vector2 center;
		float radius;
		float slope;
		float yIntercept;
		boolean expected;
		// ------------------------------
		center = new Vector2(0, 0);
		radius = 1;
		slope = -1;
		yIntercept = 1;
		expected = true;
		assertEquals(isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
		// ------------------------------------
		center = new Vector2(0, 0);
		radius = 0;
		slope = -5;
		yIntercept = 1;
		expected = false;
		assertEquals(isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
		// ------------------------------------
		center = new Vector2(0, 0);
		radius = 0;
		slope = -5;
		yIntercept = 0;
		expected = true;
		assertEquals(isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
	}
	

	/**
	 * Test method for {@link AIMath#yInterceptOfLine}
	 * @author Malte
	 */
	@Test
	public void testYInterceptOfLine()
	{
		Vector2 point = new Vector2(0, 0);
		float slope = -2;
		assertEquals(yInterceptOfLine(point, slope), 0, ACCURACY);
		point.y = 2;
		assertEquals(yInterceptOfLine(point, slope), 2, ACCURACY);
		point.x = 2;
		assertEquals(yInterceptOfLine(point, slope), 6, ACCURACY);
	}
	
	

	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#distancePL}
	 * @author Malte
	 */
	@Test
	public void testDistancePL()
	{
		Vector2 point;
		Vector2 line1;
		Vector2 line2;
		point = new Vector2(0, 0);
		line1 = new Vector2(-2, 0);
		line2 = new Vector2(3, 0);
		assertEquals(distancePL(point, line1, line2), 0, ACCURACY);
		
		point.y = 3;
		assertEquals(distancePL(point, line1, line2), 3, ACCURACY);
		
		point.y = -3;
		assertEquals(distancePL(point, line1, line2), 3, ACCURACY);
		
		line1 = new Vector2(0, 1);
		line2 = new Vector2(1, 0);
		point = new Vector2(0, 0);
		assertEquals(distancePL(point, line1, line2), 1 / AIMath.sqrt(2), ACCURACY);
	}
	

	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath#isPointInCircle}
	 * @author Steffen
	 * 
	 */
	@Test
	public void testIsPointInCircle()
	{
		Vector2 center = new Vector2(6, 4); // Stützvektor
		Circle circle = new Circle(center, 4.9f); // Kreis
		
		Vector2 point = new Vector2(2, 7); // TestPunkt
		
		assertEquals(circle.isPointInShape(point), false);
	}
	

	/**
	 * Tests trigonometric functions of AIMath
	 * 
	 */
	@Test
	public void testTrigonometry()
	{
		// sin
		assertEquals(AIMath.sin(AIMath.PI), 0, ACCURACY);
		assertEquals(AIMath.sin(4.5f), -0.9775301, ACCURACY);
		assertEquals(AIMath.sin(-34), -0.529, ACCURACY);
		
		// cos
		assertEquals(AIMath.cos(5), 0.28366, ACCURACY);
		assertEquals(AIMath.cos(-0.1f), 0.9950, ACCURACY);
		
		// tan
		assertEquals(AIMath.tan(3), -0.1425, ACCURACY);
		assertEquals(AIMath.tan(-2), 2.185, ACCURACY);
		
	}
	

	/**
	 * test {@link AIMath#hasDigitsAfterDecimalPoint(float)}
	 * 
	 */
	@Test
	public void testHasDigitsAfterDecimal()
	{
		assertFalse(AIMath.hasDigitsAfterDecimalPoint(1.0f));
		assertTrue(AIMath.hasDigitsAfterDecimalPoint(1.22f));
		assertTrue(AIMath.hasDigitsAfterDecimalPoint(-1.02f));
		assertFalse(AIMath.hasDigitsAfterDecimalPoint(-1.0f));
	}
	

}