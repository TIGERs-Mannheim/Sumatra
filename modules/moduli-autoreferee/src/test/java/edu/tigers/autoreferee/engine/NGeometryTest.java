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
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


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
		IRectangle field = NGeometry.getField();
		List<IVector2> corners = field.getCorners();
		List<IVector2> closerCorners = field.withMargin(-5).getCorners();
		for (IVector2 corner : corners)
		{
			assertEquals(corner, NGeometry.getClosestCorner(corner));
		}
		for (int i = 0; i < 4; i++)
		{
			assertEquals(corners.get(i), NGeometry.getClosestCorner(closerCorners.get(i)));
		}
	}
	
	
	/**
	 * Test method for
	 * {@link edu.tigers.autoreferee.engine.NGeometry#getTeamOfClosestGoalLine(IVector2)}.
	 */
	@Test
	public void testGetTeamOfClosestGoalLine()
	{
		ETeamColor leftTeam = Geometry.getNegativeHalfTeam();
		
		assertEquals(leftTeam, NGeometry.getTeamOfClosestGoalLine(Vector2.fromXY(-5, 0)));
		assertEquals(leftTeam.opposite(), NGeometry.getTeamOfClosestGoalLine(Vector2.fromXY(5, 0)));
	}
	
	
	/**
	 * Test method for {@link edu.tigers.autoreferee.engine.NGeometry#ballInsideGoal(IVector3)}
	 */
	@Test
	public void testBallInsideGoal()
	{
		IRectangle field = Geometry.getField();
		assertFalse(NGeometry.ballInsideGoal(Vector3.zero()));
		assertTrue(NGeometry.ballInsideGoal(Vector3.fromXY((field.xExtent() / 2) + 1, 0)));
		assertTrue(NGeometry.ballInsideGoal(Vector3.fromXY(-(field.xExtent() / 2) - 1, 0)));
		assertFalse(NGeometry.ballInsideGoal(Vector3.fromXYZ((field.xExtent() / 2) + 1, 0, Geometry.getGoalHeight())));
		assertFalse(
				NGeometry.ballInsideGoal(Vector3.fromXY((field.xExtent() / 2) + Geometry.getGoalOur().getDepth() + 1, 0)));
		assertFalse(
				NGeometry.ballInsideGoal(Vector3.fromXY(-(field.xExtent() / 2) - Geometry.getGoalOur().getDepth() - 1, 0)));
		
	}
	
}
