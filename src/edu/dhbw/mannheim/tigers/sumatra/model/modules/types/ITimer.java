/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.FrameID;


/**
 * An interface that declares some methods for measurements purposes.
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITimer
{
	/**
	 * Stop timer of module
	 * 
	 * @param moduleName
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param frameId
	 */
	void stop(String moduleName, FrameID frameId);
	
	
	/**
	 * Start timer of module
	 * 
	 * @param moduleName
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param frameId
	 */
	void start(String moduleName, FrameID frameId);
	
	
	/**
	 * Tell observers to refresh timerInfo
	 * 
	 * @param frameId
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	void notifyNewTimerInfo(FrameID frameId);
}
