/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.shapes.field;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.shapes.I2DShapeTest;


/**
 * Test class for {@link PenaltyArea}
 * @author Frieder Berthold
 * 
 */
public class PenaltyAreaTest implements I2DShapeTest
{
	
	
	private final float	TOLERANCE	= 0.01f;
	
	
	/**
	 */
	@Before
	public void init()
	{
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, "RoboCup_2012.xml");
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
	}
	
	
	/**
	 * 
	 * Since AIConfig can't be used in this test, the config file needs to be read separately.
	 * Change file name in this method if the path changes.
	 * 
	 * @param owner
	 * @return
	 */
	public PenaltyArea initiatePenaltyArea(ETeam owner)
	{
		
		final String filePath = "./config/geometry/RoboCup_2012.xml";
		final XMLConfiguration xmlConfig = new XMLConfiguration();
		try
		{
			xmlConfig.setDelimiterParsingDisabled(true);
			xmlConfig.load(filePath);
			xmlConfig.setFileName(filePath);
			
		} catch (final ConfigurationException err)
		{
			System.out.println("Unable to load '" + filePath);
			return null;
		}
		
		
		return new PenaltyArea(owner, xmlConfig);
	}
	
	
	/**
	 * 
	 * Since PenaltyArea has no real getter, only PenaltyMark can be tested.
	 * 
	 */
	@Test
	public void testIsPenaltyAreaCorrect()
	{
		final PenaltyArea tigersArea = initiatePenaltyArea(ETeam.TIGERS);
		final PenaltyArea opponentsArea = initiatePenaltyArea(ETeam.OPPONENTS);
		
		
		assertTrue(tigersArea.getPenaltyMark().equals(new Vector2(-2275, 0)));
		assertTrue(opponentsArea.getPenaltyMark().equals(new Vector2(2275, 0)));
	}
	
	
	@Override
	@Test
	public void testIsPointInShape()
	{
		final PenaltyArea tigersArea = initiatePenaltyArea(ETeam.TIGERS);
		final PenaltyArea opponentsArea = initiatePenaltyArea(ETeam.OPPONENTS);
		
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
		final PenaltyArea tigersArea = initiatePenaltyArea(ETeam.TIGERS);
		final PenaltyArea opponentsArea = initiatePenaltyArea(ETeam.OPPONENTS);
		
		// inside our Area
		final Vector2 testPoint1 = new Vector2(-3000, 100);
		final Vector2 testPoint2 = new Vector2(-3025, -80);
		final Vector2 testPoint3 = new Vector2(-3000, 180);
		final Vector2 testPoint4 = new Vector2(-3000, -180);
		final Vector2 testPointReturn1 = new Vector2(-2135, 100);
		final Vector2 testPointReturn2 = new Vector2(-2135, -80);
		final Vector2 testPointReturn3 = new Vector2(-2152.2831f, 349.5433f);
		final Vector2 testPointReturn4 = new Vector2(-2152.2831f, -349.5433f);
		
		
		// inside their Area
		final Vector2 testPoint5 = new Vector2(3025, -100);
		final Vector2 testPoint6 = new Vector2(2225, 80);
		final Vector2 testPoint7 = new Vector2(3000, 180);
		final Vector2 testPoint8 = new Vector2(3000, -180);
		final Vector2 testPointReturn5 = new Vector2(2135, -100);
		final Vector2 testPointReturn6 = new Vector2(2135, 80);
		final Vector2 testPointReturn7 = new Vector2(2152.2831f, 349.5433f);
		final Vector2 testPointReturn8 = new Vector2(2152.2831f, -349.5433f);
		
		
		// outside of both Areas
		final Vector2 testPoint9 = new Vector2(100, 100);
		final Vector2 testPoint10 = new Vector2(100, 100);
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint5).equals(testPointReturn5, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint6).equals(testPointReturn6, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint7).equals(testPointReturn7, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint8).equals(testPointReturn8, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint9).equals(testPoint9, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint10).equals(testPoint10, TOLERANCE));
		
		assertTrue(tigersArea.nearestPointOutside(testPoint1).equals(testPointReturn1, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint2).equals(testPointReturn2, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint3).equals(testPointReturn3, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint4).equals(testPointReturn4, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint9).equals(testPoint9, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint10).equals(testPoint10, TOLERANCE));
	}
	
	
	/**
	 */
	@Test
	public void testStepAlongPenArea()
	{
		doTestStepAlongPenArea(initiatePenaltyArea(ETeam.OPPONENTS));
		doTestStepAlongPenArea(initiatePenaltyArea(ETeam.TIGERS));
	}
	
	
	private void doTestStepAlongPenArea(PenaltyArea penaltyArea)
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
		final PenaltyArea tigersArea = initiatePenaltyArea(ETeam.TIGERS);
		final PenaltyArea opponentsArea = initiatePenaltyArea(ETeam.OPPONENTS);
		
		// inside our Area
		// points 11: insidePoint in rectancle, intersection on edge from rectangle and pos. circle
		final Vector2 testPoint1 = new Vector2(-3000, 100);
		final Vector2 testPoint2 = new Vector2(-3025, -80);
		final Vector2 testPoint3 = new Vector2(-3000, 180);
		final Vector2 testPoint4 = new Vector2(-3000, -180);
		final Vector2 testPoint11 = new Vector2(-2400, 0);
		final Vector2 testPoint13 = new Vector2(-3000, 180);
		final Vector2 testPointLine1 = new Vector2(-2000, 100);
		final Vector2 testPointLine2 = new Vector2(-2000, -80);
		final Vector2 testPointLine3 = new Vector2(-1400, 500);
		final Vector2 testPointLine4 = new Vector2(-1400, -500);
		final Vector2 testPointLine11 = new Vector2(-2050, 350);
		final Vector2 testPointLine13 = new Vector2(-3000, -980);
		final Vector2 testPointReturn1 = new Vector2(-2135, 100);
		final Vector2 testPointReturn2 = new Vector2(-2135, -80);
		final Vector2 testPointReturn3 = new Vector2(-2152.2831f, 349.5433f);
		final Vector2 testPointReturn4 = new Vector2(-2152.2831f, -349.5433f);
		final Vector2 testPointReturn11 = new Vector2(-2161.3604f, 238.6396f);
		final Vector2 testPointReturn13 = new Vector2(-3000f, -1064.6092f);
		
		
		// inside their Area
		final Vector2 testPoint5 = new Vector2(3025, -100);
		final Vector2 testPoint6 = new Vector2(2225, 80);
		final Vector2 testPoint7 = new Vector2(3000, 180);
		final Vector2 testPoint8 = new Vector2(3000, -180);
		final Vector2 testPoint12 = new Vector2(2400, 0);
		final Vector2 testPoint14 = new Vector2(3000, 180);
		final Vector2 testPointLine5 = new Vector2(2000, -100);
		final Vector2 testPointLine6 = new Vector2(2000, 80);
		final Vector2 testPointLine7 = new Vector2(1400, 500);
		final Vector2 testPointLine8 = new Vector2(1400, -500);
		final Vector2 testPointLine12 = new Vector2(2050, 350);
		final Vector2 testPointLine14 = new Vector2(3000, -980);
		final Vector2 testPointReturn5 = new Vector2(2135, -100);
		final Vector2 testPointReturn6 = new Vector2(2135, 80);
		final Vector2 testPointReturn7 = new Vector2(2152.2831f, 349.5433f);
		final Vector2 testPointReturn8 = new Vector2(2152.2831f, -349.5433f);
		final Vector2 testPointReturn12 = new Vector2(2161.3604f, 238.6396f);
		final Vector2 testPointReturn14 = new Vector2(3000f, -1064.6092f);
		
		// Get intersection points
		// Line testLine = new Line();
		// testLine.setPoints(testPoint13, testPointLine13);
		// Circle testCirle = new Circle(new Vector2(-3025, -175), 800);
		//
		// List<IVector2> intersections = testCirle.lineIntersections(testLine);
		//
		// for (IVector2 intersection : intersections)
		// {
		// System.out.println(intersection.toString());
		// }
		//
		
		// outside of both Areas
		final Vector2 testPoint9 = new Vector2(100, 100);
		final Vector2 testPoint10 = new Vector2(100, 100);
		
		assertTrue(opponentsArea.nearestPointOutside(testPoint5, testPointLine5).equals(testPointReturn5, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint6, testPointLine6).equals(testPointReturn6, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint7, testPointLine7).equals(testPointReturn7, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint8, testPointLine8).equals(testPointReturn8, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint9).equals(testPoint9, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint10).equals(testPoint10, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint12, testPointLine12).equals(testPointReturn12, TOLERANCE));
		assertTrue(opponentsArea.nearestPointOutside(testPoint14, testPointLine14).equals(testPointReturn14, TOLERANCE));
		
		assertTrue(tigersArea.nearestPointOutside(testPoint1, testPointLine1).equals(testPointReturn1, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint2, testPointLine2).equals(testPointReturn2, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint3, testPointLine3).equals(testPointReturn3, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint4, testPointLine4).equals(testPointReturn4, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint9, testPointLine1).equals(testPoint9, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint10, testPointLine1).equals(testPoint10, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint11, testPointLine11).equals(testPointReturn11, TOLERANCE));
		assertTrue(tigersArea.nearestPointOutside(testPoint13, testPointLine13).equals(testPointReturn13, TOLERANCE));
	}
}
