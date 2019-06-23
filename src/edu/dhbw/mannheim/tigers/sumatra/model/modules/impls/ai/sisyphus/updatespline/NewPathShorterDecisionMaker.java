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

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * checks if the new spline is significant shorter than the old one (i.e. a bot who blocked the direct way moved out of
 * the direct way)
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class NewPathShorterDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log							= Logger
																					.getLogger(NewPathShorterDecisionMaker.class.getName());
	
	
	@Configurable(comment = "if the new calculated path is faster by this amount of time it will replace the old path [s]")
	private static float				useShorterPathIfFaster	= 1.5f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath)
	{
		float newPathTotalTime = newPath.getHermiteSpline().getPositionTrajectory().getTotalTime();
		float curTimeOnSpline = oldPath.getHermiteSpline().getTrajectoryTime();
		float oldPathRemainingTime = oldPath.getHermiteSpline().getPositionTrajectory().getTotalTime() - curTimeOnSpline;
		float threshold = useShorterPathIfFaster;
		if ((newPathTotalTime < (oldPathRemainingTime - threshold)) && !newPath.isRambo())
		{
			log.trace("New Spline - Shorter Path found: " + newPathTotalTime + " < (" + oldPathRemainingTime + " - "
					+ useShorterPathIfFaster + "s)");
			return EDecision.OPTIMIZATION_FOUND;
		}
		return EDecision.NO_VIOLATION;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
