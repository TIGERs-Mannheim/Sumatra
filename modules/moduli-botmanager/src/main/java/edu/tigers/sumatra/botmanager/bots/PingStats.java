/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots;

/**
 * Ping statistics.
 */
@SuppressWarnings("squid:ClassVariableVisibilityCheck")
public class PingStats
{
	/** Average Delay */
	public double	avgDelay;
	/** Minimum Delay */
	public double	minDelay;
	/** Maximum Delay */
	public double	maxDelay;
	/** Lost pings per second */
	public int		lostPings;
	
	
	/**
	 * Default constrcutor.
	 */
	public PingStats()
	{
		avgDelay = 0;
		minDelay = Double.MAX_VALUE;
		maxDelay = 0;
		lostPings = 0;
	}
}
