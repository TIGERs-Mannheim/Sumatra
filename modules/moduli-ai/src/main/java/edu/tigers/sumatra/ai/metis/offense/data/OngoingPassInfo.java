/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.data;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;


/**
 * Data class to hold the pass target and the duration of the ongoing pass.
 *
 * @author Dominik Engelhardt
 */
public class OngoingPassInfo
{
	private IPassTarget passTarget;
	private double timeSinceStart;
	
	
	/**
	 * @param passTarget
	 * @param timeSinceStart
	 */
	public OngoingPassInfo(final IPassTarget passTarget, final double timeSinceStart)
	{
		this.passTarget = passTarget;
		this.timeSinceStart = timeSinceStart;
	}
	
	
	public IPassTarget getPassTarget()
	{
		return passTarget;
	}
	
	
	public double getTimeSinceStart()
	{
		return timeSinceStart;
	}
}
