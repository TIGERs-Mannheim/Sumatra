/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.spline.decisionmaker;

import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.spline.EDecision;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


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
	private static double			allowedDistanceVerticalToSpline	= 50;
	@Configurable(comment = "how much is the bot allowed to differ from the current spline until a new spline is calculated[mm]")
	private static double			allowedDistanceToSpline				= 200;
																						
																						
	static
	{
		ConfigRegistration.registerClass("sisyphus", BotNotOnSplineDecisionMaker.class);
	}
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ITrajectory<IVector3> oldSpline,
			final ITrajectory<IVector3> newSpline,
			final List<IDrawableShape> shapes, final double curTime)
	{
		double time = curTime;
		IVector2 pointOnSpline = oldSpline.getPositionMM(time).getXYVector();
		IVector2 velocityOnSpline = oldSpline.getVelocity(time).getXYVector();
		// double angleOnSpline = oldPath.getHermiteSpline().getRotationTrajectory().getPosition(time);
		
		ITrackedBot bot = localPathFinderInput.getFieldInfo().getwFrame().getBot(localPathFinderInput.getBotId());
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
