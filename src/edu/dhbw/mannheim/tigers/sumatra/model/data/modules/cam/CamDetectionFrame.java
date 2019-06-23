/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam;

import java.util.Collections;
import java.util.List;


/**
 * This class contains every information a
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionFrame} has to offer about
 * the current situation on the field
 * 
 * @author Gero
 */
public class CamDetectionFrame
{
	/** time-stamp in System.nanotime() */
	private final long				tCapture;
	
	/** time-stamp in System.nanotime() */
	private final long				tSent;
	
	/** time-stamp in System.nanotime() */
	private final long				tReceived;
	
	/** ID 0 or 1 */
	private final int					cameraId;
	
	/** independent frame number, continuous */
	private final long				frameNumber;
	private final List<CamBall>	balls;
	private final List<CamRobot>	robotsYellow;
	private final List<CamRobot>	robotsBlue;
	
	
	/**
	 * @param tCapture
	 * @param tSent
	 * @param tReceived
	 * @param cameraId
	 * @param frameNumber
	 * @param balls
	 * @param yellowBots
	 * @param blueBots
	 */
	public CamDetectionFrame(final long tCapture, final long tSent, final long tReceived, final int cameraId,
			final long frameNumber,
			final List<CamBall> balls, final List<CamRobot> yellowBots, final List<CamRobot> blueBots)
	{
		// Fields
		this.tCapture = tCapture;
		this.tSent = tSent;
		this.tReceived = tReceived;
		this.cameraId = cameraId;
		this.frameNumber = frameNumber;
		
		// Collections
		this.balls = Collections.unmodifiableList(balls);
		robotsYellow = Collections.unmodifiableList(yellowBots);
		robotsBlue = Collections.unmodifiableList(blueBots);
	}
	
	
	@Override
	public String toString()
	{
		return "tCapture:" + gettCapture() + "/tSend:" + gettSent() + "/tReceived:" + gettReceived() + "/cameraId:"
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
	 * @return the tReceived
	 */
	public long gettReceived()
	{
		return tReceived;
	}
	
	
	/**
	 * @return the cameraId
	 */
	public int getCameraId()
	{
		return cameraId;
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
		return balls;
	}
	
	
	/**
	 * @return the robotsYellow
	 */
	public List<CamRobot> getRobotsYellow()
	{
		return robotsYellow;
	}
	
	
	/**
	 * @return the robotsBlue
	 */
	public List<CamRobot> getRobotsBlue()
	{
		return robotsBlue;
	}
}
