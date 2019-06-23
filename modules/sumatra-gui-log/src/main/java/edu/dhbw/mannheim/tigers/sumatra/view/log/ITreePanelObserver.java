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
