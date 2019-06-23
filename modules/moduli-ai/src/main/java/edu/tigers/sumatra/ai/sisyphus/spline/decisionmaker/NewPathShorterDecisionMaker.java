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
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * checks if the new spline is significant shorter than the old one (i.e. a bot who blocked the direct way moved out of
 * the direct way)
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class NewPathShorterDecisionMaker implements IUpdateSplineDecisionMaker
{
	
	
	private static final Logger	log							= Logger
																					.getLogger(NewPathShorterDecisionMaker.class.getName());
																					
																					
	@Configurable(comment = "[s] if the new calculated path is faster by this amount of time it will replace the old path [s]")
	private static double			useShorterPathIfFaster	= 1.5;
																			
																			
	static
	{
		ConfigRegistration.registerClass("sisyphus", NewPathShorterDecisionMaker.class);
	}
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ITrajectory<IVector3> oldSpline,
			final ITrajectory<IVector3> newSpline,
			final List<IDrawableShape> shapes, final double curTime)
	{
		double newPathTotalTime = newSpline.getTotalTime();
		double curTimeOnSpline = curTime;
		double oldPathRemainingTime = oldSpline.getTotalTime() - curTimeOnSpline;
		double threshold = useShorterPathIfFaster;
		if ((newPathTotalTime < (oldPathRemainingTime - threshold)))
		{
			log.trace("New Spline - Shorter Path found: " + newPathTotalTime + " < (" + oldPathRemainingTime + " - "
					+ useShorterPathIfFaster + "s)");
			return EDecision.OPTIMIZATION_FOUND;
		}
		return EDecision.NO_VIOLATION;
	}
}
