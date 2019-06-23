/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log;

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
