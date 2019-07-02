/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;


/**
 * The passive autoRef engine does not communicate with the game-controller.
 */
public class PassiveAutoRefEngine extends AutoRefEngine
{
	public PassiveAutoRefEngine(final Set<EGameEventDetectorType> activeDetectors)
	{
		super(activeDetectors);
	}
	
	
	@Override
	public void process(final IAutoRefFrame frame)
	{
		processEngine(frame).forEach(this::processGameEvent);
	}
}
