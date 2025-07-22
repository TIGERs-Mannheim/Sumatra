/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.replay.view;

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
