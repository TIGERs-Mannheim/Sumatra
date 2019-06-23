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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.updateSplineDecision.IUpdateSplineDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * checks if the Bot left the spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class BotNotOnSplineDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(BotNotOnSplineDecisionMaker.class.getName());
	
	
	// private static final float ANGLE_ON_SPLINE_TOL = 0.2f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath)
	{
		float time = localPathFinderInput.getCurrentTimeOnSpline();
		IVector2 pointOnSpline = oldPath.getHermiteSpline().getPositionTrajectory().getValueByTime(time);
		// float angleOnSpline = oldPath.getHermiteSpline().getRotationTrajectory().getPosition(time);
		
		pointOnSpline = DistanceUnit.METERS.toMillimeters(pointOnSpline);
		TrackedTigerBot bot = localPathFinderInput.getwFrame().getTiger(localPathFinderInput.getBotId());
		IVector2 botPos = bot.getPos();
		
		boolean isBotOnSpline = pointOnSpline.equals(botPos, AIConfig.getOptimization().getAllowedDistanceToSpline());
		boolean isRotationCorrect = true; // SumatraMath.isEqual(angleOnSpline, bot.getAngle(), ANGLE_ON_SPLINE_TOL);
		if (isBotOnSpline && isRotationCorrect)
		{
			
			return EDecision.NO_VIOLATION;
		}
		
		log.trace("New Spline - Bot is not on Spline: " + time + ": " + pointOnSpline + " " + botPos
				+ " (isBotOnSpline: " + isBotOnSpline + " / isRotationCorrect: " + isRotationCorrect + ")");
		return EDecision.VIOLATION;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
