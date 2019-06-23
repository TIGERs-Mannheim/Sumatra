/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.sumatra.components.IEnumPanel;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefMainPanel
{
	/**
	 * @return
	 */
	IStartStopPanel getStartStopPanel();
	
	
	/**
	 * @return
	 */
	IActiveEnginePanel getEnginePanel();
	
	
	/**
	 * @return
	 */
	IEnumPanel<EGameEventDetectorType> getGameEventDetectorPanel();
	
	
	IEnumPanel<EGameEvent> getGameEventPanel();
}
