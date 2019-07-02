/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.thread;

/**
 * Observer for the watchdog.
 * 
 * @author AndreR
 */
public interface IWatchdogObserver
{
	/**
	 *
	 */
	void onWatchdogTimeout();
}
