/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 15, 2011
 * Author(s): Birgit
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.flyingBalls.Altigraph;


/**
 * This Class is to correct the ssl-ballPosition from bottom to fly
 * 
 * @author Birgit
 */
public class BallCorrector
{
	
	
	@SuppressWarnings("unused")
	private static final Logger		log						= Logger.getLogger(BallCorrector.class.getName());
	
	@Configurable(comment = "correct flying balls")
	private static boolean				correctFlyingBalls	= false;
	private static final double		MAX_DIST_BALL			= 50;
	private static final double		MAX_ORIENTATION_DIFF	= 0.1;
	
	private final Altigraph				altigraph;
	private final FlyingBallFitter	flyingBallFitter;
	
	private CamBall						lastSeenBall			= new CamBall();
	
	
	static
	{
		ConfigRegistration.registerClass("wp", BallCorrector.class);
	}
	
	
	/**
	 */
	public BallCorrector()
	{
		altigraph = new Altigraph();
		flyingBallFitter = new FlyingBallFitter();
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
		
		// currently use only the one selected ball
		List<CamBall> selectedBall = new ArrayList<CamBall>();
		selectedBall.add(ballToUse);
		correctFlyingBalls = true;
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
				
				flyingBallFitter.onBallCloseToBot(selectedBall);
			} else
			{
				flyingBallFitter.onBallAwayFromBot(selectedBall);
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
						ballToUse.getPixelY(), ballToUse.gettCapture(), ballToUse.gettSent(), ballToUse.getCameraId(),
						ballToUse.getFrameId());
				return newBall;
			}
		}
		return ballToUse;
	}
	
	
	private CamBall findCurrentBall(final List<CamBall> balls)
	{
		if (balls.isEmpty())
		{
			// if ((System.nanoTime() - lastSeenBall.getTimestamp()) > 3e8)
			// {
			// lastSeenBall = new CamBall(0, 0, lastSeenBall.getPos().x(), lastSeenBall.getPos().y(), lastSeenBall
			// .getPos().z(),
			// lastSeenBall.getPixelX(), lastSeenBall.getPixelY(), System.nanoTime(), System.nanoTime(),
			// lastSeenBall.getCameraId(), lastSeenBall.getFrameId());
			// }
			return lastSeenBall;
		}
		
		CamBall ballToUse = balls.get(0);
		double shortestDifference = difference(ballToUse, lastSeenBall);
		for (CamBall ball : balls)
		{
			if (difference(ball, lastSeenBall) < shortestDifference)
			{
				ballToUse = ball;
				shortestDifference = difference(ball, lastSeenBall);
			}
		}
		
		boolean containsBallFromCurrentCam = balls.stream().anyMatch(b -> b.getCameraId() == lastSeenBall.getCameraId());
		double dt = Math.abs(lastSeenBall.getTimestamp() - ballToUse.getTimestamp()) / 1e9;
		if (!containsBallFromCurrentCam && (dt < 0.05))
		{
			// wait some time before switching cameras
			// note: with a delay of 50ms, the ball can travel up to 40cm with 8m/s
			// but: if the ball is fast, it will slow down quickly and it will not do unexpected direction changes,
			// so we accept this to get more stable ball positions.
			return lastSeenBall;
		}
		double dist = difference(ballToUse, lastSeenBall) / 1000.0;
		double vel = dist * dt;
		if (vel > 10)
		{
			// high velocity, probably noise
			return lastSeenBall;
		}
		
		// 1mm -> 0.1ms
		// 10mm -> 1ms -> 10m/s
		// if the ball is moving with less than 10m/s, the new ball will always be used. (following if will be false)
		// if (difference(lastSeenBall, ballToUse) > (TimeUnit.NANOSECONDS.toMillis(System.nanoTime()
		// - lastSeenBall.getTimestamp()) * 10))
		// {
		// return lastSeenBall;
		// }
		lastSeenBall = new CamBall(ballToUse);
		return ballToUse;
	}
	
	
	private double difference(final CamBall ball1, final CamBall ball2)
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
			IVector2 kickerPos = GeoMath
					.getBotKickerPos(bot.getPos(), bot.getOrientation(), Geometry.getCenter2DribblerDistDefault());
			if (GeoMath.distancePP(kickerPos, ball.getPos().getXYVector()) > (MAX_DIST_BALL + Geometry
					.getBallRadius()))
			{
				continue;
			}
			IVector2 bot2Ball = ball.getPos().getXYVector().subtractNew(bot.getPos());
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
		long timestamp = lastSeenBall.getTimestamp() + (long) (1e8);
		lastSeenBall = new CamBall(1, 0, pos.x(), pos.y(), 0, 0, 0, timestamp, timestamp, 0,
				0);
	}
	
	
	/**
	 * @param folder
	 */
	public static void runOnData(final String folder)
	{
		List<CamBall> allRawBalls = ExportDataContainer.readRawBall(folder, "rawBalls");
		List<CamBall> rawBalls = ExportDataContainer.readRawBall(folder, "rawBall");
		List<CamRobot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<CamRobot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		BallCorrector ballCorrector = new BallCorrector();
		ballCorrector.lastSeenBall = rawBalls.get(0);
		
		List<CamBall> newRawBalls = new ArrayList<>();
		
		for (int i = 0; i < rawBalls.size(); i++)
		{
			CamBall rawBall = rawBalls.get(i);
			
			List<CamBall> frameBalls = allRawBalls.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
			List<CamRobot> frameBotsY = yellowBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
			List<CamRobot> frameBotsB = blueBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
			CamBall corrBall = ballCorrector.correctBall(frameBalls, frameBotsY, frameBotsB);
			
			newRawBalls.add(corrBall);
			
			// for (CamBall cball : frameBalls)
			// {
			// System.out.println("\t" + cball);
			// }
			// System.out.println(rawBall);
			// System.out.println(corrBall);
			// System.out.println();
		}
		
		CSVExporter.exportList(folder, "rawBallCorrected", newRawBalls.stream().map(c -> c));
	}
}
