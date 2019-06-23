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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamRobot;


/**
 *
 */
public class UnregisteredBot
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** */
	public double		oldTimestamp;
	/** */
	public double		newTimestamp;
	/** */
	public WPCamBot	visionBot;
	/** */
	public int			count;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param time
	 * @param visionbot
	 */
	public UnregisteredBot(double time, CamRobot visionbot)
	{
		oldTimestamp = time;
		newTimestamp = time;
		visionBot = new WPCamBot(visionbot);
		count = 0;
	}
	
	
	/**
	 * 
	 * @param time
	 * @param visionbot
	 */
	public UnregisteredBot(double time, WPCamBot visionbot)
	{
		oldTimestamp = time;
		newTimestamp = time;
		visionBot = visionbot;
		count = 0;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param time
	 * @param visionbot
	 */
	public void addBot(double time, CamRobot visionbot)
	{
		newTimestamp = time;
		visionBot = new WPCamBot(visionbot);
		count++;
	}
	
	
	/**
	 * 
	 * @param time
	 * @param visionbot
	 */
	public void addBot(double time, WPCamBot visionbot)
	{
		newTimestamp = time;
		visionBot = visionbot;
		count++;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
