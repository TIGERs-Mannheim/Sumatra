/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class NGeometryTest
{
	@Test
	void testClosestCorner()
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
	 * {@link NGeometry#getTeamOfClosestGoalLine(IVector2)}.
	 */
	@Test
	void testGetTeamOfClosestGoalLine()
	{
		ETeamColor leftTeam = Geometry.getNegativeHalfTeam();
		
		assertEquals(leftTeam, NGeometry.getTeamOfClosestGoalLine(Vector2.fromXY(-5, 0)));
		assertEquals(leftTeam.opposite(), NGeometry.getTeamOfClosestGoalLine(Vector2.fromXY(5, 0)));
	}
	
	
	/**
	 * Test method for {@link NGeometry#ballInsideGoal(IVector3)}
	 */
	@Test
	void testBallInsideGoal()
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
