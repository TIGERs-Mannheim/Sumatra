/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

/**
 */
public class PingStats
{
	/** */
	public PingStats()
	{
		avgDelay = 0;
		minDelay = Float.MAX_VALUE;
		maxDelay = 0;
		lostPings = 0;
	}
	
	/** Average Delay */
	public float	avgDelay;
	/** Minimum Delay */
	public float	minDelay;
	/** Maximum Delay */
	public float	maxDelay;
	/** Lost pings per second */
	public int		lostPings;
}
