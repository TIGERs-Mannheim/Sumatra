/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.TeamConfig;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMathTest
{
	
	/**
	 * Test method for {@link edu.tigers.autoreferee.engine.AutoRefMath#getClosestCornerKickPos(IVector2)}.
	 */
	@Test
	public void testGetClosestCornerKickPos()
	{
		IRectangle field = NGeometry.getField();
		List<IVector2> corners = field.getCorners();
		List<IVector2> closerCorners = field.withMargin(-AutoRefMath.THROW_IN_DISTANCE).getCorners();
		
		Rectangle smallRect = Rectangle.fromCenter(AVector2.ZERO_VECTOR, 5, 5);
		List<IVector2> smallCorners = smallRect.getCorners();
		
		for (int i = 0; i < 4; i++)
		{
			assertEquals(closerCorners.get(i), AutoRefMath.getClosestCornerKickPos(corners.get(i)));
			assertEquals(closerCorners.get(i), AutoRefMath.getClosestCornerKickPos(smallCorners.get(i)));
		}
	}
	
	
	/**
	 * Test method for {@link edu.tigers.autoreferee.engine.AutoRefMath#getClosestGoalKickPos(IVector2)}.
	 */
	@Test
	public void testGetClosestGoalKickPos()
	{
		IRectangle field = NGeometry.getField();
		List<IVector2> corners = field.getCorners();
		List<IVector2> closerCorners = field
				.withMarginXy(-AutoRefMath.GOAL_KICK_DISTANCE, -AutoRefMath.THROW_IN_DISTANCE)
				.getCorners();
		Rectangle smallRect = Rectangle.fromCenter(AVector2.ZERO_VECTOR, 5, 5);
		List<IVector2> smallCorners = smallRect.getCorners();
		
		for (int i = 0; i < 4; i++)
		{
			assertEquals(closerCorners.get(i), AutoRefMath.getClosestGoalKickPos(corners.get(i)));
			assertEquals(closerCorners.get(i), AutoRefMath.getClosestGoalKickPos(smallCorners.get(i)));
		}
	}
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.AutoRefMath#distanceToNearestPointOutside(IPenaltyArea, IVector2)}.
	 */
	@Test
	public void testDistanceToNearestPointOutside()
	{
		double delta = 0.001d;
		IPenaltyArea penArea = Geometry.getPenaltyAreaOur();
		IVector2 center = Geometry.getCenter();
		IVector2 penMark = Geometry.getPenaltyMarkOur();
		
		IVector2 penMarkToGoalCenter = penArea.getGoalCenter().subtractNew(penMark);
		IVector2 penAreaCenter = penMark.addNew(penMarkToGoalCenter.multiplyNew(0.5d));
		
		assertEquals(0.0d, AutoRefMath.distanceToNearestPointOutside(penArea, center), delta);
		assertEquals(0.0d, AutoRefMath.distanceToNearestPointOutside(penArea, penMark), delta);
		assertEquals(VectorMath.distancePP(penMark, penAreaCenter),
				AutoRefMath.distanceToNearestPointOutside(penArea, penAreaCenter), delta);
		
	}
	
	
	@Test
	public void testPositionInPenaltyKickArea()
	{
		ETeamColor leftTeam = TeamConfig.getLeftTeam();
		ETeamColor rightTeam = leftTeam.opposite();
		
		double maxX = Geometry.getPenaltyMarkTheir().x()
				- Geometry.getDistancePenaltyMarkToPenaltyLine();
		double margin = 10.0d;
		IVector2 posInside = Vector2.fromXY(maxX + (margin / 2.0d), 0);
		IVector2 posOutside = Vector2.fromXY(maxX - (margin / 2.0d), 0);
		
		assertTrue(AutoRefMath.positionInPenaltyKickArea(leftTeam, posInside, 0.0d));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posInside, -margin));
		
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posOutside, 0.0d));
		assertTrue(AutoRefMath.positionInPenaltyKickArea(leftTeam, posOutside, margin));
		
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posInside, 0.0d));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posInside, -margin));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posOutside, 0.0));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posOutside, margin));
		
		posInside = posInside.multiplyNew(-1.0d);
		posOutside = posOutside.multiplyNew(-1.0d);
		
		assertTrue(AutoRefMath.positionInPenaltyKickArea(rightTeam, posInside, 0.0d));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posInside, -margin));
		
		assertFalse(AutoRefMath.positionInPenaltyKickArea(rightTeam, posOutside, 0.0d));
		assertTrue(AutoRefMath.positionInPenaltyKickArea(rightTeam, posOutside, margin));
		
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posInside, 0.0d));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posInside, -margin));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posOutside, 0.0d));
		assertFalse(AutoRefMath.positionInPenaltyKickArea(leftTeam, posOutside, margin));
	}
}
