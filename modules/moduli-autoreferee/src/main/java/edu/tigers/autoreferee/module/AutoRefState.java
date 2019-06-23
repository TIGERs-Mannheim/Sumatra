/* 
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2016
 * Author(s): "Lukas Magel"
 *
 * *********************************************************
 */
package edu.tigers.autoreferee.module;

/**
 * @author Lukas Magel
 */
public enum AutoRefState
{
	/**  */
	RUNNING,
	/** Right before running, but only when the engine is first started */
	STARTED,
	/**  */
	PAUSED,
	/**  */
	STARTING,
	/**  */
	STOPPED
}