/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Check if new destination makes new path necessary
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DestinationChangedDecisionMaker implements IUpdateSplineDecisionMaker
{
	private static final Logger	log							= Logger.getLogger(DestinationChangedDecisionMaker.class
																					.getName());
	
	@Configurable(comment = "time [s] - if remaining time is below this, new spline will be enforced.")
	private static float				timeRemainingTolerance	= 0.5f;
	@Configurable(comment = "dist [mm] - if distance between old and new destination on spline is greater, new spline will be enforced")
	private static float				destinationTolerance		= 100f;
	@Configurable(comment = "time [s] - destinations will be taken on spline now plus lookahead.")
	private static float				lookahead					= 0.5f;
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline,
			final List<IDrawableShape> shapes)
	{
		float curTime = oldSpline.getCurrentTime();
		// IVector2 oldPathPos = oldPath.getSpline().getValueByTime(curTime + lookahead);
		// IVector2 oldPathSpeed = oldPath.getSpline().getAccelerationByTime(curTime + lookahead);
		// IVector2 newPathPos = newPath.getSpline().getValueByTime(lookahead);
		
		// float diff = oldPathPos.subtractNew(newPathPos).getLength2();
		// if (!oldPathSpeed.isZeroVector())
		// {
		// Line tangentToOldPath = new Line(oldPathPos, oldPathSpeed);
		// diff = GeoMath.distancePL(newPathPos, tangentToOldPath);
		// }
		float timeRemaining = oldSpline.getTotalTime() - curTime;
		
		// if ((diff > destinationTolerance))
		// {
		// log.trace(String.format("New Spline - destination changed path too much. Diff in %.2fs will be %.3f > %.3f",
		// lookahead, diff, destinationTolerance));
		// return EDecision.ENFORCE;
		// }
		float finalDiffMM = GeoMath.distancePP(oldSpline.getPositionByTime(oldSpline.getTotalTime()).getXYVector(),
				newSpline.getPositionByTime(newSpline.getTotalTime()).getXYVector());
		if (((timeRemaining < timeRemainingTolerance) && (finalDiffMM > 50)))
		{
			log.trace("New Spline - near destination and destination changed.");
			return EDecision.ENFORCE;
		}
		if (finalDiffMM > (destinationTolerance))
		{
			log.trace("New Spline - destination changed, diff=" + finalDiffMM);
			return EDecision.ENFORCE;
		}
		return EDecision.NO_VIOLATION;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
