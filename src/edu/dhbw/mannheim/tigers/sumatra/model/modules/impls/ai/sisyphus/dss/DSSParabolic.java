/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.11.2010
 * Author(s): torn8
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.dss;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;

/**
 * Container for a parabolic.
 * 
 * @author torn8
 * 
 */
public class DSSParabolic
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	public Vector2 x; // x-vector
	public Vector2 v; // v-vector
	public Vector2 a; // acc-vector
	public float[] time = new float[2]; // interval time [0]->starting time ; [1]-> end time of interval

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DSSParabolic(Vector2 x, Vector2 v, Vector2 a, float timeStart, float timeEnd)
	{
		this.x = x;
		this.v = v;
		this.a = a;
		this.time[0] = timeStart;
		this.time[1] = timeEnd;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
