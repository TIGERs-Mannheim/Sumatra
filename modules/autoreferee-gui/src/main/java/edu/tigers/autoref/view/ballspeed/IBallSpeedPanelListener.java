/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.ballspeed;

/**
 * @author "Lukas Magel"
 */
public interface IBallSpeedPanelListener
{
	/**
	 * 
	 */
	void pauseButtonPressed();
	
	
	/**
	 * 
	 */
	void resumeButtonPressed();
	
	
	/**
	 * @param value
	 */
	void stopChartValueChanged(boolean value);
	
	
	/**
	 * @param value new value in [s]
	 */
	void timeRangeSliderValueChanged(int value);
}
