/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.sleepycat.persist.model.Persistent;
import lombok.Getter;


/**
 * This class contains every information a
 * SSL_DetectionFrame has to offer about
 * the current situation on the field
 *
 * @author Gero
 */
@Persistent
public class CamDetectionFrame
{
	/**
	 * time-stamp in System.nanotime()
	 */
	private final long tCapture;

	/**
	 * time-stamp in System.nanotime()
	 */
	private final long tSent;

	/**
	 * Camera capture time, not necessarily synced to tCapture or tSent, can be null [ns]
	 */
	@Getter
	private final Long tCaptureCamera;

	private final long tAssembly;

	/**
	 * ID 0..n
	 */
	private final int cameraId;
	private final long camFrameNumber;

	/**
	 * independent frame number, continuous
	 */
	private final long frameNumber;
	private final List<CamBall> balls;
	private final List<CamRobot> robotsYellow;
	private final List<CamRobot> robotsBlue;
	private final transient List<CamRobot> robots = new ArrayList<>();


	protected CamDetectionFrame()
	{
		tCapture = 0;
		tSent = 0;
		tCaptureCamera = null;
		tAssembly = 0;
		cameraId = 0;
		camFrameNumber = 0;
		frameNumber = 0;
		balls = null;
		robotsYellow = Collections.emptyList();
		robotsBlue = Collections.emptyList();
	}


	/**
	 * @param tCapture
	 * @param tSent
	 * @param cameraId
	 * @param camFrameNumber
	 * @param frameNumber
	 * @param balls
	 * @param yellowBots
	 * @param blueBots
	 */
	@SuppressWarnings("squid:S00107") // number of parameters - we accept this for performance reasons here
	public CamDetectionFrame(final long tCapture, final long tSent, final Long tCaptureCamera, final int cameraId,
			final long camFrameNumber, final long frameNumber,
			final List<CamBall> balls, final List<CamRobot> yellowBots, final List<CamRobot> blueBots)
	{
		// Fields
		this.tCapture = tCapture;
		this.tSent = tSent;
		this.tCaptureCamera = tCaptureCamera;
		this.cameraId = cameraId;
		this.camFrameNumber = camFrameNumber;
		this.frameNumber = frameNumber;

		// Collections
		this.balls = balls;
		robotsYellow = yellowBots;
		robotsBlue = blueBots;

		tAssembly = System.nanoTime();
	}


	/**
	 * @param f
	 */
	public CamDetectionFrame(final CamDetectionFrame f)
	{
		tCapture = f.tCapture;
		tSent = f.tSent;
		tCaptureCamera = f.tCaptureCamera;
		tAssembly = f.tAssembly;
		cameraId = f.cameraId;
		camFrameNumber = f.camFrameNumber;
		frameNumber = f.frameNumber;
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
	public CamDetectionFrame(final CamDetectionFrame f,
			final List<CamBall> balls, final List<CamRobot> robotsYellow, final List<CamRobot> robotsBlue)
	{
		tCapture = f.tCapture;
		tSent = f.tSent;
		tCaptureCamera = f.tCaptureCamera;
		tAssembly = f.tAssembly;
		cameraId = f.cameraId;
		camFrameNumber = f.camFrameNumber;
		frameNumber = f.frameNumber;

		this.balls = balls;
		this.robotsBlue = robotsBlue;
		this.robotsYellow = robotsYellow;
	}


	/**
	 * New CamDetectionFrame with adjusted tCapture.
	 *
	 * @param f
	 * @param tCapture
	 */
	public CamDetectionFrame(final CamDetectionFrame f, final long tCapture)
	{
		balls = f.getBalls().stream()
				.map(b -> new CamBall(b, tCapture))
				.collect(Collectors.toList());

		robotsYellow = f.getRobotsYellow().stream()
				.map(r -> new CamRobot(r, tCapture))
				.collect(Collectors.toList());

		robotsBlue = f.getRobotsBlue().stream()
				.map(r -> new CamRobot(r, tCapture))
				.collect(Collectors.toList());

		this.tCapture = tCapture;
		tSent = f.tSent;
		tCaptureCamera = f.tCaptureCamera;
		tAssembly = f.tAssembly;
		cameraId = f.cameraId;
		camFrameNumber = f.camFrameNumber;
		frameNumber = f.frameNumber;
	}


	@Override
	public String toString()
	{
		return "tCapture:" + gettCapture() + "/tSend:" + gettSent() + "/cameraId:"
				+ getCameraId()
				+ "/frameNumber:" + getFrameNumber() + "/balls:" + getBalls() + "/rY:"
				+ getRobotsYellow()
				+ "/rB:" + getRobotsBlue();
	}


	/**
	 * @return the tCapture
	 */
	public long gettCapture()
	{
		return tCapture;
	}


	/**
	 * @return the tSent
	 */
	public long gettSent()
	{
		return tSent;
	}


	/**
	 * @return the cameraId
	 */
	public int getCameraId()
	{
		return cameraId;
	}


	/**
	 * @return the frameNumber for the current camera
	 */
	public long getCamFrameNumber()
	{
		return camFrameNumber;
	}


	/**
	 * @return the frameNumber
	 */
	public long getFrameNumber()
	{
		return frameNumber;
	}


	/**
	 * @return the balls
	 */
	public List<CamBall> getBalls()
	{
		return Collections.unmodifiableList(balls);
	}


	/**
	 * @return the robotsYellow
	 */
	public List<CamRobot> getRobotsYellow()
	{
		return Collections.unmodifiableList(robotsYellow);
	}


	/**
	 * @return the robotsBlue
	 */
	public List<CamRobot> getRobotsBlue()
	{
		return Collections.unmodifiableList(robotsBlue);
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


	/**
	 * @return the assembly timestamp in [ns]
	 */
	public long gettAssembly()
	{
		return tAssembly;
	}
}
