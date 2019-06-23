/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 17, 2011
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;


/**
 * Takes the origin frame and modifies, if necessary
 * 
 * @author Birgit
 * 
 */
public class FramePacker
{
	private static final Logger	log		= Logger.getLogger(FramePacker.class.getName());
	private CamDetectionFrame		oldFrame	= null;
	private CamDetectionFrame		newFrame	= null;
	
	private CamBall					newBall	= null;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param frame
	 */
	public FramePacker(CamDetectionFrame frame)
	{
		oldFrame = frame;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param frame
	 */
	public void setNewFrame(CamDetectionFrame frame)
	{
		if (newFrame == null)
		{
			newFrame = frame;
		} else
		{
			throw new IllegalArgumentException("WP:FlyBall:FramePacker: New Frame is already set");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public CamDetectionFrame getMaybeCorrectedFrame()
	{
		if (newFrame != null)
		{
			return newFrame;
		}
		return oldFrame;
	}
	
	
	/**
	 * @return
	 */
	public CamDetectionFrame getOldFrame()
	{
		return oldFrame;
	}
	
	
	/**
	 * @return
	 */
	public List<CamRobot> getBotList()
	{
		final List<CamRobot> bots = new ArrayList<CamRobot>(oldFrame.robotsYellow);
		bots.addAll(oldFrame.robotsBlue);
		return bots;
	}
	
	
	/**
	 * @return
	 */
	public CamBall getBall()
	{
		return oldFrame.balls.get(0);
	}
	
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setNewCamBall(float x, float y, float z)
	{
		if (newBall == null)
		{
			final CamBall oldBall = oldFrame.balls.get(0);
			
			newBall = new CamBall(oldBall.confidence, oldBall.area, x, y, z, oldBall.pixelX, oldBall.pixelY);
			
			
			final List<CamBall> newBalls = new ArrayList<CamBall>();
			newBalls.add(newBall);
			
			newFrame = new CamDetectionFrame(oldFrame.tCapture, oldFrame.tSent, oldFrame.tReceived, oldFrame.cameraId,
					oldFrame.frameNumber, oldFrame.fps, newBalls, oldFrame.robotsYellow, oldFrame.robotsBlue,
					oldFrame.teamProps);
		} else
		{
			throw new IllegalArgumentException("WP:FlyBall:FramePacker: New Ball is already set");
		}
	}
	
	
	/**
	 * @param height
	 * @param distance
	 * @param fly
	 */
	public void print(final double height, final double distance, final boolean fly)
	{
		if (newFrame == null)
		{
			WriteFlyData.getInstance().addDataSet(oldFrame.balls.get(0).pos.x(), oldFrame.balls.get(0).pos.y(),
					oldFrame.balls.get(0).pos.x(), oldFrame.balls.get(0).pos.y(), height, 0, 0, fly);
		} else
		{
			WriteFlyData.getInstance().addDataSet(oldFrame.balls.get(0).pos.x(), oldFrame.balls.get(0).pos.y(),
					newFrame.balls.get(0).pos.x(), newFrame.balls.get(0).pos.y(), height, newFrame.balls.get(0).pos.z(),
					distance, fly);
			
			if (Def.DEBUG_FLY_HEIGHT)
			{
				log.debug("---Extern: " + newFrame.balls.get(0).pos.z());
			}
		}
	}
}
