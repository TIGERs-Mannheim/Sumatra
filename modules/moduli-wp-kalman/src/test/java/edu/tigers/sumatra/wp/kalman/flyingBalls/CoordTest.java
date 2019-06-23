package edu.tigers.sumatra.wp.kalman.flyingBalls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 */
public class CoordTest
{
	
	private static final double	eps	= 12e-8f;
	
	double								sx		= 1;
	double								sy		= 2;
	double								px		= 4;
	double								py		= 6;
	double								qx		= 8;
	double								qy		= 9;
	double								e_dis	= 5;
	
	
	/**
	 */
	@Test
	public void testIVector2DoubleDouble()
	{
		
		final IVector2 c = new Vector2(sx, sy);
		assertEquals(sx, c.x(), eps);
		assertEquals(sy, c.y(), eps);
	}
	
	
	/**
	 */
	@Test
	public void testIVector2IVector2()
	{
		// create first IVector2
		final IVector2 sc = new Vector2(sx, sy);
		assertEquals(sx, sc.x(), eps);
		assertEquals(sy, sc.y(), eps);
		
		// create second
		Vector2 pc = new Vector2(px, py);
		assertEquals(px, pc.x(), eps);
		assertEquals(py, pc.y(), eps);
		
		// make the second like first, change the new first and the old first has to be the same as before
		pc = new Vector2(sc);
		assertEquals(sx, pc.x(), eps);
		assertEquals(sy, pc.y(), eps);
		
		pc.setX(qx);
		pc.setY(qy);
		
		assertEquals(sx, sc.x(), eps);
		assertEquals(sy, sc.y(), eps);
		
		assertEquals(qx, pc.x(), eps);
		assertEquals(qy, pc.y(), eps);
		
		
	}
	
	
	/**
	 */
	@Test
	public void testGetDistanceTo()
	{
		
		final IVector2 sc = new Vector2(sx, sy);
		assertEquals(sx, sc.x(), eps);
		assertEquals(sy, sc.y(), eps);
		
		final IVector2 pc = new Vector2(px, py);
		assertEquals(px, pc.x(), eps);
		assertEquals(py, pc.y(), eps);
		assertEquals(e_dis, GeoMath.distancePP(sc, pc), eps);
	}
	
	
	/**
	 */
	@Test
	public void testGetLength()
	{
		
		final IVector2 sc = new Vector2(sx, sy);
		assertEquals(sx, sc.x(), eps);
		assertEquals(sy, sc.y(), eps);
		
		final double e_dis = Math.sqrt((sx * sx) + (sy * sy));
		
		assertEquals(e_dis, sc.getLength2(), eps);
	}
	
	
	/**
	 */
	@Test
	public void getVectorTo()
	{
		final IVector2 vec1 = new Vector2(1, 2);
		final IVector2 vec2 = new Vector2(-1, 3);
		final IVector2 e_vec = new Vector2(-2, 1);
		
		final IVector2 vec = vec2.subtractNew(vec1);
		assertEquals(e_vec.x(), vec.x(), eps);
		assertEquals(e_vec.y(), vec.y(), eps);
	}
	
	
	/**
	 */
	@Test
	public void testSet()
	{
		
		final Vector2 c = new Vector2(sx, sy);
		assertEquals(sx, c.x(), eps);
		assertEquals(sy, c.y(), eps);
		
		c.setX(px);
		c.setY(py);
		assertEquals(px, c.x(), eps);
		assertEquals(py, c.y(), eps);
		
	}
	
	
	/**
	 */
	@Test
	public void testSetX()
	{
		final Vector2 c = new Vector2(sx, sy);
		assertEquals(sx, c.x(), eps);
		assertEquals(sy, c.y(), eps);
		
		c.setX(px);
		assertEquals(px, c.x(), eps);
		assertEquals(sy, c.y(), eps);
	}
	
	
	/**
	 */
	@Test
	public void testSetY()
	{
		final Vector2 c = new Vector2(sx, sy);
		assertEquals(sx, c.x(), eps);
		assertEquals(sy, c.y(), eps);
		
		c.setY(py);
		assertEquals(sx, c.x(), eps);
		assertEquals(py, c.y(), eps);
	}
	
}
