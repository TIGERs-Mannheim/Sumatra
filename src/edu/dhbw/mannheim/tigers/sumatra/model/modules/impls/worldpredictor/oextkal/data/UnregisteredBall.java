/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.10.2010
 * Author(s): Yakisoba
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamBall;


/**
 *
 */
public class UnregisteredBall
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public double		oldTimestamp;
	/** */
	public double		newTimestamp;
	/** */
	public WPCamBall	visionBall;
	/** */
	public int			count;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param time
	 * @param visionBall
	 */
	public UnregisteredBall(double time, CamBall visionBall)
	{
		oldTimestamp = time;
		newTimestamp = time;
		this.visionBall = new WPCamBall(visionBall);
		count = 0;
	}
	
	
	/**
	 * 
	 * @param time
	 * @param visionBall
	 */
	public UnregisteredBall(double time, WPCamBall visionBall)
	{
		oldTimestamp = time;
		newTimestamp = time;
		this.visionBall = visionBall;
		count = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param time
	 * @param visionBall
	 */
	public void addBall(double time, CamBall visionBall)
	{
		newTimestamp = time;
		this.visionBall = new WPCamBall(visionBall);
		count++;
	}
	
	
	/**
	 * 
	 * @param time
	 * @param visionBall
	 */
	public void addBall(double time, WPCamBall visionBall)
	{
		newTimestamp = time;
		this.visionBall = visionBall;
		count++;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
