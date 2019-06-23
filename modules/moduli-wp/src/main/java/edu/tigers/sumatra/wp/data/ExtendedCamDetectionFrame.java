/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;


/**
 * This frame _might_ contain data from multiple camera frames
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class ExtendedCamDetectionFrame extends CamDetectionFrame
{
	private final CamBall ball;
	
	
	@SuppressWarnings("unused")
	private ExtendedCamDetectionFrame()
	{
		super();
		ball = null;
	}
	
	
	/**
	 * @param frame
	 * @param ball
	 */
	public ExtendedCamDetectionFrame(final CamDetectionFrame frame, final CamBall ball)
	{
		super(frame);
		this.ball = ball;
	}
	
	
	/**
	 * @param frameId
	 * @param frame
	 * @param balls
	 * @param yellowBots
	 * @param blueBots
	 * @param ball
	 */
	public ExtendedCamDetectionFrame(final long frameId, final CamDetectionFrame frame,
			final List<CamBall> balls, final List<CamRobot> yellowBots, final List<CamRobot> blueBots,
			final CamBall ball)
	{
		super(frame.gettCapture(), frame.gettSent(), frame.getCameraId(), frame.getCamFrameNumber(), frameId, balls,
				yellowBots,
				blueBots);
		this.ball = ball;
	}
	
	
	/**
	 * @return the ball
	 */
	public final CamBall getBall()
	{
		return ball;
	}
}
