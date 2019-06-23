/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShapeTest;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * Test class for {@link PenaltyArea}
 * 
 * @author Frieder Berthold
 */
public class PenaltyAreaTest implements I2DShapeTest
{
	
	
	private final float	TOLERANCE	= 0.01f;
	
	
	/**
	 */
	@Before
	public void init()
	{
		SumatraSetupHelper.setupSumatra();
	}
	
	
	/**
	 * Since PenaltyArea has no real getter, only PenaltyMark can be tested.
	 */
	public void testIsPenaltyAreaCorrect()
	{
		final PenaltyArea tigersArea = AIConfig.getGeometry().getPenaltyAreaOur();
		final PenaltyArea opponentsArea = AIConfig.getGeometry().getPenaltyAreaTheir();
		
		
		assertTrue(tigersArea.getPenaltyMark().equals(new Vector2(-2275, 0)));
		assertTrue(opponentsArea.getPenaltyMark().equals(new Vector2(2275, 0)));
	}
	
	
	@Override
	public void testIsPointInShape()
	{
		final PenaltyArea tigersArea = AIConfig.getGeometry().getPenaltyAreaOur();
		final PenaltyArea opponentsArea = AIConfig.getGeometry().getPenaltyAreaTheir();
		
		// inside our Area
		final Vector2 testPoint1 = new Vector2(-3024, 974);
		final Vector2 testPoint2 = new Vector2(-2225, -175);
		final Vector2 testPoint3 = new Vector2(-2725, 741);
		final Vector2 testPoint4 = new Vector2(-2500, 160);
		// Vector2 testPoint = new Vector2(,);
		// Vector2 testPoint = new Vector2(,);
		
		// inside their Area
		final Vector2 testPoint5 = new Vector2(3025, -974);
		final Vector2 testPoint6 = new Vector2(2225, 80);
		final Vector2 testPoint7 = new Vector2(2725, -741);
		final Vector2 testPoint8 = new Vector2(2500, 160);
		
		
		// outside of both Areas
		final Vector2 testPoint9 = new Vector2(100, 100);
		final Vector2 testPoint10 = new Vector2(100, 100);
		
		assertTrue(!opponentsArea.isPointInShape(testPoint1));
		assertTrue(!opponentsArea.isPointInShape(testPoint2));
		assertTrue(!opponentsArea.isPointInShape(testPoint3));
		assertTrue(!opponentsArea.isPointInShape(testPoint4));
		assertTrue(opponentsArea.isPointInShape(testPoint5));
		assertTrue(opponentsArea.isPointInShape(testPoint6));
		assertTrue(opponentsArea.isPointInShape(testPoint7));
		assertTrue(opponentsArea.isPointInShape(testPoint8));
		assertTrue(!opponentsArea.isPointInShape(testPoint9));
		assertTrue(!opponentsArea.isPointInShape(testPoint10));
		
		assertTrue(tigersArea.isPointInShape(testPoint1));
		assertTrue(tigersArea.isPointInShape(testPoint2));
		assertTrue(tigersArea.isPointInShape(testPoint3));
		assertTrue(tigersArea.isPointInShape(testPoint4));
		assertTrue(!tigersArea.isPointInShape(testPoint5));
		assertTrue(!tigersArea.isPointInShape(testPoint6));
		assertTrue(!tigersArea.isPointInShape(testPoint7));
		assertTrue(!tigersArea.isPointInShape(testPoint8));
		assertTrue(!tigersArea.isPointInShape(testPoint9));
		assertTrue(!tigersArea.isPointInShape(testPoint10));
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
	public void testIsLineIntersectingShape()
	{
		fail("Not implemented");
	}
	
	
	@Override
	@Test
	public void testNearestPointOutside()
	{
		final PenaltyArea tigersArea = AIConfig.getGeometry().getPenaltyAreaOur();
		final PenaltyArea opponentsArea = AIConfig.getGeometry().getPenaltyAreaTheir();
		
		// inside our Area
		final IVector2 testPoint1 = tigersArea.getPenaltyCirclePosCentre().addNew(
				new Vector2(tigersArea.getPenaltyCirclePos().radius() / 2, tigersArea.getPenaltyCirclePos().radius() / 3));
		final IVector2 testPointReturn1 = tigersArea.getPenaltyCirclePos().nearestPointOutside(testPoint1);
		final IVector2 testPoint2 = tigersArea.getPenaltyCircleNegCentre().addNew(
				new Vector2(tigersArea.getPenaltyCirclePos().radius() / 3, -tigersArea.getPenaltyCirclePos().radius() / 4));
		final IVector2 testPointReturn2 = tigersArea.getPenaltyCircleNeg().nearestPointOutside(testPoint2);
		final IVector2 testPoint3 = tigersArea.getGoalCenter().addNew(
				new Vector2(tigersArea.getRadiusOfPenaltyArea() / 3, tigersArea.getRadiusOfPenaltyArea() / 4));
		final IVector2 testPointReturn3 = new Vector2(
				tigersArea.getPenaltyCirclePosCentre().x() + tigersArea.getRadiusOfPenaltyArea(), testPoint3.y());
		
		// inside their Area
		final IVector2 testPoint4 = opponentsArea.getPenaltyCirclePosCentre().addNew(
				new Vector2(-opponentsArea.getPenaltyCirclePos().radius() / 2,
						opponentsArea.getPenaltyCirclePos().radius() / 3));
		final IVector2 testPointReturn4 = opponentsArea.getPenaltyCirclePos().nearestPointOutside(testPoint4);
		final IVector2 testPoint5 = opponentsArea.getPenaltyCircleNegCentre().addNew(
				new Vector2(-opponentsArea.getPenaltyCirclePos().radius() / 3, -opponentsArea.getPenaltyCirclePos()
						.radius() / 4));
		final IVector2 testPointReturn5 = opponentsArea.getPenaltyCircleNeg().nearestPointOutside(testPoint5);
		final IVector2 testPoint6 = opponentsArea.getGoalCenter().addNew(
				new Vector2(-opponentsArea.getRadiusOfPenaltyArea() / 3, opponentsArea.getRadiusOfPenaltyArea() / 4));
		final IVector2 testPointReturn6 = new Vector2(
				opponentsArea.getPenaltyCirclePosCentre().x() - opponentsArea.getRadiusOfPenaltyArea(), testPoint6.y());
		
		// outside of both Areas
		final IVector2 testPoint7 = new Vector2();
		final IVector2 testPoint8 = new Vector2(
				opponentsArea.getPenaltyCirclePosCentre().x() - opponentsArea.getRadiusOfPenaltyArea(), testPoint6.y());
		
		assertTrue(tigersArea.nearestPointOutside(testPoint1).equals(testPointReturn1, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint2).equals(testPointReturn2, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint3).equals(testPointReturn3, TOLERANCE));
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint4).equals(testPointReturn4, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint5).equals(testPointReturn5, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint6).equals(testPointReturn6, TOLERANCE));
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint7).equals(testPoint7, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint8).equals(testPoint8, TOLERANCE));
	}
	
	
	/**
	 */
	@Test
	public void testStepAlongPenArea()
	{
		doTestStepAlongPenArea(AIConfig.getGeometry().getPenaltyAreaOur());
		doTestStepAlongPenArea(AIConfig.getGeometry().getPenaltyAreaOur());
	}
	
	
	private void doTestStepAlongPenArea(final PenaltyArea penaltyArea)
	{
		float perimeterQuart = (penaltyArea.getRadiusOfPenaltyArea() * AngleMath.PI) / 2;
		float frontLineLen = penaltyArea.getLengthOfPenaltyAreaFrontLineHalf() * 2;
		IVector2 pFrontLinePos = new Vector2(penaltyArea.getPenaltyAreaFrontLine().supportVector().x(), penaltyArea
				.getPenaltyRectangle().yExtend() / 2);
		IVector2 pFrontLineNeg = new Vector2(penaltyArea.getPenaltyAreaFrontLine().supportVector().x(), -penaltyArea
				.getPenaltyRectangle().yExtend() / 2);
		IVector2 pLastPoint = new Vector2(penaltyArea.getGoalCenter().x(),
				-((penaltyArea.getPenaltyRectangle().yExtend() / 2) + penaltyArea.getPenaltyCircleNeg().radius()));
		
		IVector2 point;
		point = penaltyArea.stepAlongPenArea(perimeterQuart);
		assertTrue("Exp:" + pFrontLinePos + " but:" + point, pFrontLinePos.equals(point, 5));
		point = penaltyArea.stepAlongPenArea(perimeterQuart + frontLineLen);
		assertTrue("Exp:" + pFrontLineNeg + " but:" + point, pFrontLineNeg.equals(point, 5));
		point = penaltyArea.stepAlongPenArea((perimeterQuart * 2) + frontLineLen);
		assertTrue("Exp:" + pLastPoint + " but:" + point, pLastPoint.equals(point, 5));
	}
	
	
	/**
	 */
	@Test
	public void testNearestPointOutsideWithLine()
	{
		final PenaltyArea tigersArea = AIConfig.getGeometry().getPenaltyAreaOur();
		final PenaltyArea opponentsArea = AIConfig.getGeometry().getPenaltyAreaTheir();
		
		// inside our Area
		final IVector2 testPoint1 = tigersArea.getPenaltyCirclePosCentre().addNew(
				new Vector2(tigersArea.getPenaltyCirclePos().radius() / 2, tigersArea.getPenaltyCirclePos().radius() / 3));
		final IVector2 testPointReturn1 = tigersArea.getPenaltyCirclePos().nearestPointOutside(testPoint1);
		final IVector2 testPoint2 = tigersArea.getPenaltyCircleNegCentre().addNew(
				new Vector2(tigersArea.getPenaltyCirclePos().radius() / 3, -tigersArea.getPenaltyCirclePos().radius() / 4));
		final IVector2 testPointReturn2 = tigersArea.getPenaltyCircleNeg().nearestPointOutside(testPoint2);
		final IVector2 testPoint3 = tigersArea.getGoalCenter().addNew(
				new Vector2(tigersArea.getRadiusOfPenaltyArea() / 3, tigersArea.getRadiusOfPenaltyArea() / 4));
		final IVector2 testPointReturn3 = new Vector2(
				tigersArea.getPenaltyCirclePosCentre().x() + tigersArea.getRadiusOfPenaltyArea(), testPoint3.y()
						- (tigersArea.getRadiusOfPenaltyArea() / 4));
		
		// inside their Area
		final IVector2 testPoint4 = opponentsArea.getPenaltyCirclePosCentre().addNew(
				new Vector2(-opponentsArea.getPenaltyCirclePos().radius() / 2,
						opponentsArea.getPenaltyCirclePos().radius() / 3));
		final IVector2 testPointReturn4 = opponentsArea.getPenaltyCirclePos().nearestPointOutside(testPoint4);
		final IVector2 testPoint5 = opponentsArea.getPenaltyCircleNegCentre().addNew(
				new Vector2(-opponentsArea.getPenaltyCirclePos().radius() / 3, -opponentsArea.getPenaltyCirclePos()
						.radius() / 4));
		final IVector2 testPointReturn5 = opponentsArea.getPenaltyCircleNeg().nearestPointOutside(testPoint5);
		final IVector2 testPoint6 = opponentsArea.getGoalCenter().addNew(
				new Vector2(-opponentsArea.getRadiusOfPenaltyArea() / 3, opponentsArea.getRadiusOfPenaltyArea() / 4));
		final IVector2 testPointReturn6 = new Vector2(
				opponentsArea.getPenaltyCirclePosCentre().x() - opponentsArea.getRadiusOfPenaltyArea(), testPoint6.y()
						- (tigersArea.getRadiusOfPenaltyArea() / 4));
		
		// outside of both Areas
		final IVector2 testPoint7 = new Vector2();
		final IVector2 testPoint8 = new Vector2(
				opponentsArea.getPenaltyCirclePosCentre().x() - opponentsArea.getRadiusOfPenaltyArea(), testPoint6.y());
		
		assertTrue(tigersArea.nearestPointOutside(testPoint1, testPointReturn1, 0).equals(testPointReturn1, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint2, testPointReturn2, 0).equals(testPointReturn2, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint3, testPointReturn3, 0).equals(testPointReturn3, TOLERANCE));
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint4, testPointReturn4, 0).equals(testPointReturn4, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint5, testPointReturn5, 0).equals(testPointReturn5, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint6, testPointReturn6, 0).equals(testPointReturn6, TOLERANCE));
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint7, testPoint8, 0).equals(testPoint7, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint8, testPoint7, 0).equals(testPoint8, TOLERANCE));
	}
}
