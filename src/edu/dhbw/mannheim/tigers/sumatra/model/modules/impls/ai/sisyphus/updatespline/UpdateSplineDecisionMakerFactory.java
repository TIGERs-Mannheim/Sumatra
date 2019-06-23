/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updatespline;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * creates all decision makers
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class UpdateSplineDecisionMakerFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<IUpdateSplineDecisionMaker>	decisionMakers	= new ArrayList<IUpdateSplineDecisionMaker>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public UpdateSplineDecisionMakerFactory()
	{
		// decisionMakers.add(new BotNotOnSplineDecisionMaker());
		// decisionMakers.add(new CollisionDetectionDecisionMaker());
		decisionMakers.add(new NewPathShorterDecisionMaker());
		// decisionMakers.add(new SplineEndGoalNotReachedDecisionMaker());
		decisionMakers.add(new DestinationChangedDecisionMaker());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * check if a new spline should be used
	 * 
	 * @param localPathFinderInput
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public EDecision check(final PathFinderInput localPathFinderInput, final Path oldPath, final Path newPath)
	{
		EDecision hardestDecision = EDecision.NO_VIOLATION;
		for (IUpdateSplineDecisionMaker decisionMaker : decisionMakers)
		{
			EDecision decision = decisionMaker.check(localPathFinderInput, oldPath, newPath);
			hardestDecision = decision.max(hardestDecision);
		}
		return hardestDecision;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
