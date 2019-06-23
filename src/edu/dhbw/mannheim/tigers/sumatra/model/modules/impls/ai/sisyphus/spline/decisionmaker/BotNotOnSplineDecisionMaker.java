/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * checks if the Bot left the spline
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class BotNotOnSplineDecisionMaker implements IUpdateSplineDecisionMaker
{
	private static final Logger	log										= Logger.getLogger(BotNotOnSplineDecisionMaker.class
																								.getName());
	
	@Configurable(comment = "how much is the bot allowed to differ vertical from the current spline until a new spline is calculated[mm]")
	private static float				allowedDistanceVerticalToSpline	= 50;
	@Configurable(comment = "how much is the bot allowed to differ from the current spline until a new spline is calculated[mm]")
	private static float				allowedDistanceToSpline				= 200;
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline,
			final List<IDrawableShape> shapes)
	{
		float time = oldSpline.getCurrentTime();
		IVector2 pointOnSpline = oldSpline.getPositionByTime(time).getXYVector();
		IVector2 velocityOnSpline = oldSpline.getVelocityByTime(time).getXYVector();
		// float angleOnSpline = oldPath.getHermiteSpline().getRotationTrajectory().getPosition(time);
		
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
}
