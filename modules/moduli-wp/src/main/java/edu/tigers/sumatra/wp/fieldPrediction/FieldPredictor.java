/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.fieldPrediction;

import java.util.Collection;

import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;


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
	private final Collection<ITrackedBot>	bots;
	private final TrackedBall					ball;
														
														
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param bots
	 * @param ball
	 */
	public FieldPredictor(final Collection<ITrackedBot> bots, final TrackedBall ball)
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
		for (ITrackedBot bot : bots)
		{
			FieldPredictionInformation fpi = getFieldPredictionInfo(bot.getPos(), bot.getVel().multiplyNew(1000), Geometry
					.getBotRadius());
			botFpis.put(bot.getBotId(), fpi);
		}
		FieldPredictionInformation fpiBall = getFieldPredictionInfo(ball.getPos(), ball.getVel().multiplyNew(1000),
				Geometry.getBallRadius());
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
		for (ITrackedBot bot : bots)
		{
			FieldPredictionInformation fpi = new FieldPredictionInformation(bot.getPos(), bot.getVel().multiplyNew(1000),
					Double.MAX_VALUE);
			botFpis.put(bot.getBotId(), fpi);
		}
		FieldPredictionInformation fpiBall = new FieldPredictionInformation(ball.getPos(), ball.getVel()
				.multiplyNew(1000),
				Double.MAX_VALUE);
		return new WorldFramePrediction(botFpis, fpiBall);
	}
	
	
	private FieldPredictionInformation getFieldPredictionInfo(final IVector2 pos, final IVector2 vel, final double rad)
	{
		double crash = getFirstCrashOfElement(pos, vel, rad);
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
	private double getFirstCrashOfElement(final IVector2 pos, final IVector2 vel, final double radius)
	{
		double firstCrash = Double.MAX_VALUE;
		for (ITrackedBot tiger : bots)
		{
			firstCrash = checkForEarlierCrash(pos, vel, radius, tiger.getPos(), tiger.getVel().multiplyNew(1000), Geometry
					.getBotRadius(), firstCrash);
		}
		// ball
		firstCrash = checkForEarlierCrash(pos, vel, radius, ball.getPos(), ball.getVel().multiplyNew(1000), Geometry
				.getBallRadius(), firstCrash);
				
		return firstCrash;
	}
	
	
	private double checkForEarlierCrash(final IVector2 botPos, final IVector2 botVel, final double botRad,
			final IVector2 obstaclePos,
			final IVector2 obstacleVel, final double obstacleRad, final double firstCrash)
	{
		double crash = crash(botPos, botVel, botRad, obstaclePos, obstacleVel, obstacleRad);
		if (crash < firstCrash)
		{
			return crash;
		}
		return firstCrash;
	}
	
	
	private double crash(final IVector2 botPos, final IVector2 botVel, final double botRad, final IVector2 obstaclePos,
			final IVector2 obstacleVel,
			final double obstacleRad)
	{
		if (!botPos.equals(obstaclePos, 0.01) && !botVel.isZeroVector() && !obstacleVel.isZeroVector())
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
			if ((intersecitonPoint != null) && Geometry.getField().isPointInShape(intersecitonPoint)
					&& botLine.isPointInFront(intersecitonPoint))
			{
				double timeToCrashForBot = intersecitonPoint.subtractNew(botPos).getLength2() / botVel.getLength2();
				double timeToCrashForObstacle = intersecitonPoint.subtractNew(obstaclePos).getLength2()
						/ obstacleVel.getLength2();
				double differenceOfCrashtimes = Math.abs(timeToCrashForBot - timeToCrashForObstacle);
				if (((obstacleVel.getLength2() * differenceOfCrashtimes)
						+ (botVel.getLength2() * differenceOfCrashtimes)) < (obstacleRad + botRad))
				{
					return timeToCrashForBot;
				}
			}
		}
		return Double.MAX_VALUE;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
