/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter.humanref;

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
	public void setNewWorldFrame(WorldFrameWrapper frame);
	
	
	/**
	 * @param frame
	 */
	public void setNewRefFrame(IAutoRefFrame frame);
	
	
	/**
	 * @param entry
	 */
	public void setNewGameLogEntry(GameLogEntry entry);
	
	
	/**
	 * 
	 */
	public void paintField();
	
	
	/**
	 * 
	 */
	public void start();
	
	
	/**
	 * 
	 */
	public void stop();
}
