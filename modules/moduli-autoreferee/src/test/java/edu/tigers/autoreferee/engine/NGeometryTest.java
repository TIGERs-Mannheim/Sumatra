/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author "Lukas Magel"
 */
public class NGeometryTest
{
	
	/**
	 * 
	 */
	@Test
	public void testClosestCorner()
	{
		Rectangle field = NGeometry.getField();
		assertEquals(field.bottomLeft(), NGeometry.getClosestCorner(field.bottomLeft()));
		assertEquals(field.bottomRight(), NGeometry.getClosestCorner(field.bottomRight()));
		assertEquals(field.topLeft(), NGeometry.getClosestCorner(field.topLeft()));
		assertEquals(field.topRight(), NGeometry.getClosestCorner(field.topRight()));
		
		assertEquals(field.bottomLeft(), NGeometry.getClosestCorner(new Vector2(-5, -5)));
		assertEquals(field.bottomRight(), NGeometry.getClosestCorner(new Vector2(5, -5)));
		assertEquals(field.topLeft(), NGeometry.getClosestCorner(new Vector2(-5, 5)));
		assertEquals(field.topRight(), NGeometry.getClosestCorner(new Vector2(5, 5)));
	}
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.NGeometry#getTeamOfClosestGoalLine(edu.tigers.sumatra.math.IVector2)}.
	 */
	@Test
	public void testGetTeamOfClosestGoalLine()
	{
		ETeamColor leftTeam = TeamConfig.getLeftTeam();
		
		assertEquals(leftTeam, NGeometry.getTeamOfClosestGoalLine(new Vector2(-5, 0)));
		assertEquals(leftTeam.opposite(), NGeometry.getTeamOfClosestGoalLine(new Vector2(5, 0)));
	}
	
	
	/**
	 * Test method for {@link edu.tigers.autoreferee.engine.NGeometry#ballInsideGoal(edu.tigers.sumatra.math.IVector2)}
	 */
	@Test
	public void testBallInsideGoal()
	{
		Rectangle field = Geometry.getField();
		assertFalse(NGeometry.ballInsideGoal(new Vector2(0, 0)));
		assertTrue(NGeometry.ballInsideGoal(new Vector2((field.getxExtend() / 2) + 1, 0)));
		assertTrue(NGeometry.ballInsideGoal(new Vector2(-(field.getxExtend() / 2) - 1, 0)));
		assertFalse(NGeometry.ballInsideGoal(new Vector2((field.getxExtend() / 2) + Geometry.getGoalDepth() + 1, 0)));
		assertFalse(NGeometry.ballInsideGoal(new Vector2(-(field.getxExtend() / 2) - Geometry.getGoalDepth() - 1, 0)));
	}
	
}
