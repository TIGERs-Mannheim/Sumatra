/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2012
 * Author(s): dirk
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.errt.TuneableParameter;
import edu.tigers.sumatra.shapes.path.IPath;


/**
 * every path finder needs to implement this interface
 * 
 * @author dirk
 */
public interface IPathFinder
{
	/**
	 * Returns a path from bot to target.
	 * old param: 'restrictedArea' area the bot is not allowed to enter this area; if there is no such area: use null; if
	 * current botpos
	 * or target are within restrictedArea it is set null automatically
	 * 
	 * @param pathFinderInput
	 * @return
	 */
	IPath calcPath(PathFinderInput pathFinderInput);
	
	
	/**
	 * @return
	 */
	TuneableParameter getAdjustableParams();
	
}
