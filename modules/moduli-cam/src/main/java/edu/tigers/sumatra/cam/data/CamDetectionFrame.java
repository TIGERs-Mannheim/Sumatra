/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class contains every information an
 * SSL_DetectionFrame has to offer about
 * the current situation on the field
 */
@RequiredArgsConstructor
@Getter
public class CamDetectionFrame
{
	/**
	 * independent global frame number, continuous
	 */
	private final long globalFrameId;

	/**
	 * Capture time of this frame in [ns].
	 * This may have been adjusted/optimized to regular intervals.
	 * This is not a local time, but based on ssl-vision time.
	 */
	private final long timestamp;

	/**
	 * Unmodified timestamps as reported by ssl-vision.
	 * Use only for inspection, not for AI computations.
	 */
	private final OriginalTimestamps originalTimestamps;

	private final int cameraId;
	private final long camFrameNumber;

	private final List<CamBall> balls;
	private final List<CamRobot> robotsYellow;
	private final List<CamRobot> robotsBlue;
	private final transient List<CamRobot> robots = new ArrayList<>();


	protected CamDetectionFrame()
	{
		timestamp = 0;
		originalTimestamps = new OriginalTimestamps(0, 0, null);
		cameraId = 0;
		camFrameNumber = 0;
		globalFrameId = 0;
		balls = null;
		robotsYellow = Collections.emptyList();
		robotsBlue = Collections.emptyList();
	}


	/**
	 * @param f
	 */
	public CamDetectionFrame(final CamDetectionFrame f)
	{
		timestamp = f.timestamp;
		originalTimestamps = f.originalTimestamps;
		cameraId = f.cameraId;
		camFrameNumber = f.camFrameNumber;
		globalFrameId = f.globalFrameId;
		balls = f.balls;
		robotsBlue = f.robotsBlue;
		robotsYellow = f.robotsYellow;
	}


	/**
	 * @param f            base frame to copy
	 * @param balls        new balls
	 * @param robotsYellow new robots
	 * @param robotsBlue   new robots
	 */
	public CamDetectionFrame(
			final CamDetectionFrame f,
			final List<CamBall> balls, final List<CamRobot> robotsYellow, final List<CamRobot> robotsBlue
	)
	{
		timestamp = f.timestamp;
		originalTimestamps = f.originalTimestamps;
		cameraId = f.cameraId;
		camFrameNumber = f.camFrameNumber;
		globalFrameId = f.globalFrameId;

		this.balls = Collections.unmodifiableList(balls);
		this.robotsBlue = Collections.unmodifiableList(robotsBlue);
		this.robotsYellow = Collections.unmodifiableList(robotsYellow);
	}


	/**
	 * @return all robots (yellow and blue)
	 */
	public List<CamRobot> getRobots()
	{
		if (robots.isEmpty())
		{
			robots.addAll(robotsYellow);
			robots.addAll(robotsBlue);
		}
		return Collections.unmodifiableList(robots);
	}


	@Value
	public static class OriginalTimestamps
	{
		/**
		 * Time at which ssl-vision receives a camera frame. Local ssl-vision time in [ns].
		 */
		long tCapture;
		/**
		 * Time at which ssl-vision sent the processed detection frame. Local ssl-vision time in [ns].
		 */
		long tSent;
		/**
		 * Time at which the camera captured an image. Usually, this is a camera internal clock in [ns].
		 * This field is optional, time may be null.
		 */
		Long tCaptureCamera;
	}
}
