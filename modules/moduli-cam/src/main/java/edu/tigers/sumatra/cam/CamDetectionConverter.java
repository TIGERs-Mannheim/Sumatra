/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.cam.proto.SslVisionDetection;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamDetectionConverter
{
	private long frameId = 0;
	private HashMap<Integer, ExponentialMovingAverageFilter> camToCaptureOffsets = new HashMap<>();

	@Configurable(defValue = "true", comment = "Use camera capture timestamp if present")
	private static boolean useCameraCaptureTimestamp = true;

	static
	{
		ConfigRegistration.registerClass("cam", CamDetectionConverter.class);
	}

	private static CamRobot convertRobot(
			final SslVisionDetection.SSL_DetectionRobot bot,
			final ETeamColor color,
			final long frameId,
			final int camId,
			final long tCapture,
			final Long tCaptureCamera,
			final long tSent)
	{
		return new CamRobot(
				bot.getConfidence(),
				Vector2.fromXY(bot.getPixelX(), bot.getPixelY()),
				tCapture,
				tCaptureCamera,
				tSent,
				camId,
				frameId,
				Vector2.fromXY(bot.getX(), bot.getY()),
				bot.getOrientation(),
				bot.getHeight(),
				BotID.createBotId(bot.getRobotId(), color));
	}


	/**
	 * @param detectionFrame SSL vision frame from a single camera
	 * @return a cam detection frame based on the SSL vision frame
	 */
	public CamDetectionFrame convertDetectionFrame(final SslVisionDetection.SSL_DetectionFrame detectionFrame)
	{
		double dtCaptureCam;
		if (detectionFrame.hasTCaptureCamera() && useCameraCaptureTimestamp)
		{
			dtCaptureCam = detectionFrame.getTCapture() - detectionFrame.getTCaptureCamera();
		} else
		{
			dtCaptureCam = 0.0;
		}

		ExponentialMovingAverageFilter camToCaptureOffset = camToCaptureOffsets.computeIfAbsent(
				detectionFrame.getCameraId(), k -> new ExponentialMovingAverageFilter(0.99, dtCaptureCam));
		camToCaptureOffset.update(dtCaptureCam);

		long localCaptureNs = (long) (detectionFrame.getTCapture() * 1e9);
		long localSentNs = (long) (detectionFrame.getTSent() * 1e9);
		Long localCaptureCameraNs = null;
		if (detectionFrame.hasTCaptureCamera())
			localCaptureCameraNs = (long) ((detectionFrame.getTCaptureCamera() + camToCaptureOffset.getState()) * 1e9);

		final List<CamBall> balls = new ArrayList<>();
		final List<CamRobot> blues = new ArrayList<>();
		final List<CamRobot> yellows = new ArrayList<>();

		for (final SslVisionDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, ETeamColor.BLUE, frameId, detectionFrame.getCameraId(),
					localCaptureNs, localCaptureCameraNs, localSentNs));
		}

		// --- process team Yellow ---
		for (final SslVisionDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(bot, ETeamColor.YELLOW, frameId,
					detectionFrame.getCameraId(),
					localCaptureNs, localCaptureCameraNs, localSentNs));
		}

		// --- process ball ---
		for (final SslVisionDetection.SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, localCaptureNs, localCaptureCameraNs, localSentNs, detectionFrame.getCameraId(),
					frameId));
		}


		return new CamDetectionFrame(localCaptureNs, localSentNs, localCaptureCameraNs, detectionFrame.getCameraId(),
				detectionFrame.getFrameNumber(),
				frameId++, balls, yellows, blues);
	}


	private static CamBall convertBall(
			final SslVisionDetection.SSL_DetectionBall ball,
			final long tCapture,
			final Long tCaptureCamera,
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
				tCaptureCamera,
				tSent,
				camId,
				frameId);
	}

}
