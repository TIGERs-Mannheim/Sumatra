/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

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
