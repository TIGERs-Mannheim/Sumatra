package edu.tigers.sumatra.cam;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.MessagesRobocupSslDetection;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamDetectionConverter
{
	private long frameId = 0;
	
	
	/**
	 * @param detectionFrame SSL vision frame from a single camera
	 * @param timeSync sync handle to convert timestamps
	 * @return a cam detection frame based on the SSL vision frame
	 */
	public CamDetectionFrame convertDetectionFrame(final MessagesRobocupSslDetection.SSL_DetectionFrame detectionFrame,
			final TimeSync timeSync)
	{
		long localCaptureNs = timeSync.sync(detectionFrame.getTCapture());
		long localSentNs = timeSync.sync(detectionFrame.getTSent());
		
		final List<CamBall> balls = new ArrayList<>();
		final List<CamRobot> blues = new ArrayList<>();
		final List<CamRobot> yellows = new ArrayList<>();
		
		for (final MessagesRobocupSslDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, ETeamColor.BLUE, frameId, detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process team Yellow ---
		for (final MessagesRobocupSslDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(bot, ETeamColor.YELLOW, frameId,
					detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process ball ---
		for (final MessagesRobocupSslDetection.SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, localCaptureNs, localSentNs, detectionFrame.getCameraId(),
					frameId));
		}
		
		
		return new CamDetectionFrame(localCaptureNs, localSentNs, detectionFrame.getCameraId(),
				frameId++, balls, yellows, blues);
	}
	
	
	private static CamRobot convertRobot(
			final MessagesRobocupSslDetection.SSL_DetectionRobot bot,
			final ETeamColor color,
			final long frameId,
			final int camId,
			final long tCapture,
			final long tSent)
	{
		return new CamRobot(
				bot.getConfidence(),
				Vector2.fromXY(bot.getPixelX(), bot.getPixelY()),
				tCapture,
				tSent,
				camId,
				frameId,
				Vector2.fromXY(bot.getX(), bot.getY()),
				bot.getOrientation(),
				bot.getHeight(),
				BotID.createBotId(bot.getRobotId(), color));
	}
	
	
	private static CamBall convertBall(
			final MessagesRobocupSslDetection.SSL_DetectionBall ball,
			final long tCapture,
			final long tSent,
			final int camId,
			final long frameId)
	{
		return new CamBall(
				ball.getConfidence(),
				ball.getArea(),
				Vector3.fromXYZ(ball.getX(), ball.getY(), ball.getZ()),
				Vector2.fromXY(ball.getPixelX(), ball.getPixelY()),
				tCapture,
				tSent,
				camId,
				frameId);
	}
	
}
