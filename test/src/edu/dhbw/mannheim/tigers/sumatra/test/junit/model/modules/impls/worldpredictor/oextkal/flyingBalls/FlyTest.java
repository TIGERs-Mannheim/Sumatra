package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Fly;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.RegParab;



public class FlyTest {
	
	@Test
	public void testFly() {
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		assertEquals(Def.t.startPos.x(), f.getStartPos().x(), Math.abs(Def.hund*Def.t.startPos.x()));
		assertEquals(Def.t.startPos.y(), f.getStartPos().y(), Math.abs(Def.hund*Def.t.startPos.y()));
	}

	@Test
	public void testAddNewBall() {
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		
		for(int i = 0; i < Def.t.size; i++)
		{	
			assertTrue(f.addNewBall(Def.t.ballBottom[i], Def.t.camID));
		}

		//something wrong
		assertFalse(f.addNewBall(new Coord(-123.456,-123.456), Def.t.camID));
	}
	
	
	@Test
	public void getNumberOfBalls_0()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		int number = 0;
		
		for(int i = 0; i < number; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
		}
		
		assertEquals(number, f.size());
	}
	@Test
	public void getNumberOfBalls_3()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		int number = 3;
		
		for(int i = 0; i < number; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
		}
		
		assertEquals(number, f.size());
	}
	
	@Test
	public void getNumberOfBalls_4()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		int number = 4;
		
		for(int i = 0; i < number; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
		}
		
		assertEquals(number, f.size());
	}
	
	
	@Test
	public void testParabelCalculation()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		
		for(int i = 0; i < Def.t.size; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
		}
		f.calculateFly();
		
		RegParab parabel = f.getParabel();
		
		assertEquals(Def.t.a, parabel.getA(), Math.abs(Def.t.a*Def.hund));
		assertEquals(Def.t.b, parabel.getB(), Math.abs(Def.t.b*Def.hund));
		assertEquals(Def.t.c, parabel.getC(), 10);
		assertEquals(Def.t.d, parabel.getD(), Math.abs(Def.t.d*Def.hund));
	   assertEquals(Def.t.e, parabel.getE(), Math.abs(Def.t.e*Def.hund));
	   assertEquals(Def.t.alpha, parabel.getAlpha(), Math.abs(Def.t.alpha*Def.hund));	
	}


	@Test
	public void getBallPosition()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		
		for(int i = 0; i < Def.t.size; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
			

			if(i > 3)
			{
			f.calculateFly();
			
			assertEquals(Def.t.ballFly[i].x(), f.getCurrentBallPosition().x(), Math.abs(Def.t.ballFly[i].x()*Def.hund));
			assertEquals(Def.t.ballFly[i].y(), f.getCurrentBallPosition().y(), Math.abs(Def.t.ballFly[i].y()*Def.hund));
			}			
		}
	}
	
	@Test
	public void getBallHight()
	{
		Fly f = new Fly(Def.t.roboPos, Def.t.viewAngle);
		
		for(int i = 0; i < Def.t.size; i++)
		{
			f.addNewBall(Def.t.ballBottom[i], Def.t.camID);
			
			if(i > 3)
			{
			f.calculateFly();
			
			double e_height = Def.t.height[i];
			double height = f.getCurrentBallHeight();
			//System.out.println("height: "+e_height +" : "+height+"      "+e_height/height);
			assertEquals(e_height, height, Math.abs(e_height*Def.hund*3));
			}
		}
		
		
//		
//		for(int i = 0; i < Def.t.size; i++)
//		{
//			double e_height = Def.t.height[i];
//			double height = f.getBallHight(Def.t.timeExtern[i]);
//			//System.out.println("height: "+e_height +" : "+height);
//			assertEquals(e_height, height, Math.abs(Def.t.height[i]/5));
//		}
	}


}
