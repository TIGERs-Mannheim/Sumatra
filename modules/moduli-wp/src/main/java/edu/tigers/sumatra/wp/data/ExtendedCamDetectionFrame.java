/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;


/**
 * This frame _might_ contain data from multiple camera frames
 */
public class ExtendedCamDetectionFrame extends CamDetectionFrame
{
	private final CamBall ball;


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
	 * @return the ball
	 */
	public final CamBall getBall()
	{
		return ball;
	}
}
