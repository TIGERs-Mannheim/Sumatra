/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.replay;

/**
 * Observer for updating GUI slider position
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IReplayPositionObserver
{
	/**
	 * @param position
	 */
	void onPositionChanged(long position);
}
