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
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Log4j2
public class CamDetectionConverter
{
	private static final int CAM_TO_CAPTURE_ESTIMATOR_NUM_SAMPLES = 100;

	private long frameId = 0;
	private final HashMap<Integer, List<Double>> camToCaptureOffsets = new HashMap<>();

	@Configurable(defValue = "true", comment = "Use camera capture timestamp if present")
	private static boolean useCameraCaptureTimestamp = true;

	static
	{
		ConfigRegistration.registerClass("cam", CamDetectionConverter.class);
	}


	/**
	 * @param detectionFrame SSL vision frame from a single camera
	 * @return a cam detection frame based on the SSL vision frame
	 */
	public CamDetectionFrame convertDetectionFrame(final SslVisionDetection.SSL_DetectionFrame detectionFrame)
	{
		var originalTimestamps = new CamDetectionFrame.OriginalTimestamps(
				(long) (detectionFrame.getTCapture() * 1e9), (long) (detectionFrame.getTSent() * 1e9),
				detectionFrame.hasTCaptureCamera() ? (long) (detectionFrame.getTCaptureCamera() * 1e9) : null
		);

		long timestamp = (long) (detectionFrame.getTCapture() * 1e9);

		if (detectionFrame.hasTCaptureCamera())
		{
			var camToCaptureOffsetList = camToCaptureOffsets.computeIfAbsent(
					detectionFrame.getCameraId(),
					k -> new LinkedList<>()
			);

			camToCaptureOffsetList.add(detectionFrame.getTCapture() - detectionFrame.getTCaptureCamera());
			if (camToCaptureOffsetList.size() > CAM_TO_CAPTURE_ESTIMATOR_NUM_SAMPLES)
			{
				camToCaptureOffsetList.removeFirst();
			}

			if (!camToCaptureOffsetList.isEmpty() && useCameraCaptureTimestamp)
			{
				double camToCaptureOffsetMedian = StatisticsMath.median(camToCaptureOffsetList);
				timestamp = (long) ((detectionFrame.getTCaptureCamera() + camToCaptureOffsetMedian) * 1e9);
			}
		}

		final List<CamBall> balls = new ArrayList<>();
		final List<CamRobot> blues = new ArrayList<>();
		final List<CamRobot> yellows = new ArrayList<>();

		for (final SslVisionDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, ETeamColor.BLUE, frameId, detectionFrame.getCameraId(), timestamp));
		}

		for (final SslVisionDetection.SSL_DetectionRobot bot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(bot, ETeamColor.YELLOW, frameId, detectionFrame.getCameraId(), timestamp));
		}

		for (final SslVisionDetection.SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, timestamp, detectionFrame.getCameraId(), frameId));
		}

		return new CamDetectionFrame(
				frameId++, timestamp, originalTimestamps,
				detectionFrame.getCameraId(), detectionFrame.getFrameNumber(), balls, yellows, blues
		);
	}


	private static CamRobot convertRobot(
			final SslVisionDetection.SSL_DetectionRobot bot,
			final ETeamColor color,
			final long globalFrameId,
			final int camId,
			final long tCapture
	)
	{
		return new CamRobot(
				bot.getConfidence(),
				Vector2.fromXY(bot.getPixelX(), bot.getPixelY()),
				tCapture,
				camId,
				globalFrameId,
				Vector2.fromXY(bot.getX(), bot.getY()),
				bot.getOrientation(),
				bot.getHeight(),
				BotID.createBotId(bot.getRobotId(), color)
		);
	}


	private static CamBall convertBall(
			final SslVisionDetection.SSL_DetectionBall ball,
			final long tCapture,
			final int camId,
			final long globalFrameId
	)
	{
		return new CamBall(
				ball.getConfidence(),
				ball.getArea(),
				Vector3.fromXYZ(ball.getX(), ball.getY(), ball.getZ()),
				Vector2.fromXY(ball.getPixelX(), ball.getPixelY()),
				tCapture,
				camId,
				globalFrameId
		);
	}
}
