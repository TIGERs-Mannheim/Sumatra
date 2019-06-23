/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee;

import edu.tigers.autoreferee.module.AutoRefState;


/**
 * @author Lukas Magel
 */
public interface IAutoRefStateObserver
{
	/**
	 * @param state
	 */
	void onAutoRefStateChanged(AutoRefState state);
	
	
	/**
	 * @param frame
	 */
	void onNewAutoRefFrame(IAutoRefFrame frame);
}
