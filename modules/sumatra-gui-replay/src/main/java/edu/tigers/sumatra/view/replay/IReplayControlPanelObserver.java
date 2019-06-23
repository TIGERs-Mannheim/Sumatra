/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.replay;

/**
 * Observer for {@link ReplayControlPanel}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IReplayControlPanelObserver
{
	/**
	 * @param speed
	 */
	void onSetSpeed(double speed);
	
	
	/**
	 * @param playing true if playing, false if paused
	 */
	void onPlayPause(boolean playing);
	
	
	/**
	 * @param time
	 */
	void onChangeAbsoluteTime(long time);
	
	
	/**
	 * @param relTime
	 */
	void onChangeRelativeTime(long relTime);
	
	
	/**
	 * jump to next frame
	 */
	void onNextFrame();
	
	
	/**
	 * jump to prev frame
	 */
	void onPreviousFrame();
	
	
	/**
	 * @param enable
	 */
	void onSetSkipStop(final boolean enable);

	/**
	 * Enable ball placement skipping
	 * @param enable
	 */
	void onSetSkipBallPlacement(final boolean enable);
	
	
	/**
	 * @param enable
	 */
	void onSearchKickoff(boolean enable);
	
	
	/**
	 * save snapshot to file
	 */
	void onSnapshot();
	
	
	/**
	 * Copy snapshot to clipboard
	 */
	void onCopySnapshot();
	
}
