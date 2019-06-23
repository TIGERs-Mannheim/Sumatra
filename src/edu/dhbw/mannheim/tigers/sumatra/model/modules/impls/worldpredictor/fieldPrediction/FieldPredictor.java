/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction;

import java.util.Collection;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * calculates the position of all bots and the ball in the future<br>
 * crashes are regarded as followed:<br>
 * - ball reflects in another direction<br>
 * - bots stop at a crash
 * the class uses a
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public final class FieldPredictor
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Collection<TrackedTigerBot>	bots;
	private final TrackedBall						ball;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param bots
	 * @param ball
	 */
	public FieldPredictor(final Collection<TrackedTigerBot> bots, final TrackedBall ball)
	{
		this.bots = bots;
		this.ball = ball;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * creates a WorldPredictionInformation, this is a pre calculation to get an estimation where the bots and the ball
	 * will be in the future
	 * 
	 * @return
	 */
	public WorldFramePrediction create()
	{
		IBotIDMap<FieldPredictionInformation> botFpis = new BotIDMap<FieldPredictionInformation>();
		for (TrackedTigerBot bot : bots)
		{
			FieldPredictionInformation fpi = getFieldPredictionInfo(bot.getPos(), bot.getVelInMM(), AIConfig.getGeometry()
					.getBotRadius());
			botFpis.put(bot.getId(), fpi);
		}
		FieldPredictionInformation fpiBall = getFieldPredictionInfo(ball.getPos(), ball.getVelInMM(), AIConfig
				.getGeometry().getBallRadius());
		return new WorldFramePrediction(botFpis, fpiBall);
	}
	
	
	/**
	 * Create a stub wfp without crash information for fast calculation (testing purpose)
	 * 
	 * @return
	 */
	public WorldFramePrediction createStub()
	{
		IBotIDMap<FieldPredictionInformation> botFpis = new BotIDMap<FieldPredictionInformation>();
		for (TrackedTigerBot bot : bots)
		{
			FieldPredictionInformation fpi = new FieldPredictionInformation(bot.getPos(), bot.getVelInMM(),
					Float.MAX_VALUE);
			botFpis.put(bot.getId(), fpi);
		}
		FieldPredictionInformation fpiBall = new FieldPredictionInformation(ball.getPos(), ball.getVelInMM(),
				Float.MAX_VALUE);
		return new WorldFramePrediction(botFpis, fpiBall);
	}
	
	
	private FieldPredictionInformation getFieldPredictionInfo(final IVector2 pos, final IVector2 vel, final float rad)
	{
		float crash = getFirstCrashOfElement(pos, vel, rad);
		return new FieldPredictionInformation(pos, vel, crash);
	}
	
	
	/**
	 * first crash of this element
	 * 
	 * @param pos
	 * @param vel
	 * @param radius
	 * @return the time when the bot will crash the first time [s]
	 */
	private float getFirstCrashOfElement(final IVector2 pos, final IVector2 vel, final float radius)
	{
		float firstCrash = Float.MAX_VALUE;
		for (TrackedTigerBot tiger : bots)
		{
			firstCrash = checkForEarlierCrash(pos, vel, radius, tiger.getPos(), tiger.getVelInMM(), AIConfig.getGeometry()
					.getBotRadius(), firstCrash);
		}
		// ball
		firstCrash = checkForEarlierCrash(pos, vel, radius, ball.getPos(), ball.getVelInMM(), AIConfig.getGeometry()
				.getBallRadius(), firstCrash);
		
		return firstCrash;
	}
	
	
	private float checkForEarlierCrash(final IVector2 botPos, final IVector2 botVel, final float botRad,
			final IVector2 obstaclePos,
			final IVector2 obstacleVel, final float obstacleRad, final float firstCrash)
	{
		float crash = crash(botPos, botVel, botRad, obstaclePos, obstacleVel, obstacleRad);
		if (crash < firstCrash)
		{
			return crash;
		}
		return firstCrash;
	}
	
	
	private float crash(final IVector2 botPos, final IVector2 botVel, final float botRad, final IVector2 obstaclePos,
			final IVector2 obstacleVel,
			final float obstacleRad)
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
