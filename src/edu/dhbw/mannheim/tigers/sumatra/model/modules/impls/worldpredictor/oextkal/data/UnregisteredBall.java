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

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamBall;

public class UnregisteredBall
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public double 	  oldTimestamp;
	public double 	  newTimestamp;
	public WPCamBall visionBall;
	public int 		  count;


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public UnregisteredBall(double time, CamBall visionBall) {
		this.oldTimestamp = time;
		this.newTimestamp = time;
		this.visionBall 	= new WPCamBall(visionBall);
		count = 0;
	}
	
	public UnregisteredBall(double time, WPCamBall visionBall) {
		this.oldTimestamp = time;
		this.newTimestamp = time;
		this.visionBall 	= visionBall;
		count = 0;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addBall(double time, CamBall visionBall) {
		this.newTimestamp = time;
		this.visionBall 	= new WPCamBall(visionBall);
		count++;
	}
	
	public void addBall(double time, WPCamBall visionBall) {
		this.newTimestamp = time;
		this.visionBall 	= visionBall;
		count++;
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
