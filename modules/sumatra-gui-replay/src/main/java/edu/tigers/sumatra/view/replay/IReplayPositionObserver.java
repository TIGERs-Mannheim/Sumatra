/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.replay;

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
