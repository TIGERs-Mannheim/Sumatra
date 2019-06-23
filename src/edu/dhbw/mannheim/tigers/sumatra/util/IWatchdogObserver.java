/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

/**
 * Observer for the watchdog.
 * 
 * @author AndreR
 * 
 */
public interface IWatchdogObserver
{
	public void onWatchdogTimeout();
}
