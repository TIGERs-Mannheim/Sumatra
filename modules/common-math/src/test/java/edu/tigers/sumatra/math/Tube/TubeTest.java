/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.Tube;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * test methods of tube
 *
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class TubeTest
{
	private IVector2 startCenter = Vector2.zero();
	private IVector2 endCenter = Vector2.fromXY(1000, 0);
	private double radius = 10;
	
	
	@Test
	public void testIsPointInShape()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);
		
		Assert.assertTrue(tube.isPointInShape(startCenter));
		Assert.assertTrue(tube.isPointInShape(endCenter));
		Assert.assertTrue(tube.isPointInShape(Vector2.fromXY(-9, 0)));
		Assert.assertTrue(tube.isPointInShape(Vector2.fromXY(1009, 0)));
		Assert.assertTrue(tube.isPointInShape(Vector2.fromXY(0, 9)));
		Assert.assertTrue(tube.isPointInShape(Vector2.fromXY(1000, -9)));
		Assert.assertTrue((tube.isPointInShape(Vector2.fromXY(500, 0))));
		Assert.assertTrue(tube.isPointInShape(Vector2.fromXY(800, 9)));
		
		Assert.assertTrue(tubeZeroLength.isPointInShape(startCenter));
		Assert.assertTrue(tubeZeroLength.isPointInShape(Vector2.fromXY(-9, 0)));
		Assert.assertTrue(tubeZeroLength.isPointInShape(Vector2.fromXY(0, 9)));
		
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(-11, 0)));
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(0, -11)));
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(-1000, 0)));
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(500, 11)));
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(1000, 11)));
		Assert.assertFalse(tube.isPointInShape(Vector2.fromXY(1011, 0)));
		
		Assert.assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(-11, 0)));
		Assert.assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(0, -11)));
		Assert.assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(11, 0)));
		Assert.assertFalse(tubeZeroLength.isPointInShape(Vector2.fromXY(0, 11)));
	}
	
	
	@Test
	public void testIsIntersectingWithLine()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);
		
		IVector2 p1 = Vector2.fromXY(0, 20);
		IVector2 p2 = Vector2.fromXY(0, -20);
		IVector2 p3 = Vector2.fromXY(1000, 20);
		IVector2 p4 = Vector2.fromXY(500, -20);
		IVector2 p5 = Vector2.fromXY(-20, -20);
		IVector2 p6 = Vector2.fromXY(-10, 10);
		IVector2 p7 = Vector2.fromXY(-10, -10);
		IVector2 p8 = Vector2.fromXY(1000, 10);
		
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p1, p2)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p2, p3)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p1, p5)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p4, p3)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p6, p7)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p6, p8)));
		Assert.assertTrue(tube.isIntersectingWithLine(Line.fromPoints(p1, p8)));
		
		Assert.assertTrue(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p1, p2)));
		Assert.assertTrue(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p1, p5)));
		Assert.assertTrue(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p6, p7)));
		Assert.assertTrue(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p6, p8)));
		
		Assert.assertFalse(tube.isIntersectingWithLine(Line.fromPoints(p1, p3)));
		Assert.assertFalse(tube.isIntersectingWithLine(Line.fromPoints(p5, p4)));
		
		Assert.assertFalse(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p1, p3)));
		Assert.assertFalse(tubeZeroLength.isIntersectingWithLine(Line.fromPoints(p8, p3)));
	}
	
	
	@Test
	public void testNearestPointOutside()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);
		
		IVector2 pInside = Vector2.fromXY(0, 1);
		IVector2 nearestPointOutside = Vector2.fromXY(0, 10);
		
		Assert.assertEquals(tube.nearestPointOutside(pInside), nearestPointOutside);
		Assert.assertEquals(tube.nearestPointOutside(nearestPointOutside), nearestPointOutside);
		Assert.assertEquals(tubeZeroLength.nearestPointOutside(pInside), nearestPointOutside);
	}
	
	
	@Test
	public void testNearestPointInside()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);
		
		IVector2 nearestPointInside = Vector2.fromXY(0, 9);
		IVector2 pOutside = Vector2.fromXY(0, 11);
		
		Assert.assertEquals(tube.nearestPointInside(pOutside), nearestPointInside);
		Assert.assertEquals(tube.nearestPointInside(nearestPointInside), nearestPointInside);
		Assert.assertEquals(tubeZeroLength.nearestPointInside(pOutside), nearestPointInside);
	}
	
	
	@Test
	public void testLineIntersections()
	{
		ITube tube = Tube.create(startCenter, endCenter, radius);
		ITube tubeZeroLength = Tube.create(startCenter, startCenter, radius);
		
		IVector2 p1 = Vector2.fromXY(0, 20);
		IVector2 p2 = Vector2.fromXY(0, -20);
		IVector2 p3 = Vector2.fromXY(1000, 20);
		IVector2 p4 = Vector2.fromXY(-10, 0);
		IVector2 p5 = Vector2.fromXY(-10, 10);
		IVector2 p6 = Vector2.fromXY(10, 10);
		
		
		List<IVector2> intersections = tube.lineIntersections(Line.fromPoints(p1, p2));
		
		Assert.assertEquals(intersections.size(), 2);
		Assert.assertEquals(tube.lineIntersections(Line.fromPoints(p4, startCenter)).size(), 2);
		Assert.assertEquals(tube.lineIntersections(Line.fromPoints(p5, p4)).size(), 1);
		Assert.assertEquals(tube.lineIntersections(Line.fromPoints(p1, p3)).size(), 0);
		Assert.assertTrue(tube.lineIntersections(Line.fromPoints(p5, p6)).size() > 0);
		
		for (IVector2 v : intersections)
		{
			if (!(v.equals(Vector2.fromXY(0, 10)) || v.equals(Vector2.fromXY(0, -10))))
			{
				Assert.fail();
			}
		}
		
		Assert.assertEquals(tubeZeroLength.lineIntersections(Line.fromPoints(p1, p2)).size(), 2);
		Assert.assertEquals(tubeZeroLength.lineIntersections(Line.fromPoints(p4, startCenter)).size(), 2);
		Assert.assertEquals(tubeZeroLength.lineIntersections(Line.fromPoints(p4, p5)).size(), 1);
		Assert.assertEquals(tubeZeroLength.lineIntersections(Line.fromPoints(p6, p5)).size(), 1);
		Assert.assertEquals(tubeZeroLength.lineIntersections(Line.fromPoints(p1, p3)).size(), 0);
	}
	
}
