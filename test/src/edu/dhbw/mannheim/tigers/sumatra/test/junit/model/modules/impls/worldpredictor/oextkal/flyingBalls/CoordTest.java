package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;


/**
 */
public class CoordTest
{
	
	double	sx		= 1;
	double	sy		= 2;
	double	px		= 4;
	double	py		= 6;
	double	qx		= 8;
	double	qy		= 9;
	double	e_dis	= 5;
	
	
	/**
	 */
	@Test
	public void testCoordDoubleDouble()
	{
		
		final Coord c = new Coord(sx, sy);
		assertEquals(sx, c.x(), Def.eps);
		assertEquals(sy, c.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testCoordCoord()
	{
		// create first coord
		final Coord sc = new Coord(sx, sy);
		assertEquals(sx, sc.x(), Def.eps);
		assertEquals(sy, sc.y(), Def.eps);
		
		// create second
		Coord pc = new Coord(px, py);
		assertEquals(px, pc.x(), Def.eps);
		assertEquals(py, pc.y(), Def.eps);
		
		// make the second like first, change the new first and the old first has to be the same as before
		pc = new Coord(sc);
		assertEquals(sx, pc.x(), Def.eps);
		assertEquals(sy, pc.y(), Def.eps);
		
		pc.set(qx, qy);
		
		assertEquals(sx, sc.x(), Def.eps);
		assertEquals(sy, sc.y(), Def.eps);
		
		assertEquals(qx, pc.x(), Def.eps);
		assertEquals(qy, pc.y(), Def.eps);
		
		
	}
	
	
	/**
	 */
	@Test
	public void testGetDistanceTo()
	{
		
		final Coord sc = new Coord(sx, sy);
		assertEquals(sx, sc.x(), Def.eps);
		assertEquals(sy, sc.y(), Def.eps);
		
		final Coord pc = new Coord(px, py);
		assertEquals(px, pc.x(), Def.eps);
		assertEquals(py, pc.y(), Def.eps);
		assertEquals(e_dis, sc.getDistanceTo(pc), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testGetLength()
	{
		
		final Coord sc = new Coord(sx, sy);
		assertEquals(sx, sc.x(), Def.eps);
		assertEquals(sy, sc.y(), Def.eps);
		
		final double e_dis = Math.sqrt((sx * sx) + (sy * sy));
		
		assertEquals(e_dis, sc.getLength(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void getVectorTo()
	{
		final Coord vec1 = new Coord(1, 2);
		final Coord vec2 = new Coord(-1, 3);
		final Coord e_vec = new Coord(-2, 1);
		
		final Coord vec = vec1.getVectorTo(vec2);
		assertEquals(e_vec.x(), vec.x(), Def.eps);
		assertEquals(e_vec.y(), vec.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testSet()
	{
		
		final Coord c = new Coord(sx, sy);
		assertEquals(sx, c.x(), Def.eps);
		assertEquals(sy, c.y(), Def.eps);
		
		c.set(px, py);
		assertEquals(px, c.x(), Def.eps);
		assertEquals(py, c.y(), Def.eps);
		
	}
	
	
	/**
	 */
	@Test
	public void testSetX()
	{
		final Coord c = new Coord(sx, sy);
		assertEquals(sx, c.x(), Def.eps);
		assertEquals(sy, c.y(), Def.eps);
		
		c.setX(px);
		assertEquals(px, c.x(), Def.eps);
		assertEquals(sy, c.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testSetY()
	{
		final Coord c = new Coord(sx, sy);
		assertEquals(sx, c.x(), Def.eps);
		assertEquals(sy, c.y(), Def.eps);
		
		c.setY(py);
		assertEquals(sx, c.x(), Def.eps);
		assertEquals(py, c.y(), Def.eps);
	}
	
}
