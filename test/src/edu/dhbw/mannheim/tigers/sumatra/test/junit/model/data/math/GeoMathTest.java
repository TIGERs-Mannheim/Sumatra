/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;


/**
 * Geometry math problems testing.
 * 
 * @author stei_ol
 * 
 */
public class GeoMathTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	ACCURACY	= 0.001f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	static
	{
		// Load configuration
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, Agent.VALUE_AI_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_BOT_CONFIG, Agent.VALUE_BOT_CONFIG);
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getBotClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Test method for {@link GeoMath#intersectionPoint}
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
			result = GeoMath.intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err2)
		{
			result = new Vector2(GeoMath.INIT_VECTOR);
		}
		assertEquals(result.x, 0.5, ACCURACY);
		assertEquals(result.y, 0.5, ACCURACY);
		
		p1 = new Vector2(4, 4);
		v1 = new Vector2(453, 13);
		p2 = new Vector2(4, 4);
		v2 = new Vector2(-45, 18);
		try
		{
			result = GeoMath.intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err1)
		{
			result = new Vector2(GeoMath.INIT_VECTOR);
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
			result = GeoMath.intersectionPoint(p1, v1, p2, v2);
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
			result = GeoMath.intersectionPoint(p1, v1, p2, v2);
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
			result = GeoMath.intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err)
		{
			result = new Vector2(GeoMath.INIT_VECTOR);
		}
		assertEquals(-1, result.x, ACCURACY);
		assertEquals(4, result.y, ACCURACY);
		
		
		// Tests from Malte:
		Line l1 = new Line(new Vector2(0, -1), new Vector2(1, 1));
		Line l2 = new Line(new Vector2(2, 0), new Vector2(2, -3));
		try
		{
			GeoMath.intersectionPoint(l1, l2);
		} catch (MathException err)
		{
			fail();
		}
		
		l1 = new Line(new Vector2(0, 0), AVector2.Y_AXIS);
		l2 = new Line(new Vector2(2, 0), AVector2.Y_AXIS);
		try
		{
			System.out.println(GeoMath.intersectionPoint(l1, l2));
			fail();
		} catch (MathException err)
		{
			// expected
		}
		
		
	}
	
	
	/**
	 * Test method for {@link GeoMath#isLineInterceptingCircle}
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
		assertEquals(GeoMath.isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
		// ------------------------------------
		center = new Vector2(0, 0);
		radius = 0;
		slope = -5;
		yIntercept = 1;
		expected = false;
		assertEquals(GeoMath.isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
		// ------------------------------------
		center = new Vector2(0, 0);
		radius = 0;
		slope = -5;
		yIntercept = 0;
		expected = true;
		assertEquals(GeoMath.isLineInterceptingCircle(center, radius, slope, yIntercept), expected);
	}
	
	
	/**
	 * Test method for {@link GeoMath#yInterceptOfLine}
	 * @author Malte
	 */
	@Test
	public void testYInterceptOfLine()
	{
		Vector2 point = new Vector2(0, 0);
		float slope = -2;
		assertEquals(GeoMath.yInterceptOfLine(point, slope), 0, ACCURACY);
		point.y = 2;
		assertEquals(GeoMath.yInterceptOfLine(point, slope), 2, ACCURACY);
		point.x = 2;
		assertEquals(GeoMath.yInterceptOfLine(point, slope), 6, ACCURACY);
	}
	
	
	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath#distancePL}
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
		assertEquals(GeoMath.distancePL(point, line1, line2), 0, ACCURACY);
		
		point.y = 3;
		assertEquals(GeoMath.distancePL(point, line1, line2), 3, ACCURACY);
		
		point.y = -3;
		assertEquals(GeoMath.distancePL(point, line1, line2), 3, ACCURACY);
		
		line1 = new Vector2(0, 1);
		line2 = new Vector2(1, 0);
		point = new Vector2(0, 0);
		assertEquals(GeoMath.distancePL(point, line1, line2), 1 / SumatraMath.sqrt(2), ACCURACY);
	}
	
	
	/**
	 * 
	 * Test method for {@link Circle#isPointInShape(IVector2)}
	 * @author Steffen
	 * 
	 */
	@Test
	public void testIsPointInCircle()
	{
		Vector2 center = new Vector2(6, 4); // St�tzvektor
		Circle circle = new Circle(center, 4.9f); // Kreis
		
		Vector2 point = new Vector2(2, 7); // TestPunkt
		
		assertEquals(circle.isPointInShape(point), false);
	}
	
	
	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath#leadPointOnLine}
	 * @author Malte
	 */
	@Test
	public void testLeadPointOnLine()
	{
		// normal case
		Vector2 pointA = new Vector2(4, 1);
		Vector2 line1A = new Vector2(1, 2);
		Vector2 line2A = new Vector2(5, 4);
		
		Vector2 resultA = GeoMath.leadPointOnLine(pointA, line1A, line2A);
		assertEquals(3, resultA.x, ACCURACY);
		assertEquals(3, resultA.y, ACCURACY);
		
		// special case 1. line is orthogonal to x-axis
		Vector2 pointB = new Vector2(-1, -1);
		Vector2 line1B = new Vector2(-2, 0);
		Vector2 line2B = new Vector2(3, 0);
		Vector2 result1B = new Vector2(-1, 0);
		
		Vector2 result2B = GeoMath.leadPointOnLine(pointB, line1B, line2B);
		assertEquals(result1B.x, result2B.x, ACCURACY);
		assertEquals(result1B.y, result2B.y, ACCURACY);
		
		// special case 2. line is orthogonal to y-axis
		Vector2 pointC = new Vector2(-3, 3);
		Vector2 line1C = new Vector2(2, -2);
		Vector2 line2C = new Vector2(2, 4);
		
		Vector2 resultC = GeoMath.leadPointOnLine(pointC, line1C, line2C);
		assertEquals(2, resultC.x, ACCURACY);
		assertEquals(3, resultC.y, ACCURACY);
	}
	
	
	/**
	 * Test method for {@link GeoMath#calculateBisector}
	 * 
	 * @author Malte
	 */
	@Test
	public void testCalculateBisector()
	{
		Vector2 result = GeoMath.calculateBisector(new Vector2(0, 0), new Vector2(0, 2), new Vector2(2, 0));
		assertEquals(result.x, 1, ACCURACY);
		assertEquals(result.y, 1, ACCURACY);
		
		result = GeoMath.calculateBisector(new Vector2(-1, -1), new Vector2(-1, 0), new Vector2(0, -1));
		assertEquals(result.x, -0.5, ACCURACY);
		assertEquals(result.y, -0.5, ACCURACY);
	}
	
	
	/**
	 * Test method for {@link GeoMath#stepAlongCircle(IVector2, IVector2, float)}
	 * 
	 */
	@Test
	public void testGetNextPointOnCircle()
	{
		Vector2 v = GeoMath.stepAlongCircle(new Vector2(0f, 1f), new Vector2(0f, 0f), AngleMath.PI_HALF);
		assertEquals(-1, v.x, ACCURACY);
		assertEquals(0, v.y, ACCURACY);
		
		Vector2 v2 = GeoMath.stepAlongCircle(new Vector2(0f, 1f), new Vector2(0f, 0f), -AngleMath.PI_HALF);
		assertEquals(1, v2.x, ACCURACY);
		assertEquals(0, v2.y, ACCURACY);
		
		Vector2 v3 = GeoMath.stepAlongCircle(new Vector2(0f, 1f), new Vector2(1f, 1f), -AngleMath.PI_HALF);
		assertEquals(1, v3.x, ACCURACY);
		assertEquals(2, v3.y, ACCURACY);
	}
	
	
	/**
	 * 
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath#distancePP}
	 * 
	 */
	@Test
	public void testDistancePP()
	{
		Vector2 point1 = new Vector2(1, 2);
		Vector2 point2 = new Vector2(5, 4);
		
		float result2 = GeoMath.distancePP(point1, point2);
		assertEquals(4.47213, result2, ACCURACY);
		
		// other tests by malte
		assertEquals((0), GeoMath.distancePP(new Vector2(0, 0), new Vector2(0, 0)), ACCURACY);
		assertEquals((3), GeoMath.distancePP(new Vector2(-12, 2), new Vector2(-12, 5)), ACCURACY);
		assertEquals((SumatraMath.sqrt(2)), GeoMath.distancePP(new Vector2(0, 0), new Vector2(1, 1)), ACCURACY);
	}
	
	
	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath#angleBetweenVectorAndVectorWithNegative}
	 */
	@Test
	public void testAngleBetweenVectorAndVectorWithNegative()
	{
		// positive x-axis
		IVector2 v1 = new Vector2(1, 0);
		// positive y-axis
		IVector2 v2 = new Vector2(0, 1);
		float angle = 0;
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v1, v2);
		assertEquals(-Math.PI / 2, angle, ACCURACY);
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v2, v1);
		assertEquals(Math.PI / 2, angle, ACCURACY);
		
		// positive x-axis
		v1 = new Vector2(1, 0);
		// negative x-axis
		v2 = new Vector2(-1, 0);
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v1, v2);
		assertEquals(-Math.PI, angle, ACCURACY);
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v2, v1);
		assertEquals(Math.PI, angle, ACCURACY);
		
		// http://en.wikipedia.org/wiki/Atan2
		v1 = new Vector2(1, 0);
		v2 = new Vector2(1f / 2f, (float) Math.sqrt(3) / 2f);
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v1, v2);
		assertEquals(-Math.PI / 3, angle, ACCURACY);
		angle = GeoMath.angleBetweenVectorAndVectorWithNegative(v2, v1);
		assertEquals(Math.PI / 3, angle, ACCURACY);
	}
	
	
	/**
	 */
	@Test
	public void testApproxOrientationBallDamp()
	{
		IVector2 shootVel = new Vector2(AngleMath.PI - 1).scaleTo(8);
		IVector2 incomingVec = new Vector2(AngleMath.PI_QUART).scaleTo(5);
		IVector2 targetVec = GeoMath.ballDamp(shootVel, incomingVec, EBotType.TIGER);
		System.out.println("shootVel: " + shootVel.getAngle());
		System.out.println("incomingVec: " + incomingVec.getAngle());
		System.out.println("target angle: " + targetVec.getAngle());
		float targetOrientation = GeoMath.approxOrientationBallDamp(8, incomingVec, EBotType.TIGER,
				AngleMath.PI_HALF + 0.1f, targetVec.getAngle());
		Assert.assertEquals(AngleMath.PI - 1, targetOrientation, 0.1f);
	}
	
	
	/**
	 */
	@Test
	public void testQuadrantCheck()
	{
		// Normal TEst
		IVector2 quadrant1 = new Vector2(1200, 1200);
		IVector2 quadrant2 = new Vector2(-1200, 1200);
		IVector2 quadrant3 = new Vector2(-1200, -1200);
		IVector2 quadrant4 = new Vector2(1200, -1200);
		
		assertEquals(1, GeoMath.checkQuadrant(quadrant1));
		assertEquals(2, GeoMath.checkQuadrant(quadrant2));
		assertEquals(3, GeoMath.checkQuadrant(quadrant3));
		assertEquals(4, GeoMath.checkQuadrant(quadrant4));
		
		
		// Kanten
		quadrant1 = new Vector2(0, 1);
		quadrant2 = new Vector2(0, -1);
		quadrant3 = new Vector2(-1, 0);
		quadrant4 = new Vector2(1, 0);
		
		assertEquals(1, GeoMath.checkQuadrant(quadrant1));
		assertEquals(3, GeoMath.checkQuadrant(quadrant2));
		assertEquals(3, GeoMath.checkQuadrant(quadrant3));
		assertEquals(1, GeoMath.checkQuadrant(quadrant4));
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
