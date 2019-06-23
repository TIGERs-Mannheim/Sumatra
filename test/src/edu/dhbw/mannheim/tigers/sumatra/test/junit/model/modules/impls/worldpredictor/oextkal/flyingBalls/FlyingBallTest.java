package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Coord;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.FlyingBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.LinFunc;


public class FlyingBallTest 
{

	@Test
	public void testInit() 
	{
		Coord b = new Coord(4.0, 6.0);	

		FlyingBall ball = new FlyingBall (b);
		
		double getX = Def.DUMMY;
		double getY = Def.DUMMY;
		
		getX = ball.getBottomPositionX();
		getY = ball.getBottomPositionY();
		assertEquals(b.x(), getX, Def.eps);
		assertEquals(b.y(), getY, Def.eps);
	}
	
	@Test
	public void testDistanceCalculation_Correct() 
	{
		Coord b = new Coord(4.0,5.0);	
		
		Coord start = new Coord(0.0,1.0);	
		Coord view = new Coord(4.0,3.0);


		FlyingBall ball = new FlyingBall (b);
		
		LinFunc linearFly = new LinFunc(start, view, true);
		LinFunc ballFunction = new LinFunc(new Coord(0,0), b, true);
		
		ball.setFlyPositionAndCalculateFlyingHeight(LinFunc.getCutCoords(linearFly, ballFunction), Def.t.camID);
	
		double e_distance = 2.5;
		ball.calculateDistanceToStart(start);
		
		assertEquals(e_distance, ball.getDistance(), Def.hund*e_distance);

	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testDistanceCalculation_exceptException() 
	{
		Coord b = new Coord(4.0,5.0);
		
		Coord start = new Coord(2,2);
		
		FlyingBall ball = new FlyingBall (b);

		ball.calculateDistanceToStart(start);
	}
	
	@Test
	public void testFlyingCalculation() 
	{
		for(int i = 0; i < Def.t.size; i++)
		{
			FlyingBall ball = new FlyingBall (Def.t.ballBottom[i]);
		
			LinFunc linearFly = new LinFunc(Def.t.startPos, Def.t.view, true);
			LinFunc ballFunction = new LinFunc(new Coord(Def.CamNullX, Def.CamNullY), Def.t.ballBottom[i]);

			ball.setFlyPositionAndCalculateFlyingHeight(LinFunc.getCutCoords(linearFly, ballFunction), Def.t.camID);
			double e_flyPosX = ball.getFlyingPositionX();
			double e_flyPosY= ball.getFlyingPositionY();
			double e_height = ball.getFlyingHeight();
			
			double g_flyPosX = ball.getFlyingPositionX();
			double g_flyPosY= ball.getFlyingPositionY();
			double g_height = ball.getFlyingHeight();
		
			assertEquals(e_flyPosX,  g_flyPosX,  Def.hund*e_flyPosX);
			assertEquals(e_flyPosY,  g_flyPosY,  Def.hund*e_flyPosY);
			assertEquals(e_height,   g_height,   Def.hund*e_height);
		}
	}
}
