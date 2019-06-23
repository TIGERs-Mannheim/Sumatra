/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 15, 2011
 * Author(s): Birgit
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.RealTimeClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.StaticSimulationClock;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBall;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.RawBot;


/**
 * This Class is to correct the ssl-ballPosition from bottom to fly
 * 
 * @author Birgit
 */
public class BallCorrector
{
	@SuppressWarnings("unused")
	private static final Logger	log						= Logger.getLogger(BallCorrector.class.getName());
	
	@Configurable(comment = "correct flying balls")
	private static boolean			correctFlyingBalls	= false;
	private static final float		MAX_DIST_BALL			= 50;
	private static final float		MAX_ORIENTATION_DIFF	= 0.1f;
	
	private Altigraph					altigraph;
	
	private CamBall					lastSeenBall			= CamBall.defaultInstance();
	
	
	/**
	 */
	public BallCorrector()
	{
		altigraph = new Altigraph();
	}
	
	
	/**
	 * correct the ball frame, if ball is flying
	 * 
	 * @param balls
	 * @param yellowBots
	 * @param blueBots
	 * @return
	 */
	public CamBall correctBall(final List<CamBall> balls, final List<CamRobot> yellowBots, final List<CamRobot> blueBots)
	{
		CamBall ballToUse = findCurrentBall(balls);
		
		if (correctFlyingBalls)
		{
			// get Data of Ball and Bots
			final List<CamRobot> bots = new ArrayList<>(blueBots.size() + yellowBots.size());
			bots.addAll(blueBots);
			bots.addAll(yellowBots);
			
			// if kick possible, add new fly
			CamRobot possShooter = findPossibleShooter(ballToUse, bots);
			if (possShooter != null)
			{
				// append new fly
				// log.trace("kicker zone identified: " + possShooter.getPos() + " " + possShooter.getOrientation());
				altigraph.addKickerZoneIdentified(possShooter.getPos(), possShooter.getOrientation());
			}
			
			// append the ball to old and new flys
			altigraph.addCamFrame(ballToUse.getPos().getXYVector(), ballToUse.getCameraId());
			
			// if ball is flying
			if (altigraph.isBallFlying())
			{
				// log.trace("Ball is flying: " + altigraph.getCorrectedFrame().z());
				CamBall newBall = new CamBall(ballToUse.getConfidence(), ballToUse.getArea(), altigraph
						.getCorrectedFrame().x(),
						altigraph.getCorrectedFrame().y(), altigraph.getCorrectedFrame().z(),
						ballToUse.getPixelX(),
						ballToUse.getPixelY(), ballToUse.getTimestamp(), ballToUse.getCameraId());
				return newBall;
			}
		}
		return ballToUse;
	}
	
	
	private CamBall findCurrentBall(final List<CamBall> balls)
	{
		if (balls.isEmpty())
		{
			if ((SumatraClock.nanoTime() - lastSeenBall.getTimestamp()) > 3e8)
			{
				lastSeenBall = new CamBall(0, 0, lastSeenBall.getPos().x(), lastSeenBall.getPos().y(), lastSeenBall
						.getPos().z(),
						lastSeenBall.getPixelX(), lastSeenBall.getPixelY(), SumatraClock.nanoTime(),
						lastSeenBall.getCameraId());
			}
			return lastSeenBall;
		}
		
		CamBall ballToUse = balls.get(0);
		float shortestDifference = difference(ballToUse, lastSeenBall);
		for (CamBall ball : balls)
		{
			if (difference(ball, lastSeenBall) < shortestDifference)
			{
				ballToUse = ball;
				shortestDifference = difference(ball, lastSeenBall);
			}
		}
		
		boolean containsBallFromCurrentCam = balls.stream().anyMatch(b -> b.getCameraId() == lastSeenBall.getCameraId());
		float dt = Math.abs(lastSeenBall.getTimestamp() - ballToUse.getTimestamp()) / 1e9f;
		if (!containsBallFromCurrentCam && (dt < 0.05))
		{
			// wait some time before switching cameras
			// note: with a delay of 50ms, the ball can travel up to 40cm with 8m/s
			// but: if the ball is fast, it will slow down quickly and it will not do unexpected direction changes,
			// so we accept this to get more stable ball positions.
			return lastSeenBall;
		}
		float dist = difference(ballToUse, lastSeenBall) / 1000f;
		float vel = dist * dt;
		if (vel > 10)
		{
			// high velocity, probably noise
			return lastSeenBall;
		}
		
		// 1mm -> 0.1ms
		// 10mm -> 1ms -> 10m/s
		// if the ball is moving with less than 10m/s, the new ball will always be used. (following if will be false)
		// if (difference(lastSeenBall, ballToUse) > (TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime()
		// - lastSeenBall.getTimestamp()) * 10))
		// {
		// return lastSeenBall;
		// }
		lastSeenBall = new CamBall(ballToUse);
		return ballToUse;
	}
	
	
	private float difference(final CamBall ball1, final CamBall ball2)
	{
		if (ball1.getTimestamp() > (ball2.getTimestamp() + 1e8))
		{
			return 0;
		}
		return ball1.getPos().subtractNew(ball2.getPos()).getLength2()
				// add a penalty to balls that are not on same camera
				+ (ball1.getCameraId() == ball2.getCameraId() ? 0 : 200);
	}
	
	
	/*
	 * find out the potential kicker-bot
	 */
	private CamRobot findPossibleShooter(final CamBall ball, final List<CamRobot> bots)
	{
		for (final CamRobot bot : bots)
		{
			IVector2 kickerPos = AiMath
					.getBotKickerPos(bot.getPos(), bot.getOrientation(), Geometry.getCenter2DribblerDistDefault());
			if (GeoMath.distancePP(kickerPos, ball.getPos().getXYVector()) > (MAX_DIST_BALL + AIConfig.getGeometry()
					.getBotRadius()))
			{
				continue;
			}
			IVector2 bot2Ball = ball.getPos().getXYVector().subtract(bot.getPos());
			if (Math.abs(AngleMath.difference(bot2Ball.getAngle(), bot.getOrientation())) > MAX_ORIENTATION_DIFF)
			{
				continue;
			}
			
			return bot;
		}
		return null;
	}
	
	
	/**
	 * @param pos
	 */
	public void setLatestBallPos(final IVector2 pos)
	{
		lastSeenBall = new CamBall(1, 0, pos.x(), pos.y(), 0, 0, 0, SumatraClock.nanoTime(), 0);
	}
	
	
	/**
	 * @param folder
	 */
	public static void runOnData(final String folder)
	{
		List<RawBall> allRawBalls = ExportDataContainer.readRawBall(folder, "rawBalls");
		List<RawBall> rawBalls = ExportDataContainer.readRawBall(folder, "rawBall");
		List<RawBot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<RawBot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		StaticSimulationClock clock = new StaticSimulationClock();
		clock.setNanoTime(rawBalls.get(0).getTimestamp());
		SumatraClock.setClock(clock);
		
		BallCorrector ballCorrector = new BallCorrector();
		ballCorrector.lastSeenBall = rawBalls.get(0).toCamBall();
		
		
		if (rawBalls.get(0).getFrameId() == -1)
		{
			List<RawBall> allRawBallsTmp = new ArrayList<>(allRawBalls);
			for (int i = 0; i < rawBalls.size(); i++)
			{
				rawBalls.get(i).setFrameId(i);
				while (!allRawBallsTmp.isEmpty()
						&& (allRawBallsTmp.get(0).getTimestamp() <= rawBalls.get(i).getTimestamp()))
				{
					allRawBallsTmp.remove(0).setFrameId(i);
				}
			}
		}
		
		
		List<RawBall> newRawBalls = new ArrayList<>();
		
		for (int i = 0; i < rawBalls.size(); i++)
		{
			RawBall rawBall = rawBalls.get(i);
			CamBall ball = rawBall.toCamBall();
			clock.setNanoTime(ball.getTimestamp());
			
			List<CamBall> frameBalls = allRawBalls.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamBall()).collect(Collectors.toList());
			List<CamRobot> frameBotsY = yellowBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamRobot()).collect(Collectors.toList());
			List<CamRobot> frameBotsB = blueBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb.toCamRobot()).collect(Collectors.toList());
			CamBall corrBall = ballCorrector.correctBall(frameBalls, frameBotsY, frameBotsB);
			
			newRawBalls.add(new RawBall(corrBall.getTimestamp(), corrBall.getCameraId(), corrBall.getPos(), rawBall
					.getFrameId()));
			
			// for (CamBall cball : frameBalls)
			// {
			// System.out.println("\t" + cball);
			// }
			// System.out.println(rawBall);
			// System.out.println(corrBall);
			// System.out.println();
		}
		
		SumatraClock.setClock(new RealTimeClock());
		
		CSVExporter.exportList(folder, "rawBallCorrected", newRawBalls.stream().map(c -> c));
	}
}
