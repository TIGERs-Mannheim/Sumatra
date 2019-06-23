/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import org.apache.log4j.Level;


/**
 * Observes changes in log level slider.
 * 
 * @author AndreR
 */
public interface ISlidePanelObserver
{
	/**
	 * @param level
	 */
	void onLevelChanged(Level level);
}
