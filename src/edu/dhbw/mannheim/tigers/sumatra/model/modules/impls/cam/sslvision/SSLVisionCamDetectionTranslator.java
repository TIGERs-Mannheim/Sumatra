/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Lukas
 * Clemens
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;


/**
 * Provides a static conversion-method for the {@link SSL_DetectionFrame} to wrap the incoming SSL-Vision formats with
 * our own, internal representations
 * 
 * @see edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam
 * @author Lukas, Clemens, Gero
 */
public class SSLVisionCamDetectionTranslator
{
	@SuppressWarnings("unused")
	private static final Logger	log		= Logger.getLogger(SSLVisionCamDetectionTranslator.class.getName());
	
	private long						offsetMs	= -1;
	private long						offsetNs	= -1;
	
	
	private long convertVision2LocalTime(final double visionS)
	{
		double visionMs = visionS * 1e3;
		double localMs = visionMs - offsetMs;
		long localNs = (long) (localMs * 1e6) - offsetNs;
		return localNs;
	}
	
	
	/**
	 * Static Method for translating.
	 * <p>
	 * By convention, we are always playing from right to left. So this method has to turn everything around to guarantee
	 * that
	 * </p>
	 * 
	 * @param detectionFrame
	 * @return
	 */
	public CamDetectionFrame translate(final SSL_DetectionFrame detectionFrame)
	{
		long localSentNs = convertVision2LocalTime(detectionFrame.getTSent());
		long diff = System.nanoTime() - localSentNs;
		if (Math.abs(diff) > 1e9)
		{
			long sysNs = System.nanoTime();
			double sentMs = detectionFrame.getTSent() * 1e3;
			long sentMsCut = (long) sentMs;
			offsetMs = sentMsCut - (long) (sysNs * 1e-6);
			
			sentMs -= sentMsCut;
			sysNs -= (long) (sysNs * 1e-6) * 1e6;
			offsetNs = (long) (sentMs * 1e6) - sysNs;
			
			localSentNs = convertVision2LocalTime(detectionFrame.getTSent());
			
			log.info(String.format("Synced with vision clock. offsetMs:%d offsetNs:%d", offsetMs, offsetNs));
		}
		
		long localCaptureNs = convertVision2LocalTime(detectionFrame.getTCapture());
		long localReceiveNs = System.nanoTime();
		
		// network is fast, clocks are probably not synced.
		// so lets assume tSent and tReceived is equal.
		// with this assumption, we can calculate tCapture in System.nanoTime() from tSent and tCapture in detectionFrame
		// long diff = (long) ((detectionFrame.getTSent() - detectionFrame.getTCapture()) * 1e9);
		// long tReceived = System.nanoTime();
		// long tSent = tReceived;
		// // long tCapture = tReceived - diff;
		// long tCapture = (long) (detectionFrame.getTCapture() * 1e9);
		
		final List<CamBall> balls = new ArrayList<CamBall>();
		final List<CamRobot> blues = new ArrayList<CamRobot>();
		final List<CamRobot> yellows = new ArrayList<CamRobot>();
		
		// --- if we play from left to right, turn ball and robots, so that we're always playing from right to left ---
		// --- process team Blue ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, localCaptureNs, detectionFrame.getCameraId()));
		}
		
		// --- process team Yellow ---
		for (final SSL_DetectionRobot detectionRobot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(detectionRobot, localCaptureNs, detectionFrame.getCameraId()));
		}
		
		// --- process ball ---
		for (final SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, localCaptureNs, detectionFrame.getCameraId()));
		}
		
		CamDetectionFrame frame = new CamDetectionFrame(localCaptureNs, localSentNs, localReceiveNs,
				detectionFrame.getCameraId(),
				detectionFrame.getFrameNumber(), balls, yellows, blues);
		return frame;
	}
	
	
	/**
	 * @param bot
	 * @param timestamp
	 * @return A {@link CamRobot} representing the given {@link SSL_DetectionRobot}
	 */
	private static CamRobot convertRobot(final SSL_DetectionRobot bot, final long timestamp, final int camId)
	{
		float orientation;
		float x;
		float y;
		
		orientation = bot.getOrientation();
		
		x = bot.getX();
		y = bot.getY();
		
		// Finally put everything together
		return new CamRobot(bot.getConfidence(), bot.getRobotId(),
				x, y, orientation,
				bot.getPixelX(), bot.getPixelY(),
				bot.getHeight(),
				timestamp,
				camId);
	}
	
	
	/**
	 * @param ball
	 * @param timestamp
	 * @return A {@link CamBall} representing the given {@link SSL_DetectionBall}
	 */
	private static CamBall convertBall(final SSL_DetectionBall ball, final long timestamp, final int camId)
	{
		float x;
		float y;
		x = ball.getX();
		y = ball.getY();
		
		return new CamBall(ball.getConfidence(),
				ball.getArea(),
				x, y,
				ball.getZ(),
				ball.getPixelX(), ball.getPixelY(),
				timestamp,
				camId);
	}
}
