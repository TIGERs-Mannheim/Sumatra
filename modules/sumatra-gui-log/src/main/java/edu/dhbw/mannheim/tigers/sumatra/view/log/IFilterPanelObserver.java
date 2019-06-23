/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log;

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
