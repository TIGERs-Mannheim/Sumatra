/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;


/**
 * PathDriver based on hermite splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class HermiteSplinePathDriver extends SplinePathDriver
{
	/**
	 * @param bot
	 * @param path
	 * @param maxSpeed
	 * @param forcedEndTime
	 */
	public HermiteSplinePathDriver(final TrackedTigerBot bot, final IPath path, final float maxSpeed,
			final float forcedEndTime)
	{
		super(createSpline(bot, path, maxSpeed), forcedEndTime);
		
		if (path.getStartPos() == null)
		{
			path.setStartPos(bot.getPos());
		}
	}
	
	
	private static ISpline createSpline(final TrackedTigerBot bot, final IPath path, final float maxSpeed)
	{
		SplineGenerator splineGenerator = new SplineGenerator(bot.getBotType());
		ISpline spline = splineGenerator.createSpline(bot, path.getPathPoints(), path.getTargetOrientation(), maxSpeed);
		return spline;
	}
}
