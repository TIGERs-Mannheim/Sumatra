/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main.toolbar;


/**
 * MainFrame observer
 * 
 * @author AndreR
 * 
 */
public interface IToolbarObserver
{
	/**
	 *
	 */
	void onStartStopModules();
	
	
	/**
	 *
	 */
	void onEmergencyStop();
}
