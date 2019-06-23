/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.sim.stopcrit;

import java.util.concurrent.TimeUnit;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimStopTimeCrit extends ASimStopCriterion
{
	private final long timeout;
	
	
	/**
	 * @param timeout in ms
	 */
	public SimStopTimeCrit(final long timeout)
	{
		this.timeout = timeout;
	}
	
	
	@Override
	protected boolean checkStopSimulation()
	{
		long runtimeNs = getRuntime(getLatestFrame().getWorldFrame().getTimestamp());
		return (TimeUnit.NANOSECONDS.toMillis(runtimeNs) >= timeout);
	}
	
	
	/**
	 * @return the timeout
	 */
	public final long getTimeout()
	{
		return timeout;
	}
}
