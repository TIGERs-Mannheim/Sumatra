/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.06.2013
 * Author(s): JulianT
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals;

/**
 * Observer interface for the timeSlider of the statistic view
 * 
 * @author JulianT
 * 
 */
public interface ITimeSliderObserver
{
	/**
	 * Function to be called on change
	 * 
	 */
	void onTimeSlide();
	
	
	/**
	 * Function to be called to load new statistics file
	 * @param filename
	 * 
	 */
	void onFileLoad(String filename);
}
