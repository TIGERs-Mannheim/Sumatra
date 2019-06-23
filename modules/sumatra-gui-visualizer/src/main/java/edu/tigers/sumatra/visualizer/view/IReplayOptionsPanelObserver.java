/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

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
