/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.main;

import edu.tigers.autoreferee.engine.events.IGameEventDetector.EGameEventDetectorType;
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
	IEnumPanel<EGameEventDetectorType> getEventPanel();
}
