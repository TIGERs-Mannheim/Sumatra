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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
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
	private static final Logger	log										= Logger.getLogger(BotNotOnSplineDecisionMaker.class
																								.getName());
	
	@Configurable(comment = "how much is the bot allowed to differ vertical from the current spline until a new spline is calculated[mm]")
	private static float				allowedDistanceVerticalToSpline	= 50;
	@Configurable(comment = "how much is the bot allowed to differ from the current spline until a new spline is calculated[mm]")
	private static float				allowedDistanceToSpline				= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public EDecision check(PathFinderInput localPathFinderInput, Path oldPath, Path newPath)
	{
		float time = oldPath.getHermiteSpline().getTrajectoryTime();
		IVector2 pointOnSpline = oldPath.getHermiteSpline().getPositionTrajectory().getValueByTime(time);
		IVector2 velocityOnSpline = oldPath.getHermiteSpline().getPositionTrajectory().getVelocity(time);
		// float angleOnSpline = oldPath.getHermiteSpline().getRotationTrajectory().getPosition(time);
		
		pointOnSpline = DistanceUnit.METERS.toMillimeters(pointOnSpline);
		TrackedTigerBot bot = localPathFinderInput.getFieldInfo().getwFrame().getBot(localPathFinderInput.getBotId());
		IVector2 botPos = bot.getPos();
		
		boolean isBotOnSpline = pointOnSpline.equals(botPos, allowedDistanceToSpline);
		boolean isBotVerticalOnSpline = true;
		if (!velocityOnSpline.isZeroVector())
		{
			Line tangent = new Line(pointOnSpline, velocityOnSpline);
			isBotVerticalOnSpline = GeoMath.distancePL(botPos, tangent) < allowedDistanceVerticalToSpline;
		}
		boolean isRotationCorrect = true; // SumatraMath.isEqual(angleOnSpline, bot.getAngle(), ANGLE_ON_SPLINE_TOL);
		if (isBotOnSpline && isBotVerticalOnSpline && isRotationCorrect)
		{
			
			return EDecision.NO_VIOLATION;
		}
		
		log.trace("New Spline - Bot is not on Spline: " + time + ": " + pointOnSpline + " " + botPos
				+ " (isBotOnSpline: " + isBotOnSpline + " / isBotVerticalOnSpline: " + isBotVerticalOnSpline
				+ " / isRotationCorrect: " + isRotationCorrect + ")");
		return EDecision.VIOLATION;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
