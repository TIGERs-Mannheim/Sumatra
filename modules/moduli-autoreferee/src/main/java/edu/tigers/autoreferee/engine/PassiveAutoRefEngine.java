/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import edu.tigers.autoreferee.IAutoRefFrame;


/**
 * @author "Lukas Magel"
 */
public class PassiveAutoRefEngine extends AbstractAutoRefEngine
{
	
	@Override
	public void stop()
	{
	}
	
	
	@Override
	public AutoRefMode getMode()
	{
		return AutoRefMode.PASSIVE;
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (engineState == EEngineState.PAUSED)
		{
			return;
		}
		super.process(frame);
		logGameEvents(getGameEvents(frame));
	}
}
