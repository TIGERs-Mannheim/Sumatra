/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2011
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;

/**
 * Test the correct function of BallCorrector
 * 
 * @author Birgit
 * 
 */
public class BallCorrector_0_OptimalTest
{
	
	@Test
	public void test()
	{
		
		BallCorrector corr = new BallCorrector();
		
		for(int i = 0; i < Def.t.size; i++)
		{
			CamDetectionFrame frame = createFrame(i);
			
			frame = corr.correctFrame(frame);
			
			//ball is flying
			if ( i >= 3)
			{
				//System.out.println("Test: Ball should fly");
				assertEquals(Def.t.ballFly[i].x(), ((double) frame.balls.get(0).pos.x),  Def.hund*Def.t.ballFly[i].x());
				assertEquals(Def.t.ballFly[i].y(), ((double) frame.balls.get(0).pos.y),	 Def.hund*Def.t.ballFly[i].y());
				assertEquals(Def.t.height[i]     , ((double) frame.balls.get(0).pos.z),	 3*Def.hund*Def.t.height[i]);
				
			}
			//ball is not flying
			else
			{
				//System.out.println("Test: Ball should bottom");
				assertEquals(Def.t.ballBottom[i].x(), ((double) frame.balls.get(0).pos.x),  Math.abs(Def.hund*Def.t.ballBottom[i].x()));
				assertEquals(Def.t.ballBottom[i].y(), ((double) frame.balls.get(0).pos.y),	 Math.abs(Def.hund*Def.t.ballBottom[i].y()));
				assertEquals(0                      , ((double) frame.balls.get(0).pos.z),	 Math.abs(Def.hund));
			}
		}
	}
	
	private CamDetectionFrame createFrame(int i)
	{
		CamBall ball = new CamBall(0,0, (float) Def.t.ballBottom[i].x(), (float) Def.t.ballBottom[i].y(), 0, 0, 0);
		List<CamBall> balls = new ArrayList<CamBall>();
		balls.add(ball);
		
		List<CamRobot> tigers = new ArrayList<CamRobot>();
		List<CamRobot> foods = new ArrayList<CamRobot>();
		
		for(int j = 0; j < Def.t.botsNumber; j++)
		{
			CamRobot bot = new CamRobot(0, 0, 
					(float) Def.t.bots[0][j][0],
					(float) Def.t.bots[0][j][1], 
					(float) Def.t.bots[0][j][2], 0, 0, 0);
		
			if(j%2 == 0)
			{
				tigers.add(bot);
			}
			else
			{
				foods.add(bot);
			}
		}
		
	
		
		CamDetectionFrame frame = new CamDetectionFrame(
				//0, 0, 0, 1, 0, 0, balls, tigers, foods);
				0, 0, 0, Def.t.camID, 0, 0, balls, tigers, foods);
		
		/*
		System.out.println("################ Frame ######################");
		System.out.println("Ball:      "+frame.balls.get(0).pos.x+"/"+frame.balls.get(0).pos.y);
		
		
		for (CamRobot bot : frame.robotsTigers)
		{
			System.out.println("BotT :      "+bot.pos.x+"/"+bot.pos.y);
		}
		
		for (CamRobot bot : frame.robotsEnemies)
		{
			System.out.println("BotE :      "+bot.pos.x+"/"+bot.pos.y);
		}
		*/
		return frame;
	}
}
