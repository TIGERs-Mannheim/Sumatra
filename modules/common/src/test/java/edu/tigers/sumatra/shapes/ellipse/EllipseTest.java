/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 18, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.ellipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.I2DShapeTest;


/**
 * Test class for ellipse
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class EllipseTest implements I2DShapeTest
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final double	ASSERT_EQUALS_DELTA	= 0.001;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private List<Ellipse> getFlatEllipses()
	{
		final List<Ellipse> ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = new Ellipse(new Vector2(0, 0), 100, 70);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, 100), 100, 70);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 100, 70);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 100, 70);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, 100), 100, 70);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 10, 9);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 13);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 4000, 2000);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 4000, 3999);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 1000);
		ells.add(tmpEll);
		return ells;
	}
	
	
	private List<Ellipse> getThinEllipses()
	{
		final List<Ellipse> ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = new Ellipse(new Vector2(0, 0), 70, 100);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, 100), 70, 100);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 70, 100);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 70, 100);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, 100), 70, 100);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 9, 10);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 13, 1000);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 2000, 4000);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 3999, 4000);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 1000);
		ells.add(tmpEll);
		return ells;
	}
	
	
	private List<Ellipse> getTurnedEllipses()
	{
		// both flat and thin
		final List<Ellipse> ells = new ArrayList<Ellipse>(10);
		Ellipse tmpEll;
		tmpEll = new Ellipse(new Vector2(0, 0), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 70, 100, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 70, 100, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 70, 100, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, 100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 70, 100, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 70, 100, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 70, 100, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, 100), 70, 100, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 9, 10, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 13, 1000, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 2000, 4000, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 3999, 4000, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 1000, Math.PI);
		ells.add(tmpEll);
		
		tmpEll = new Ellipse(new Vector2(0, 0), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, 100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 100, 70, Math.PI / 2.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 100, 70, Math.PI / 4.0);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, -100), 100, 70, Math.PI * 2);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(100, -100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(-100, 100), 100, 70, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 10, 9, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 13, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 4000, 2000, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 4000, 3999, Math.PI);
		ells.add(tmpEll);
		tmpEll = new Ellipse(new Vector2(0, 0), 1000, 1000, Math.PI);
		ells.add(tmpEll);
		return ells;
	}
	
	
	private List<Ellipse> getAllEllipses()
	{
		final List<Ellipse> ells = new ArrayList<Ellipse>(50);
		ells.addAll(getFlatEllipses());
		ells.addAll(getThinEllipses());
		ells.addAll(getTurnedEllipses());
		return ells;
	}
	
	
	/**
	 * Test {@link Ellipse#getFocusFromCenter()}
	 */
	@Test
	public void testGetFocusFromCenter()
	{
		for (Ellipse ell : getAllEllipses())
		{
			final String errStr = "Failed for " + ell.toString();
			
			// vector from center to focus without turn angle of ell
			IVector2 focusFromCenter = ell.getFocusFromCenter().turnNew(-ell.getTurnAngle());
			double distCF = GeoMath.distancePP(ell.getCenter().addNew(focusFromCenter), ell.getCenter());
			
			// final double x = (ell.getRadiusX() < ell.getRadiusY() ? ell.getRadiusX() : 0);
			// final double y = (ell.getRadiusX() < ell.getRadiusY() ? 0 : ell.getRadiusY());
			
			// focus should be on one of the axis (remember, we turned focusFromCenter)
			Assert.assertEquals(errStr, 0, Math.min(focusFromCenter.x(), focusFromCenter.y()), ASSERT_EQUALS_DELTA);
			// is distance center <-> focus smaller than greater radius?
			Assert.assertTrue(errStr + " " + distCF, distCF < Math.max(ell.getRadiusX(), ell.getRadiusY()));
		}
	}
	
	
	// /**
	// * Test {@link Ellipse#getFocusFromCenter()} for flat ellipse
	// *
	// */
	// @Test
	// public void testGetFocusFromCenterFlat()
	// {
	// for (Ellipse ell : getFlatEllipses())
	// {
	// IVector2 focusFromCenter = ell.getFocusFromCenter();
	// final String errStr = "Failed for " + ell.toString();
	// Assert.assertEquals(errStr, 0, focusFromCenter.y(), ASSERT_EQUALS_DELTA);
	// Assert.assertTrue(errStr, focusFromCenter.x() < ell.getRadiusX());
	// Assert.assertEquals(
	// errStr,
	// ell.getRadiusX(),
	// GeoMath.distancePP(ell.getCenter().addNew(focusFromCenter),
	// ell.getCenter().addNew(new Vector2(0, ell.getRadiusY()))), ASSERT_EQUALS_DELTA);
	// }
	// }
	
	
	@Override
	@Test
	public void testConstructor()
	{
		IVector2 center = new Vector2(42, 42);
		double radiusX = 2 * 42;
		double radiusY = 3 * 42;
		Ellipse ell = new Ellipse(center, radiusX, radiusY);
		Assert.assertEquals(radiusX, ell.getRadiusX(), ASSERT_EQUALS_DELTA);
		Assert.assertEquals(radiusY, ell.getRadiusY(), ASSERT_EQUALS_DELTA);
		Assert.assertTrue(center.equals(ell.getCenter(), ASSERT_EQUALS_DELTA));
	}
	
	
	@Override
	@Test
	public void testGetArea()
	{
		/**
		 * this test is actually quite senseless... there is a simple formula
		 * for the calculation that we have to belief its correct
		 */
		Ellipse ell = new Ellipse(new Vector2(42, 42), 100, 100);
		Assert.assertEquals(Math.PI * 100 * 100, ell.getArea(), ASSERT_EQUALS_DELTA);
	}
	
	
	@Override
	@Test
	@Ignore
	public void testIsLineIntersectingShape()
	{
		/**
		 * void, because test is executed in testIntersecting
		 */
	}
	
	
	/**
	 * Test intersecting methods {@link Ellipse#lineIntersections(ILine)}
	 * {@link Ellipse#isLineIntersectingShape(ILine)}
	 */
	@Test
	public void testIntersecting()
	{
		for (Ellipse ell : getAllEllipses())
		{
			// line -> expected intersection points
			Map<ILine, Integer> lines = new HashMap<ILine, Integer>();
			ILine tmpLine;
			tmpLine = new Line(ell.getCenter(), new Vector2(1, 0));
			lines.put(tmpLine, 2);
			tmpLine = new Line(ell.getCenter(), new Vector2(0, 1));
			lines.put(tmpLine, 2);
			double smallerRadius = Math.min(ell.getRadiusX(), ell.getRadiusY());
			tmpLine = new Line(ell.getCenter().addNew(new Vector2(0, smallerRadius - 1)), new Vector2(1, 0));
			lines.put(tmpLine, 2);
			
			for (Map.Entry<ILine, Integer> entry : lines.entrySet())
			{
				final ILine line = entry.getKey();
				final int expIntersectionPoints = entry.getValue();
				
				final String errStr = "Failed with " + ell.toString() + " and " + line.toString();
				
				List<IVector2> points = ell.lineIntersections(line);
				Assert.assertEquals(errStr, expIntersectionPoints, points.size());
				
				final IVector2 pt1 = line.supportVector();
				final IVector2 pt2 = line.supportVector().addNew(line.directionVector());
				// ensure a long line
				final IVector2 p1 = GeoMath.stepAlongLine(pt1, pt2, -10000);
				final IVector2 p2 = GeoMath.stepAlongLine(pt2, pt1, -10000);
				// same as above, but with two points
				List<IVector2> points2 = ell.getIntersectingPoints(p1, p2);
				Assert.assertEquals(errStr, expIntersectionPoints, points2.size());
				
				for (IVector2 p : points)
				{
					double dist = GeoMath.distancePP(ell.getCenter(), p);
					Assert.assertTrue(errStr, dist < ell.getDiameterMax());
				}
				Assert.assertTrue(errStr, ell.isLineIntersectingShape(line));
			}
		}
	}
	
	
	@Override
	@Test
	public void testNearestPointOutside()
	{
		for (Ellipse ell : getAllEllipses())
		{
			IVector2 p1 = ell.getCenter().addNew(new Vector2(0, ell.getRadiusY() / 2.0).turn(ell.getTurnAngle()));
			Assert.assertTrue("Failed with " + ell + " " + p1, GeoMath.distancePP(
					ell.getCenter().addNew(new Vector2(0, ell.getRadiusY()).turn(ell.getTurnAngle())),
					ell.nearestPointOutside(p1)) < ASSERT_EQUALS_DELTA);
		}
	}
	
	
	@Override
	@Test
	public void testIsPointInShape()
	{
		for (Ellipse ell : getAllEllipses())
		{
			final String errStr = "Failed with " + ell.toString();
			
			double smallerRadius = Math.min(ell.getRadiusX(), ell.getRadiusY());
			
			Assert.assertTrue(errStr, ell.isPointInShape(ell.getCenter()));
			Assert.assertTrue(errStr, ell.isPointInShape(ell.getCenter().addNew(new Vector2(smallerRadius, 0))));
			Assert.assertTrue(errStr, ell.isPointInShape(ell.getCenter().addNew(new Vector2(0, smallerRadius))));
		}
		
	}
	
	
	/**
	 * Tests {@link Ellipse#stepOnCurve(IVector2, double)}
	 */
	@Test
	public void testStepOnCurve()
	{
		for (Ellipse ell : getAllEllipses())
		{
			Map<Double, Double> cfs = new HashMap<Double, Double>(2);
			cfs.put(ell.getCircumference(), 0.0);
			cfs.put(-ell.getCircumference(), 0.0);
			cfs.put(ell.getCircumference() / 2, (ell.getRadiusY() * 2));
			cfs.put(-ell.getCircumference() / 2, (ell.getRadiusY() * 2));
			for (Map.Entry<Double, Double> entry : cfs.entrySet())
			{
				double circumference = entry.getKey();
				IVector2 start = ell.getCenter().addNew(new Vector2(0, ell.getRadiusY()).turn(ell.getTurnAngle()));
				IVector2 end = ell.stepOnCurve(start, circumference);
				double dist = GeoMath.distancePP(start, end);
				// we are not that accurate
				final double tol = 2 + entry.getValue();
				Assert.assertTrue("Failed with " + dist + " " + start + " " + end + " " + ell, dist < tol);
			}
		}
	}
	
	
	/**
	 * Tests the correctness of the circumference ("Umfang")
	 */
	@Test
	public void testGetCircumference()
	{
		Ellipse ell;
		ell = new Ellipse(new Vector2(0, 0), 100, 100);
		Assert.assertEquals(2 * Math.PI * ell.getRadiusX(), ell.getCircumference(), ASSERT_EQUALS_DELTA);
		ell = new Ellipse(new Vector2(0, 0), 4000, 4000);
		Assert.assertEquals(2 * Math.PI * ell.getRadiusX(), ell.getCircumference(), ASSERT_EQUALS_DELTA);
		ell = new Ellipse(new Vector2(42, -42), 2000, 2000);
		Assert.assertEquals(2 * Math.PI * ell.getRadiusX(), ell.getCircumference(), ASSERT_EQUALS_DELTA);
		
		Ellipse ell1 = new Ellipse(new Vector2(42, -42), 1800, 2200);
		Ellipse ell2 = new Ellipse(new Vector2(42, -42), 2200, 1800);
		Assert.assertEquals(ell1.getCircumference(), ell2.getCircumference(), ASSERT_EQUALS_DELTA);
		
		Ellipse ell3 = new Ellipse(new Vector2(42, -42), 1800, 2200, 0);
		Ellipse ell4 = new Ellipse(new Vector2(42, -42), 2200, 1800, Math.PI / 2.0);
		Assert.assertEquals(ell3.getCircumference(), ell4.getCircumference(), ASSERT_EQUALS_DELTA);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
