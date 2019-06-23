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
	 */
	void onNextFrame();
	
	
	/**
	 * 
	 */
	void onPreviousFrame();
	
	
	/**
	 * @param enable
	 */
	void onSetSkipStop(final boolean enable);
	
	
	/**
	 * @param enable
	 */
	void onSearchKickoff(boolean enable);
	
	
	/**
	 * @param selected
	 */
	void onRunCurrentAi(boolean selected);
	
	
	/**
	 * 
	 */
	void onSnapshot();
	
}
