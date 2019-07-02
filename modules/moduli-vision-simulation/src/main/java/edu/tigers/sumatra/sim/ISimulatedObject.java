/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim;


/**
 * An object that can be simulated
 */
interface ISimulatedObject
{
	/**
	 * Simulate object
	 * 
	 * @param dt the time step to simulate
	 */
	void dynamics(final double dt);
}
