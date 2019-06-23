package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.PredBall;


/**
 */
public class PredBallTest
{
	
	double	x	= 1.0;
	double	y	= 2.0;
	double	z	= 3.0;
	
	
	/**
	 */
	@Test
	public void testSetX()
	{
		final PredBall b = new PredBall();
		b.setX(x);
		
		assertEquals(x, b.x(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testSetY()
	{
		final PredBall b = new PredBall();
		b.setY(y);
		
		assertEquals(y, b.y(), Def.eps);
	}
	
	
	/**
	 */
	@Test
	public void testSetZ()
	{
		final PredBall b = new PredBall();
		b.setZ(z);
		
		assertEquals(z, b.z(), Def.eps);
	}
}