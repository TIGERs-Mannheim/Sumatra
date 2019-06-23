/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import java.util.List;


/**
 * Observes user filter changes.
 * 
 * @author AndreR
 */
public interface IFilterPanelObserver
{
	/**
	 * @param allowed
	 */
	void onNewFilter(List<String> allowed);
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param freeze
	 */
	void onFreeze(boolean freeze);
}
