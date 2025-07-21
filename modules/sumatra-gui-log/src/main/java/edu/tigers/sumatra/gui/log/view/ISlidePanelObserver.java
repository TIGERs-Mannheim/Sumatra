/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.log.view;

import org.apache.logging.log4j.Level;


/**
 * Observes changes in log level slider.
 */
public interface ISlidePanelObserver
{
	/**
	 * @param level
	 */
	void onLevelChanged(Level level);
}
