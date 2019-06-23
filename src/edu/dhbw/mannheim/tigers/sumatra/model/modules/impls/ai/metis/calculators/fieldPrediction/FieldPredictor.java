/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldPrediction;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * calculates the position of all bots and the ball in the future<br>
 * crashes are regarded as followed:<br>
 * - ball reflects in another direction<br>
 * - bots stop at a crash
 * 
 * the class uses a
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class FieldPredictor
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private BotIDMapConst<TrackedTigerBot>	tigers;
	private BotIDMapConst<TrackedBot>		foes;
	private TrackedBall							ball;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * creates a WorldPredictionInformation, this is a pre calculation to get an estimation where the bots and the ball
	 * will be in the future
	 * @param worldFrame
	 * @return
	 */
	public WorldFramePrediction create(WorldFrame worldFrame)
	{
		tigers = worldFrame.tigerBotsVisible;
		foes = worldFrame.foeBots;
		ball = worldFrame.ball;
		WorldFramePrediction worldFramePrediction = new WorldFramePrediction();
		for (TrackedTigerBot tiger : tigers.values())
		{
			worldFramePrediction.setTigers(
					tiger.getId(),
					getFieldPredictionInfo(tiger.getPos(), tiger.getVelInMM(), tiger.getAcc(), AIConfig.getGeometry()
							.getBotRadius()));
		}
		for (TrackedBot foe : foes.values())
		{
			worldFramePrediction.setTigers(
					foe.getId(),
					getFieldPredictionInfo(foe.getPos(), foe.getVelInMM(), foe.getAcc(), AIConfig.getGeometry()
							.getBotRadius()));
		}
		worldFramePrediction.setBall(getFieldPredictionInfo(ball.getPos(), ball.getVelInMM(), ball.getAcc(), AIConfig
				.getGeometry().getBallRadius()));
		return worldFramePrediction;
	}
	
	
	private FieldPredictionInformation getFieldPredictionInfo(IVector2 pos, IVector2 vel, IVector2 acc, float rad)
	{
		return new FieldPredictionInformation(pos, vel, acc, getFirstCrashOfElement(pos, vel, rad));
	}
	
	
	/**
	 * first crash of this element
	 * @param pos
	 * @param vel
	 * @param radius
	 * @return the time when the bot will crash the first time [s]
	 */
	private float getFirstCrashOfElement(IVector2 pos, IVector2 vel, float radius)
	{
		float firstCrash = Float.MAX_VALUE;
		// tigers
		for (TrackedTigerBot tiger : tigers.values())
		{
			firstCrash = checkForEarlierCrash(pos, vel, radius, tiger.getPos(), tiger.getVelInMM(), AIConfig.getGeometry()
					.getBotRadius(), firstCrash);
		}
		// foes
		for (TrackedBot foe : foes.values())
		{
			firstCrash = checkForEarlierCrash(pos, vel, radius, foe.getPos(), foe.getVelInMM(), AIConfig.getGeometry()
					.getBotRadius(), firstCrash);
		}
		// ball
		firstCrash = checkForEarlierCrash(pos, vel, radius, ball.getPos(), ball.getVelInMM(), AIConfig.getGeometry()
				.getBallRadius(), firstCrash);
		
		return firstCrash;
	}
	
	
	private float checkForEarlierCrash(IVector2 botPos, IVector2 botVel, float botRad, IVector2 obstaclePos,
			IVector2 obstacleVel, float obstacleRad, float firstCrash)
	{
		float crash = crash(botPos, botVel, botRad, obstaclePos, obstacleVel, obstacleRad);
		if (crash < firstCrash)
		{
			return crash;
		}
		return firstCrash;
	}
	
	
	private float crash(IVector2 botPos, IVector2 botVel, float botRad, IVector2 obstaclePos, IVector2 obstacleVel,
			float obstacleRad)
	{
		if (!botPos.equals(obstaclePos, 0.01f) && !botVel.isZeroVector() && !obstacleVel.isZeroVector())
		{
			ILine botLine = new Line(botPos, botVel);
			ILine obstacleLine = new Line(obstaclePos, obstacleVel);
			IVector2 intersecitonPoint;
			try
			{
				intersecitonPoint = GeoMath.intersectionPoint(botLine, obstacleLine);
			} catch (MathException err)
			{
				intersecitonPoint = null;
			}
			if ((intersecitonPoint != null) && AIConfig.getGeometry().getField().isPointInShape(intersecitonPoint)
					&& botLine.isPointInFront(intersecitonPoint))
			{
				float timeToCrashForBot = intersecitonPoint.subtractNew(botPos).getLength2() / botVel.getLength2();
				float timeToCrashForObstacle = intersecitonPoint.subtractNew(obstaclePos).getLength2()
						/ obstacleVel.getLength2();
				float differenceOfCrashtimes = Math.abs(timeToCrashForBot - timeToCrashForObstacle);
				if (((obstacleVel.getLength2() * differenceOfCrashtimes) + (botVel.getLength2() * differenceOfCrashtimes)) < (obstacleRad + botRad))
				{
					return timeToCrashForBot;
				}
			}
		}
		return Float.MAX_VALUE;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
