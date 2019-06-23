/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;


/**
 * This frame _might_ contain data from multiple camera frames
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class MergedCamDetectionFrame
{
	/** independent frame number, continuous */
	private final long									frameNumber;
	private final List<CamBall>						balls;
	private final CamBall								ball;
	private final List<CamRobot>						robotsYellow;
	private final List<CamRobot>						robotsBlue;
	
	private static final MergedCamDetectionFrame	EMPTY_FRAME	= new MergedCamDetectionFrame();
	
	
	/**
	 * @param frameNumber
	 * @param balls
	 * @param robotsYellow
	 * @param robotsBlue
	 * @param ball
	 */
	public MergedCamDetectionFrame(final long frameNumber, final List<CamBall> balls, final List<CamRobot> robotsYellow,
			final List<CamRobot> robotsBlue, final CamBall ball)
	{
		this.frameNumber = frameNumber;
		this.balls = balls;
		this.robotsYellow = robotsYellow;
		this.robotsBlue = robotsBlue;
		this.ball = ball;
	}
	
	
	private MergedCamDetectionFrame()
	{
		frameNumber = -1;
		balls = new ArrayList<>(0);
		robotsBlue = new ArrayList<>(0);
		robotsYellow = new ArrayList<>(0);
		ball = CamBall.defaultInstance();
	}
	
	
	/**
	 * Get the default empty instance of this frame
	 * 
	 * @return
	 */
	public static MergedCamDetectionFrame emptyFrame()
	{
		return EMPTY_FRAME;
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
	 * @return the ball
	 */
	public final CamBall getBall()
	{
		return ball;
	}
}
