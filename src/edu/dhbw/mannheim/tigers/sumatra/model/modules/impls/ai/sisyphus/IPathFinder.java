/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * every path finder needs to implement this interface
 * 
 * @author dirk
 * 
 */
public interface IPathFinder
{
	/**
	 * Returns a path from bot to target.
	 * 
	 * old param: 'restrictedArea' area the bot is not allowed to enter this area; if there is no such area: use null; if
	 * current botpos
	 * or target are within restrictedArea it is set null automatically
	 * 
	 * @param pathFinderInput
	 * 
	 * @return
	 */
	Path calcPath(PathFinderInput pathFinderInput);
	
	
}
