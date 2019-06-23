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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * checks if the bot has reached the end of the spline but the target is not reached
 * Tigerv2 bot should automtically drive to the target, for the other bots a new spline is needed
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class SplineEndGoalNotReachedDecisionMaker implements IUpdateSplineDecisionMaker
{
	private static final Logger	log				= Logger
																		.getLogger(SplineEndGoalNotReachedDecisionMaker.class.getName());
	
	@Configurable(comment = "Dist [mm] - max allowed distance to destination when pos is considered reached.")
	private static float				positionTol		= 40;
	
	@Configurable(comment = "Dist [rad] - max allowed distance to target orientation when orientation is considered reached.")
	private static float				orientationTol	= 0.17f;
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline,
			final List<IDrawableShape> shapes)
	{
		final SimpleWorldFrame wFrame = localPathFinderInput.getFieldInfo().getwFrame();
		final TrackedTigerBot bot = wFrame.getBot(localPathFinderInput.getBotId());
		final MovementCon moveCon = localPathFinderInput.getMoveCon();
		final IVector2 destination = moveCon.getDestCon().getDestination();
		final float targetOrientation = moveCon.getAngleCon().getTargetAngle();
		float curTimeOnSpline = oldSpline.getCurrentTime();
		
		boolean isGoalReached = destination.equals(bot.getPos(), positionTol);
		boolean isTargetAngleReached = (Math.abs(AngleMath.getShortestRotation(targetOrientation, bot.getAngle())) < orientationTol);
		boolean isSplineCompleted = curTimeOnSpline > oldSpline.getTotalTime();
		boolean isTargetInPenArea = AIConfig.getGeometry().getPenaltyAreaOur()
				.isPointInShape(destination, AIConfig.getGeometry().getBotRadius());
		
		if (isSplineCompleted && (!isGoalReached || !isTargetAngleReached) && !isTargetInPenArea)
		{
			log.trace("New Spline - Spline end but goal is not reached yet: Goal not reached: "
					+ oldSpline.getPositionByTime(oldSpline.getTotalTime()).getXYVector()
					+ " != " + bot.getPos());
			return EDecision.ENFORCE;
		}
		return EDecision.NO_VIOLATION;
	}
}
