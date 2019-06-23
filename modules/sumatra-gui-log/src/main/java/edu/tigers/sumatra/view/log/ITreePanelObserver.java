/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import java.util.List;


/**
 * Observes class filter changes.
 * 
 * @author AndreR
 */
public interface ITreePanelObserver
{
	/**
	 * @param classes
	 */
	void onNewClassList(List<String> classes);
}
