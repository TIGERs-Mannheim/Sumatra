/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.List;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.geometry.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CurrentBallDetector
{
	
	private CamBall		lastSeenBall		= new CamBall();
	private final Object	lastSeenBallSync	= new Object();
	
	
	/**
	 * @param balls
	 * @return
	 */
	public CamBall findCurrentBall(final List<CamBall> balls)
	{
		synchronized (lastSeenBallSync)
		{
			double shortestDifference = Double.MAX_VALUE;
			CamBall selectedBall = null;
			for (CamBall ball : balls)
			{
				double diff = ball.getPos().subtractNew(lastSeenBall.getPos()).getLength2();
				if (diff < shortestDifference)
				{
					selectedBall = ball;
					shortestDifference = diff;
				}
			}
			if (selectedBall == null)
			{
				return lastSeenBall;
			}
			
			// note: dt may be negative if lastSeenBall is set manually
			double dt = (selectedBall.getTimestamp() - lastSeenBall.getTimestamp()) / 1e9;
			double dist = selectedBall.getPos().subtractNew(lastSeenBall.getPos()).getLength2() / 1000.0;
			double vel = dist / dt;
			if (vel > 15)
			{
				// high velocity, probably wrong ball
				return lastSeenBall;
			}
			
			double waitForNextBallTime = 0;
			if (!Geometry.getFieldWBorders().isPointInShape(selectedBall.getPos().getXYVector()) &&
					Geometry.getFieldWBorders().isPointInShape(lastSeenBall.getPos().getXYVector()))
			{
				waitForNextBallTime += 1;
			}
			
			if (selectedBall.getCameraId() != lastSeenBall.getCameraId())
			{
				waitForNextBallTime += 0.05;
			}
			
			if (dt < waitForNextBallTime)
			{
				return lastSeenBall;
			}
			
			lastSeenBall = selectedBall;
			return new CamBall(lastSeenBall);
		}
	}
	
	
	/**
	 * Reset the ball
	 */
	public void reset()
	{
		lastSeenBall = new CamBall();
	}
}
