/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import edu.tigers.sumatra.shapes.path.IPath;


/**
 * Interface for all classes that need to know about new pathes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPathConsumer
{
	/**
	 * A new path is available
	 * 
	 * @param path
	 */
	void onNewPath(IPath path);
	
	
	/**
	 * This is called each time, the PP thread has calculated a new path
	 * (quite frequently). Only execute paths from {@link IPathConsumer#onNewPath(IPath)}!
	 * 
	 * @param path
	 */
	void onPotentialNewPath(IPath path);
}
