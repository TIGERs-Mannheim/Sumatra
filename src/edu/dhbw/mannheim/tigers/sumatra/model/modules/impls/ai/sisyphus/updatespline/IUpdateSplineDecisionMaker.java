/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * ever pathplanninginterval ms a new path is calculated.
 * However, the bots drive much better if the path is not updated that often. So it needs to be decided if the path and
 * the spline will be updated or not.
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public interface IUpdateSplineDecisionMaker
{
	/**
	 * every DecisionMaker can decide whether the path should be updated or not
	 * 
	 * @param localPathFinderInput
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath);
}
