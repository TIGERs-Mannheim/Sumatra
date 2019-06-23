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

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamRobot;


public class UnregisteredBot
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public double 	 oldTimestamp;
	public double 	 newTimestamp;
	public WPCamBot visionBot;
	public int 		 count;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public UnregisteredBot(double time, CamRobot visionbot)
	{
		this.oldTimestamp   = time;
		this.newTimestamp   = time;
		this.visionBot 	  = new WPCamBot(visionbot);
		this.count		   = 0;
	}
	
	public UnregisteredBot(double time, WPCamBot visionbot)
	{
		this.oldTimestamp   = time;
		this.newTimestamp   = time;
		this.visionBot 	  = visionbot;
		this.count		     = 0;
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addBot(double time, CamRobot visionbot) {
		this.newTimestamp  = time;
		this.visionBot 	 = new WPCamBot(visionbot);
		count++;
	}
	
	public void addBot(double time, WPCamBot visionbot) {
		this.newTimestamp  = time;
		this.visionBot 	 = visionbot;
		count++;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
