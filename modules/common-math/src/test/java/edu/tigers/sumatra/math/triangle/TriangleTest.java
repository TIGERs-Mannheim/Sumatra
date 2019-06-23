/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Lukas Magel
 */
public class TriangleTest
{
	
	/**
	 * Test method for
	 * {@link Triangle#isPointInShape(IVector2, double)}.
	 */
	@Test
	public void testIsPointInShapeWithMargin()
	{
		double margin = 250d;
		Vector2 a = Vector2.fromXY(0.0, 0.0);
		Vector2 b = Vector2.fromXY(1000.0, 0.0);
		Vector2 c = Vector2.fromXY(0.0, 1000.0);
		
		Triangle triangle = Triangle.fromCorners(a, b, c);
		
		testCornersWithMargin(triangle, margin);
		
		// Testing Corners here
		Assert.assertTrue(triangle.isPointInShape(a));
		Assert.assertTrue(triangle.isPointInShape(b));
		Assert.assertTrue(triangle.isPointInShape(c));
		
		Assert.assertTrue(triangle.isPointInShape(a, margin));
		Assert.assertTrue(triangle.isPointInShape(b, margin));
		Assert.assertTrue(triangle.isPointInShape(c, margin));
		
		Assert.assertFalse(triangle.isPointInShape(a, -margin));
		Assert.assertFalse(triangle.isPointInShape(b, -margin));
		Assert.assertFalse(triangle.isPointInShape(c, -margin));
	}
	
	
	/**
	 * Lay a circle around each corner with the same radius as the margin and assert that each point on the circle edge
	 * is located inside the triangle
	 * 
	 * @param triangle
	 * @param margin
	 */
	private void testCornersWithMargin(final Triangle triangle, final double margin)
	{
		List<IVector2> corners = triangle.getCorners();
		for (int i = 0; i < corners.size(); i++)
		{
			IVector2 corner = corners.get(i);
			IVector2 otherCorner = corners.get((i + 1) % 3);
			Vector2 edgePoint = corner.addNew(otherCorner.subtractNew(corner).scaleTo(margin * 0.999));
			
			for (int j = 0; j < 100; j++)
			{
				IVector2 curPoint = CircleMath.stepAlongCircle(edgePoint, corner, Math.toRadians((j * 360.0d) / 100.0d));
				Assert.assertTrue(triangle.isPointInShape(curPoint, margin));
			}
		}
	}
}
