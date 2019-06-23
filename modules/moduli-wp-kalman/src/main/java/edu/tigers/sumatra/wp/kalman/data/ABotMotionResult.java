/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABotMotionResult extends AMotionResult
{
	
	/** */
	public final double orientation;
	
	
	/**
	 * @param x
	 * @param y
	 * @param orientation
	 * @param confidence
	 * @param onCam
	 */
	public ABotMotionResult(final double x, final double y, final double orientation, final double confidence,
			final boolean onCam)
	{
		super(x, y, confidence, onCam);
		this.orientation = orientation;
	}
	
	
	/**
	 * @param timestamp
	 * @param botId
	 * @return
	 */
	public abstract ITrackedBot motionToTrackedBot(long timestamp, final BotID botId);
	
}
