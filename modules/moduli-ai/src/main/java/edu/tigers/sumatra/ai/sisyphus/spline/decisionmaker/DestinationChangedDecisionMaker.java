/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 29, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
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
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;


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
	private static double			timeRemainingTolerance	= 0.5;
	@Configurable(comment = "dist [mm] - if distance between old and new destination on spline is greater, new spline will be enforced")
	private static double			destinationTolerance		= 100;
	@Configurable(comment = "time [s] - destinations will be taken on spline now plus lookahead.")
	private static double			lookahead					= 0.5;
																			
																			
	static
	{
		ConfigRegistration.registerClass("sisyphus", DestinationChangedDecisionMaker.class);
	}
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ITrajectory<IVector3> oldSpline,
			final ITrajectory<IVector3> newSpline,
			final List<IDrawableShape> shapes, final double curTime)
	{
		// IVector2 oldPathPos = oldPath.getSpline().getValueByTime(curTime + lookahead);
		// IVector2 oldPathSpeed = oldPath.getSpline().getAccelerationByTime(curTime + lookahead);
		// IVector2 newPathPos = newPath.getSpline().getValueByTime(lookahead);
		
		// double diff = oldPathPos.subtractNew(newPathPos).getLength2();
		// if (!oldPathSpeed.isZeroVector())
		// {
		// Line tangentToOldPath = new Line(oldPathPos, oldPathSpeed);
		// diff = GeoMath.distancePL(newPathPos, tangentToOldPath);
		// }
		double timeRemaining = oldSpline.getTotalTime() - curTime;
		
		// if ((diff > destinationTolerance))
		// {
		// log.trace(String.format("New Spline - destination changed path too much. Diff in %.2fs will be %.3f > %.3f",
		// lookahead, diff, destinationTolerance));
		// return EDecision.ENFORCE;
		// }
		double finalDiffMM = GeoMath.distancePP(oldSpline.getPositionMM(oldSpline.getTotalTime()).getXYVector(),
				newSpline.getPositionMM(newSpline.getTotalTime()).getXYVector());
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
