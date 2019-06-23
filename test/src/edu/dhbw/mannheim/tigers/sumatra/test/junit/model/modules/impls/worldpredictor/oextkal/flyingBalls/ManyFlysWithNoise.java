/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2011
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;


/**
 * TODO Birgit, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Birgit
 * 
 */
public class ManyFlysWithNoise
{
	private int FbefFly = 100;
	private int framesToFly = FbefFly +4;
	private int framesAfterFly = FbefFly+Def.t.size;
	private int runs = 120;
	
	@Test
	public void test()
	{
		//System.out.println("test");
		BallCorrector corr = new BallCorrector();
		
		for(int i = 0; i < runs ; i++)
		{
			if(Def.debug)
			System.out.print(i+":");
			CamDetectionFrame OriFrame = createFrameSameOri(i);
			
			CamDetectionFrame NewFrame = corr.correctFrame(OriFrame);
			
			if(Def.debug)
			System.out.print("Frame erstellt ");
			//ball is flying
			if ( i >= framesToFly &&  i < (framesAfterFly))
			{
				//System.out.println("Test: Ball should fly");
				assertEquals(Def.t.ballFly[i-FbefFly].x(), ((double) NewFrame.balls.get(0).pos.x),  Def.hund*Def.t.ballFly[i-FbefFly].x());
				assertEquals(Def.t.ballFly[i-FbefFly].y(), ((double) NewFrame.balls.get(0).pos.y),	 Def.hund*Def.t.ballFly[i-FbefFly].y());
				assertEquals(Def.t.height[i-FbefFly]     , ((double) NewFrame.balls.get(0).pos.z),	 3*Def.hund*Def.t.height[i-FbefFly]);
				
			}
			//ball is not flying
			else
			{
				//System.out.println("Test: Ball should bottom");
				assertEquals(OriFrame.balls.get(0).pos.x, ((double) NewFrame.balls.get(0).pos.x),  Def.eps);
				assertEquals(OriFrame.balls.get(0).pos.y, ((double) NewFrame.balls.get(0).pos.y),  Def.eps);
				assertEquals(0                    		 , ((double) NewFrame.balls.get(0).pos.z),  Def.eps);
			}                            
		}		
	}
	
	private CamDetectionFrame createFrameSameOri(int i)
	{
		List<CamBall> balls = new ArrayList<CamBall>();
		
		
		List<CamRobot> tigers = new ArrayList<CamRobot>();
		List<CamRobot> foods = new ArrayList<CamRobot>();
		
		//dummy-values
		if( i < FbefFly || i >= (FbefFly+Def.t.size))
		{
			//balls
			CamBall ball = new CamBall(0,0, (float) Def.t.ballBottom[i%Def.t.size].x(), (float) Def.t.ballBottom[i%Def.t.size].y(), 0, 0, 0);
			balls.add(ball);
			
			//bots
			CamRobot bot = new CamRobot(0, 0, 
					(float) Math.random()*5000,
				   (float) Math.random()*5000, 
				   (float)(Math.PI - Math.random()*2*Math.PI), 0, 0, 0);
			
			tigers.add(bot);
			
		}
		else
		{
			//balls
			CamBall ball = new CamBall(0,0, (float) Def.t.ballBottom[i-FbefFly].x()+1, (float) Def.t.ballBottom[i-FbefFly].y()+1, 0, 0, 0);
			balls.add(ball);
			
			for(int botId = 0; botId < Def.t.botsNumber; botId++)
			{
				CamRobot bot;
				
				//third bot is on view of first bot on ballPosition
				if(botId == 0)
				{
					bot = new CamRobot(0,0,
							(float) (Def.t.ballBottom[i-FbefFly].x()+0.8*(Def.t.ballBottom[i-FbefFly].x()-Def.t.ballBottom[i-FbefFly].x())),
							(float) (Def.t.ballBottom[i-FbefFly].y()+0.8*(Def.t.ballBottom[i-FbefFly].y()-Def.t.ballBottom[i-FbefFly].y())),
							(float) Def.t.bots[0][botId][2], 0, 0, 0);
							
				}
				else
				{
					bot = new CamRobot(0, 0, 
							(float) Math.random()*5000,
						   (float) Math.random()*5000, 
						   (float)(Math.PI - Math.random()*2*Math.PI), 0, 0, 0);
				}
			
				if(botId%2 == 0)
					tigers.add(bot);
				else
					foods.add(bot);
			}
		}
			
		

		CamDetectionFrame frame = new CamDetectionFrame(0, 0,	0,	Def.t.camID, 0,	0,	balls, tigers,	foods);
		
		/*
		System.out.println("################ Frame ######################");
		System.out.println("Ball:      "+frame.balls.get(0).pos.x+"/"+frame.balls.get(0).pos.y);
		
		for (CamRobot bot : frame.robotsTigers)
			System.out.println("BotT :      "+bot.pos.x+"/"+bot.pos.y);
		
		for (CamRobot bot : frame.robotsEnemies)
			System.out.println("BotE :      "+bot.pos.x+"/"+bot.pos.y);
		*/
		return frame;
	}
	
}
