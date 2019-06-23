/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 31, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.ai.sisyphus.spline.SplineGenerator;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


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
	 */
	public HermiteSplinePathDriver(final ITrackedBot bot, final IPath path)
	{
		super(createSpline(bot, path));
	}
	
	
	private static ITrajectory<IVector3> createSpline(final ITrackedBot bot, final IPath path)
	{
		SplineGenerator splineGenerator = new SplineGenerator(bot.getBot().getType());
		ITrajectory<IVector3> spline = splineGenerator.createSpline(bot, path.getPathPoints(),
				path.getTargetOrientation(), 0);
		return spline;
	}
}
