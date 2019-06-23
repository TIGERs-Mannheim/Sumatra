/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.view.toolbar;


/**
 * Toolbar observer
 * 
 * @author AndreR
 */
public interface IToolbarObserver
{
	/**
	 *
	 */
	default void onStartStopModules()
	{
	}
	
	
	/**
	 *
	 */
	default void onEmergencyStop()
	{
	}
	
	
	/**
	 */
	default void onToggleRecord()
	{
	}
}
