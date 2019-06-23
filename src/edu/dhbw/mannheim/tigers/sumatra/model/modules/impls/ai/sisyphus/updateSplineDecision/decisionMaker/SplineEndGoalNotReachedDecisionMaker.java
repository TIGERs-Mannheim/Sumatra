/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.decisionMaker;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.IUpdateSplineDecisionMaker;


/**
 * checks if the bot has reached the end of the spline but the target is not reached
 * 
 * Tigerv2 bot should automtically drive to the target, for the other bots a new spline is needed
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class SplineEndGoalNotReachedDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(SplineEndGoalNotReachedDecisionMaker.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath)
	{
		// if (localPathFinderInput.getwFrame().getTiger(localPathFinderInput.getBotId()).getBotType() !=
		// EBotType.TIGER_V2)
		// {
		TrackedTigerBot bot = localPathFinderInput.getwFrame().getTiger(localPathFinderInput.getBotId());
		boolean isGoalReached = oldPath.getTarget().equals(bot.getPos(), AIConfig.getErrt().getTollerableTargetShift());
		boolean isSplineCompleted = localPathFinderInput.getCurrentTimeOnSpline() > oldPath.getHermiteSpline()
				.getPositionTrajectory().getTotalTime();
		boolean isTargetInPenArea = AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(oldPath.getTarget());
		
		if (isSplineCompleted && !isGoalReached && !isTargetInPenArea)
		{
			log.trace("New Spline - Spline end but goal is not reached yet: Goal not reached: " + oldPath.getTarget()
					+ " != " + bot.getPos() + " (" + AIConfig.getErrt().getTollerableTargetShift() + ")");
			return EDecision.ENFORCE;
		}
		return EDecision.NO_VIOLATION;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
