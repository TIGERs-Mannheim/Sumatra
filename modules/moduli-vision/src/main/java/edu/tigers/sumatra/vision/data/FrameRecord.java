/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.vision.data;

import java.util.List;

import edu.tigers.sumatra.vision.tracker.BallTracker.MergedBall;


/**
 * Data container for a single frame with robots and a single ball.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class FrameRecord
{
	private final MergedBall					ball;
	private final List<FilteredVisionBot>	robots;
	
	
	/**
	 * @param ball
	 * @param robots
	 */
	public FrameRecord(final MergedBall ball, final List<FilteredVisionBot> robots)
	{
		super();
		this.ball = ball;
		this.robots = robots;
	}
	
	
	/**
	 * @return the ball
	 */
	public MergedBall getBall()
	{
		return ball;
	}
	
	
	/**
	 * @return the robots
	 */
	public List<FilteredVisionBot> getRobots()
	{
		return robots;
	}
}