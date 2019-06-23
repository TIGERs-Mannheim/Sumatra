/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefState
{
	/**
	 * @param frame
	 * @param ctx
	 */
	void update(IAutoRefFrame frame, IAutoRefStateContext ctx);
	
	
	/**
	 * @param gameEvent
	 * @param ctx
	 * @return Returns true if the event was accepted by the engine
	 */
	boolean handleGameEvent(IGameEvent gameEvent, IAutoRefStateContext ctx);
	
	
	/**
	 * @return
	 */
	boolean canProceed();
	
	
	/**
	 * 
	 */
	void reset();
}
