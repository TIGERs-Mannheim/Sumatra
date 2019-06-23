/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

/**
 * FieldPanel observer interface.
 * 
 * @author AndreR
 */
public interface IOptionsPanelObserver
{
	/**
	 * @param string
	 * @param isSelected
	 */
	void onCheckboxClick(String string, boolean isSelected);
	
	
	/**
	 * @param option
	 * @param state
	 */
	void onActionFired(EVisualizerOptions option, boolean state);
}
