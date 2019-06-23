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

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;


/**
 * Takes the origin frame and modifies, if necessary
 * 
 * @author Birgit
 * 
 */
public class FramePacker
{
	private CamDetectionFrame	oldFrame	= null;
	private CamDetectionFrame	newFrame	= null;
	
	private CamBall				newBall	= null;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public FramePacker(CamDetectionFrame frame)
	{
		oldFrame = frame;
	}
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
	
	public CamDetectionFrame getMaybeCorrectedFrame()
	{
		if (newFrame != null)
		{
			return newFrame;
		} else
		{
			return oldFrame;
		}
	}
	

	public CamDetectionFrame getOldFrame()
	{
		return oldFrame;
	}
	

	public List<CamRobot> getBotList()
	{
		List<CamRobot> bots = new ArrayList<CamRobot>();
		for (CamRobot bot : oldFrame.robotsTigers)
			bots.add(bot);
		for (CamRobot bot : oldFrame.robotsEnemies)
			bots.add(bot);
		
		return bots;
	}
	

	public CamBall getBall()
	{
		return oldFrame.balls.get(0);
	}
	

	public void setNewCamBall(float x, float y, float z)
	{
		if (newBall == null)
		{
			CamBall oldBall = oldFrame.balls.get(0);
			
			newBall = new CamBall(oldBall.confidence, oldBall.area, x, y, z, oldBall.pixelX, oldBall.pixelY);
			

			List<CamBall> newBalls = new ArrayList<CamBall>();
			newBalls.add(newBall);
			
			newFrame = new CamDetectionFrame(oldFrame.tCapture, oldFrame.tSent, oldFrame.tReceived, oldFrame.cameraId,
					oldFrame.frameNumber, oldFrame.fps, newBalls, oldFrame.robotsTigers, oldFrame.robotsEnemies);
		} else
		{
			throw new IllegalArgumentException("WP:FlyBall:FramePacker: New Ball is already set");
		}
	}
	

	public void print(final double height, final double distance, final boolean fly)
	{
		if (newFrame == null)
		{
			WriteFlyData.getInstance().addDataSet(oldFrame.balls.get(0).pos.x, oldFrame.balls.get(0).pos.y,
					oldFrame.balls.get(0).pos.x, oldFrame.balls.get(0).pos.y, height, 0, 0, fly);
		} else
		{
			WriteFlyData.getInstance().addDataSet(oldFrame.balls.get(0).pos.x, oldFrame.balls.get(0).pos.y,
					newFrame.balls.get(0).pos.x, newFrame.balls.get(0).pos.y, height, newFrame.balls.get(0).pos.z, distance,
					fly);
			
			if (Def.debugFlyHeight)
			{
				System.out.println("---Extern: " + newFrame.balls.get(0).pos.z);
			}
		}
	}
}
