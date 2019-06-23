package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.LinFunc;


/**
 */
public class LinearFunctionTest
{
	
	/**
	 */
	@Test
	public void testF()
	{
		final Coord point = new Coord(1, 3);
		final Coord view = new Coord(4, -2);
		// f(x) = -0.5x+3.5;
		
		final LinFunc func = new LinFunc(point, view, true);
		
		assertEquals(4, func.f(-1), Def.eps);
		assertEquals(3, func.f(1), Def.eps);
		assertEquals(2, func.f(3), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testFWithAngle()
	{
		final Coord point = new Coord(1, 3);
		final double viewAngle = -0.463;
		// f(x) = -0.5x+3.5;
		
		final LinFunc func = new LinFunc(point, viewAngle);
		
		assertEquals(4, func.f(-1), Def.HUND * 0.5);
		assertEquals(3, func.f(1), Def.HUND * 0.5);
		assertEquals(2, func.f(3), Def.HUND * 0.5);
	}
	
	
	/**
	 */
	@Test
	public void testGetXfromCut()
	{
		final Coord f1_Point = new Coord(1, 3);
		final Coord f1_View = new Coord(4, -2);
		// f1(x) = -0.5x+3.5;
		
		final Coord f2_Point = new Coord(4, 5);
		final Coord f2_View = new Coord(-1, -3);
		// f1(x) = 3x-7;
		
		final LinFunc func1 = new LinFunc(f1_Point, f1_View, true);
		final LinFunc func2 = new LinFunc(f2_Point, f2_View, true);
		
		final Coord g_CutPoint = LinFunc.getCutCoords(func1, func2);
		final Coord e_CutPoint = new Coord(3, 2);
		
		assertEquals(e_CutPoint.x(), g_CutPoint.x(), Def.eps);
		assertEquals(e_CutPoint.y(), g_CutPoint.y(), Def.eps);
		
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCutWithoutCut()
	{
		final Coord f1_Point = new Coord(0, 0);
		// in no point
		final Coord f2_Point = new Coord(1, 0);
		final Coord f_View = new Coord(4, -2);
		// f1(x) = -0.5x+3.5;
		
		final LinFunc func1 = new LinFunc(f1_Point, f_View, true);
		final LinFunc func2 = new LinFunc(f2_Point, f_View, true);
		
		LinFunc.getCutCoords(func1, func2);
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCutWithoutInfinityCut()
	{
		final Coord f1_Point = new Coord(0, 0);
		// in all points
		final Coord f3_Point = new Coord(1, 1);
		final Coord f_View = new Coord(4, -2);
		// f1(x) = -0.5x+3.5;
		
		final LinFunc func1 = new LinFunc(f1_Point, f_View, true);
		final LinFunc func3 = new LinFunc(f3_Point, f_View, true);
		
		LinFunc.getCutCoords(func1, func3);
	}
	
	
	/**
	 */
	@Test
	public void goDistanceFromPointTestWithViewNormal()
	{
		
		final LinFunc func = new LinFunc(new Coord(1, 2), new Coord(3, 4), true);
		final Coord newPos = func.goDistanceFromPoint(new Coord(1, 2), 5);
		
		assertEquals(4, newPos.x(), Def.eps);
		assertEquals(6, newPos.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void goDistanceFromPointTestWithCoordRegular()
	{
		
		final LinFunc func = new LinFunc(new Coord(1, 2), new Coord(4, 6));
		final Coord newPos = func.goDistanceFromPoint(new Coord(1, 2), 5);
		
		assertEquals(4, newPos.x(), Def.eps);
		assertEquals(6, newPos.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testCreateWithAngleRegular()
	{
		final double alpha = Math.atan(4.0 / 3.0);
		final LinFunc func = new LinFunc(new Coord(1, 2), alpha);
		final Coord newPos = func.goDistanceFromPoint(new Coord(1, 2), 5);
		
		assertEquals(4, newPos.x(), Def.eps);
		assertEquals(6, newPos.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testCreateWithAngleUnRegular()
	{
		final double alpha = Math.PI - Math.atan(4.0 / 3.0);
		final LinFunc func = new LinFunc(new Coord(1, 2), alpha);
		final Coord newPos = func.goDistanceFromPoint(new Coord(1, 2), 5);
		
		assertEquals(-2, newPos.x(), Def.eps);
		assertEquals(6, newPos.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void goDistanceFromPointTestWithCoordUnRegular()
	{
		
		final LinFunc func = new LinFunc(new Coord(4, 6), new Coord(1, 2));
		final Coord newPos = func.goDistanceFromPoint(new Coord(1, 2), 5);
		
		assertEquals(-2, newPos.x(), Def.eps);
		assertEquals(-2, newPos.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void getAngleFromVector()
	{
		final double n = -2;
		final Coord view = new Coord(1, -1);
		
		final LinFunc func = new LinFunc(new Coord(0, n), view, true);
		
		final double quart = Math.PI / 4.0;
		
		final Coord[] vec = new Coord[8];
		final double[] e_angle = new double[8];
		
		vec[0] = new Coord(0, 1);
		e_angle[0] = quart * 3;
		
		vec[1] = new Coord(1, 1);
		e_angle[1] = quart * 2;
		
		vec[2] = new Coord(1, 0);
		e_angle[2] = quart;
		
		vec[3] = new Coord(1, -1);
		e_angle[3] = 0;
		
		vec[4] = new Coord(0, -1);
		e_angle[4] = quart;
		
		vec[5] = new Coord(-1, -1);
		e_angle[5] = quart * 2;
		
		vec[6] = new Coord(-1, 0);
		e_angle[6] = quart * 3;
		
		vec[7] = new Coord(-1, 1);
		e_angle[7] = quart * 4;
		
		for (int i = 0; i < 8; i++)
		{
			assertEquals(e_angle[i], func.getAngleToVector(vec[i]), Def.HUND * 0.5);
		}
		
	}
}
