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
	 */
	void onSlower();
	
	
	/**
	 */
	void onFaster();
	
	
	/**
	 * @param speed
	 */
	void onSetSpeed(int speed);
	
	
	/**
	 * @param playing true if playing, false if paused
	 */
	void onPlayStateChanged(boolean playing);
	
	
	/**
	 * @param position position within graphicsStore
	 */
	void onPositionChanged(int position);
	
	
	/**
	 * @param relPos
	 */
	void onChangeRelPos(int relPos);
	
	
	/**
	 * @param enable
	 */
	void setFrameByFrame(final boolean enable);
	
	
	/**
	 * @param enable
	 */
	void onSetSkipStop(final boolean enable);
	
	
	/**
	 * 
	 */
	void onNextFrame();
	
	
	/**
	 * 
	 */
	void onSearchKickoff();
	
	
	/**
	 * @param selected
	 */
	void onRunCurrentAi(boolean selected);
}
