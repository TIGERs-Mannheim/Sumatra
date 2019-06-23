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

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.EDecision;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


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
	private static float				useShorterPathIfFaster	= 1.5f;
	
	
	@Override
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline,
			final List<IDrawableShape> shapes)
	{
		float newPathTotalTime = newSpline.getTotalTime();
		float curTimeOnSpline = oldSpline.getCurrentTime();
		float oldPathRemainingTime = oldSpline.getTotalTime() - curTimeOnSpline;
		float threshold = useShorterPathIfFaster;
		if ((newPathTotalTime < (oldPathRemainingTime - threshold)))
		{
			log.trace("New Spline - Shorter Path found: " + newPathTotalTime + " < (" + oldPathRemainingTime + " - "
					+ useShorterPathIfFaster + "s)");
			return EDecision.OPTIMIZATION_FOUND;
		}
		return EDecision.NO_VIOLATION;
	}
}
