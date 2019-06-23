/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Lukas
 * Clemens
 * Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;


/**
 * Provides a static conversion-method for the {@link SSL_DetectionFrame} to wrap the incoming SSL-Vision formats with
 * our own, internal representations
 * 
 * @see edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam
 * @author Lukas, Clemens, Gero
 * 
 */
public class SSLVisionCamDetectionTranslator
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private long	oldReceivedTimeStamp	= 1;
	private long	oldPacketCount			= 1;
	private double	fps						= 0;
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Static Method for translating.
	 * <p>
	 * By convention, we are always playing from right to left. So this method has to turn everything around to guarantee
	 * that
	 * </p>
	 * @param detectionFrame
	 * @param timeOffsetMillis
	 * @param timeOffsetNanos
	 * @param receivedTimeStamp
	 * @param packetCount
	 * @param teamProps
	 * @return
	 */
	public CamDetectionFrame translate(SSL_DetectionFrame detectionFrame, double timeOffsetMillis, long timeOffsetNanos,
			long receivedTimeStamp, long packetCount, TeamProps teamProps)
	{
		final boolean haveToTurn = teamProps.getPlayLeftToRight();
		final boolean weAreYellow = teamProps.getTigersAreYellow();
		
		
		// --- check if detectionFrame != null ---
		if (detectionFrame == null)
		{
			return null;
		}
		
		final List<CamBall> balls = new CopyOnWriteArrayList<CamBall>();
		final List<CamRobot> blues = new ArrayList<CamRobot>(6);
		final List<CamRobot> yellows = new ArrayList<CamRobot>(6);
		
		
		// --- if we play from left to right, turn ball and robots, so that we're always playing from right to left ---
		// --- process team Blue ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, haveToTurn));
		}
		
		// --- process team Yellow ---
		for (final SSL_DetectionRobot detectionRobot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(detectionRobot, haveToTurn));
		}
		
		// --- process ball ---
		for (final SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, haveToTurn));
		}
		
		// FIXME Peter should this be handled here or in grSim?
		// actually it would be nice to be independent from unix-timestamps
		// not unix-time. this is required to work with grSim
		if (detectionFrame.getTSent() < 1E12)
		{
			timeOffsetMillis = 0;
			timeOffsetNanos = 0;
		}
		
		// Process timestamps (see SSLVisionCam#SSLVisionCam for details!)
		// Times are converted from millis (relative to 01.01.1970 to System.nanotime)
		
		// FIXME: don't know what I did, but mixed team positions working
		// final double captureMillis = (detectionFrame.getTCapture() * 1000) - timeOffsetMillis;
		// final long tCapture = (long) (captureMillis * 1000000) - timeOffsetNanos;
		final long tCapture = receivedTimeStamp;
		
		// final double sentMillis = (detectionFrame.getTSent() * 1000) - timeOffsetMillis;
		// final long tSent = (long) (sentMillis * 1000000) - timeOffsetNanos;
		final long tSent = receivedTimeStamp;
		
		
		// --- refresh fps every second ---
		if (receivedTimeStamp >= (oldReceivedTimeStamp + 1000000000))
		{
			fps = ((double) (packetCount - oldPacketCount) / (double) (receivedTimeStamp - oldReceivedTimeStamp)) * 1000000000.00;
			oldPacketCount = packetCount;
			oldReceivedTimeStamp = receivedTimeStamp;
		}
		
		CamDetectionFrame frame;
		
		// --- team-colour assignment ---
		if (weAreYellow)
		{
			frame = new CamDetectionFrame(tCapture, tSent, receivedTimeStamp, detectionFrame.getCameraId(),
					detectionFrame.getFrameNumber(), fps, balls, yellows, blues, new TeamProps(teamProps));
		} else
		{
			frame = new CamDetectionFrame(tCapture, tSent, receivedTimeStamp, detectionFrame.getCameraId(),
					detectionFrame.getFrameNumber(), fps, balls, blues, yellows, new TeamProps(teamProps));
		}
		return frame;
	}
	
	
	/**
	 * @param bot
	 * @param turn Whether the new representation should be turned around {@link SSLVisionCamDetectionTranslator}
	 *           {@link #translate(SSL_DetectionFrame, double, long, long, long, TeamProps)}
	 * @return A {@link CamRobot} representing the given {@link SSL_DetectionRobot}
	 */
	private static CamRobot convertRobot(SSL_DetectionRobot bot, boolean turn)
	{
		
		// Values which depend on 'turn'
		float orientation;
		float x;
		float y;
		
		// Turn if necessary
		if (turn)
		{
			// --- rotate orientation of the robot 180 degree (pi) ---
			if (bot.getOrientation() < 0)
			{
				orientation = bot.getOrientation() + (float) Math.PI;
			} else
			{
				orientation = bot.getOrientation() - (float) Math.PI;
			}
			
			x = -bot.getX();
			y = -bot.getY();
			
			
		} else
		{
			orientation = bot.getOrientation();
			
			x = bot.getX();
			y = bot.getY();
		}
		
		// Finally put everything together
		return new CamRobot(bot.getConfidence(), bot.getRobotId(),
		
		x, y, orientation,
		
		// TODO Gero: turn pixelX and pixelY When someone knows what these values mean?!?!
				bot.getPixelX(), bot.getPixelY(),
				
				bot.getHeight());
	}
	
	
	/**
	 * @param ball
	 * @param turn Whether the new representation should be turned around {@link SSLVisionCamDetectionTranslator}
	 *           {@link #translate(SSL_DetectionFrame, double, long, long, long, TeamProps)}
	 * @return A {@link CamBall} representing the given {@link SSL_DetectionBall}
	 */
	private static CamBall convertBall(SSL_DetectionBall ball, boolean turn)
	{
		
		// Values which depend on 'turn'
		float x;
		float y;
		
		// Turn if necessary
		if (turn)
		{
			x = -ball.getX();
			y = -ball.getY();
		} else
		{
			x = ball.getX();
			y = ball.getY();
		}
		
		return new CamBall(ball.getConfidence(),
		
		// TODO Gero: turn area, pixelX and pixelY When someone knows what these values mean?!?!
				ball.getArea(),
				
				x, y,
				
				ball.getZ(),
				
				ball.getPixelX(), ball.getPixelY());
	}
}
