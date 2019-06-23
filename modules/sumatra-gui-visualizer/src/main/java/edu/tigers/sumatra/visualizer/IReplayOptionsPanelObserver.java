/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

/**
 * GUI observer for replay options panel
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IReplayOptionsPanelObserver
{
	/**
	 * @param active
	 */
	void onRecord(boolean active);
	
	
	/**
	 * @param active
	 */
	void onSave(boolean active);
	
}
