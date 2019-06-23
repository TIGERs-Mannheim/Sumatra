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

import org.junit.Test;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMathTest
{
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.AutoRefMath#getClosestCornerKickPos(edu.tigers.sumatra.math.IVector2)}.
	 */
	@Test
	public void testGetClosestCornerKickPos()
	{
		Rectangle field = NGeometry.getField();
		IVector2 topLeftCornerKick = field.topLeft().addNew(
				new Vector2(AutoRefMath.THROW_IN_DISTANCE, -AutoRefMath.THROW_IN_DISTANCE));
		IVector2 topRightCornerKick = field.topRight().addNew(
				new Vector2(-AutoRefMath.THROW_IN_DISTANCE, -AutoRefMath.THROW_IN_DISTANCE));
		IVector2 bottomLeftCornerKick = field.bottomLeft().addNew(
				new Vector2(AutoRefMath.THROW_IN_DISTANCE, AutoRefMath.THROW_IN_DISTANCE));
		IVector2 bottomRightCornerKick = field.bottomRight().addNew(
				new Vector2(-AutoRefMath.THROW_IN_DISTANCE, AutoRefMath.THROW_IN_DISTANCE));
		
		assertEquals(topLeftCornerKick, AutoRefMath.getClosestCornerKickPos(field.topLeft()));
		assertEquals(topRightCornerKick, AutoRefMath.getClosestCornerKickPos(field.topRight()));
		assertEquals(bottomLeftCornerKick, AutoRefMath.getClosestCornerKickPos(field.bottomLeft()));
		assertEquals(bottomRightCornerKick, AutoRefMath.getClosestCornerKickPos(field.bottomRight()));
		
		assertEquals(topLeftCornerKick, AutoRefMath.getClosestCornerKickPos(new Vector2(-5, 5)));
		assertEquals(topRightCornerKick, AutoRefMath.getClosestCornerKickPos(new Vector2(5, 5)));
		assertEquals(bottomLeftCornerKick, AutoRefMath.getClosestCornerKickPos(new Vector2(-5, -5)));
		assertEquals(bottomRightCornerKick, AutoRefMath.getClosestCornerKickPos(new Vector2(5, -5)));
	}
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.AutoRefMath#getClosestGoalKickPos(edu.tigers.sumatra.math.IVector2)}.
	 */
	@Test
	public void testGetClosestGoalKickPos()
	{
		Rectangle field = NGeometry.getField();
		IVector2 topLeftGoalKick = field.topLeft().addNew(
				new Vector2(AutoRefMath.GOAL_KICK_DISTANCE, -AutoRefMath.THROW_IN_DISTANCE));
		IVector2 topRightGoalKick = field.topRight().addNew(
				new Vector2(-AutoRefMath.GOAL_KICK_DISTANCE, -AutoRefMath.THROW_IN_DISTANCE));
		IVector2 bottomLeftGoalKick = field.bottomLeft().addNew(
				new Vector2(AutoRefMath.GOAL_KICK_DISTANCE, AutoRefMath.THROW_IN_DISTANCE));
		IVector2 bottomRightGoalKick = field.bottomRight().addNew(
				new Vector2(-AutoRefMath.GOAL_KICK_DISTANCE, AutoRefMath.THROW_IN_DISTANCE));
		
		assertEquals(topLeftGoalKick, AutoRefMath.getClosestGoalKickPos(field.topLeft()));
		assertEquals(topRightGoalKick, AutoRefMath.getClosestGoalKickPos(field.topRight()));
		assertEquals(bottomLeftGoalKick, AutoRefMath.getClosestGoalKickPos(field.bottomLeft()));
		assertEquals(bottomRightGoalKick, AutoRefMath.getClosestGoalKickPos(field.bottomRight()));
		
		assertEquals(topLeftGoalKick, AutoRefMath.getClosestGoalKickPos(new Vector2(-5, 5)));
		assertEquals(topRightGoalKick, AutoRefMath.getClosestGoalKickPos(new Vector2(5, 5)));
		assertEquals(bottomLeftGoalKick, AutoRefMath.getClosestGoalKickPos(new Vector2(-5, -5)));
		assertEquals(bottomRightGoalKick, AutoRefMath.getClosestGoalKickPos(new Vector2(5, -5)));
	}
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.AutoRefMath#distanceToNearestPointOutside(PenaltyArea, IVector2)}.
	 */
	@Test
	public void testDistanceToNearestPointOutside()
	{
		double delta = 0.001d;
		PenaltyArea penArea = Geometry.getPenaltyAreaOur();
		IVector2 center = Geometry.getCenter();
		IVector2 penMark = Geometry.getPenaltyMarkOur();
		
		IVector2 penMarkToGoalCenter = penArea.getGoalCenter().subtractNew(penMark);
		IVector2 penAreaCenter = penMark.addNew(penMarkToGoalCenter.multiplyNew(0.5d));
		
		assertEquals(0.0d, AutoRefMath.distanceToNearestPointOutside(penArea, center), delta);
		assertEquals(0.0d, AutoRefMath.distanceToNearestPointOutside(penArea, penMark), delta);
		assertEquals(GeoMath.distancePP(penMark, penAreaCenter),
				AutoRefMath.distanceToNearestPointOutside(penArea, penAreaCenter), delta);
		
	}
	
}
