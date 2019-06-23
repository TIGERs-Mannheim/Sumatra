/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 19, 2011
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;

/**
 * Test Ball Corrector with second bot in ball-view
 * 
 * @author Birgit
 * 
 */
public class BallCorrector_1_OtherBotOnView
{
	
	private int collWithOtherBot = 3;
	
	
	//second bot has orientation like first bot
	@Test
	public void testSameOri()
	{
		//System.out.println("testSameOri");
		BallCorrector corr = new BallCorrector();
		
		for(int i = 0; i < Def.t.size; i++)
		{
			//System.out.print(i+":");
			CamDetectionFrame frame = createFrameSameOri(i);
			
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
			
			//detect one more fly
			//System.out.println(corr.m_a.toString());
			if(collWithOtherBot <= i)
			{
				System.out.println("Erwarte 2"+corr.m_a.m_flys.size());
				assertTrue(corr.m_a.m_flys.size() == 2);
			}
			else
			{
				System.out.println("Erwarte 1"+corr.m_a.m_flys.size());
				assertTrue(corr.m_a.m_flys.size() == 1);
			}
			//System.out.print("\n");
		}
		
		
	}
	
	private CamDetectionFrame createFrameSameOri(int i)
	{
		CamBall ball = new CamBall(0,0, (float) Def.t.ballBottom[i].x(), (float) Def.t.ballBottom[i].y(), 0, 0, 0);
		List<CamBall> balls = new ArrayList<CamBall>();
		balls.add(ball);
		
		List<CamRobot> tigers = new ArrayList<CamRobot>();
		List<CamRobot> foods = new ArrayList<CamRobot>();
		
		for(int botId = 0; botId < Def.t.botsNumber; botId++)
		{
			CamRobot bot = new CamRobot(0, 0, 
					(float) Def.t.bots[0][botId][0],
					(float) Def.t.bots[0][botId][1], 
					(float) Def.t.bots[0][botId][2], 0, 0, 0);
			
			//third bot is on view of first bot on ballPosition
			if( collWithOtherBot == i && botId == 0)
			{
				bot = new CamRobot(0,0,
						(float) (Def.t.ballBottom[i-1].x()+0.8*(Def.t.ballBottom[i].x()-Def.t.ballBottom[i-1].x())),
						(float) (Def.t.ballBottom[i-1].y()+0.8*(Def.t.ballBottom[i].y()-Def.t.ballBottom[i-1].y())),
						(float) Def.t.bots[0][botId][2], 0, 0, 0);
						
			}
		
			if(botId%2 == 0)
				tigers.add(bot);
			else
				foods.add(bot);
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
	
	//second bot has orientation like first bot
	@Test
	public void testOtherOri()
	{
		//System.out.println("testOtherOri");
		BallCorrector corr = new BallCorrector();
		
		for(int i = 0; i < Def.t.size; i++)
		{
			//System.out.println("Durchgang: "+i);
			CamDetectionFrame frame = createFrameOtherOri(i);
			
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
			
			//System.out.println(corr.m_a.toString());
			if(collWithOtherBot == i)
			{
				assertTrue(corr.m_a.m_flys.size() == 2);
			}
			else
			{
				assertTrue(corr.m_a.m_flys.size() == 1);
			}
			//System.out.print("\n");
		}
		
		
	}
	
	private CamDetectionFrame createFrameOtherOri(int i)
	{
		CamBall ball = new CamBall(0,0, (float) Def.t.ballBottom[i].x(), (float) Def.t.ballBottom[i].y(), 0, 0, 0);
		List<CamBall> balls = new ArrayList<CamBall>();
		balls.add(ball);
		
		List<CamRobot> tigers = new ArrayList<CamRobot>();
		List<CamRobot> foods = new ArrayList<CamRobot>();
		
		for(int botId = 0; botId < Def.t.botsNumber; botId++)
		{
			float ang = (float) Def.t.bots[0][botId][2];
			CamRobot bot = new CamRobot(0, 0, 
					(float) Def.t.bots[0][botId][0],
					(float) Def.t.bots[0][botId][1], 
					ang, 0, 0, 0);
			
			//third bot is on view of first bot on ballPosition
			if( collWithOtherBot == i && botId == 0 )
			{
				ang = (float) (Def.t.bots[0][botId][2]-Math.PI);
				bot = new CamRobot(0,0,
						(float) (Def.t.ballBottom[i].x()-1.5*Math.cos(ang)*Def.BOT_RADIUS),
						(float) (Def.t.ballBottom[i].y()-1.5*Math.sin(ang)*Def.BOT_RADIUS),
						ang, 0, 0, 0);
						
				
			}
			if(botId==0)
			{
			//System.out.println("KickPos: "+(bot.pos.x()+Math.cos(ang)*Def.BOT_CENTER_TO_KICKER_DISTANCE)+" / "+(bot.pos.y()+Math.sin(ang)*+Def.BOT_CENTER_TO_KICKER_DISTANCE));
			//System.out.println("Ball:    "+ball.pos.x()+" / "+ball.pos.y());
			//System.out.println("View:    "+ang);
			}
			if(botId%2 == 0)
				tigers.add(bot);
			else
				foods.add(bot);
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