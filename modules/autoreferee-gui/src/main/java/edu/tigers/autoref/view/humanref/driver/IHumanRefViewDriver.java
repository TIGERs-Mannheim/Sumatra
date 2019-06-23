/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.driver;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public interface IHumanRefViewDriver
{
	/**
	 * @param frame
	 */
	void setNewWorldFrame(WorldFrameWrapper frame);
	
	
	/**
	 * @param frame
	 */
	void setNewRefFrame(IAutoRefFrame frame);
	
	
	/**
	 * @param entry
	 */
	void setNewGameLogEntry(GameLogEntry entry);
	
	
	/**
	 * 
	 */
	void paintField();
	
	
	/**
	 * 
	 */
	void start();
	
	
	/**
	 * 
	 */
	void stop();
}
