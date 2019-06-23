/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import edu.tigers.sumatra.ai.sisyphus.errt.ERRTFinder;
import edu.tigers.sumatra.ai.sisyphus.finder.IPathFinder;
import edu.tigers.sumatra.ai.sisyphus.spline.SplineGenerator;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Utility class for path estimation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PathEstimator
{
	// private final IPathFinder pathFinder = new IBAFinder();
	
	
	private final IPathFinder pathFinder = new ERRTFinder();
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param wf
	 * @param moveCon
	 * @return
	 */
	public IPath calcPath(final ITrackedBot bot, final IVector2 dest, final WorldFrame wf,
			final MovementCon moveCon)
	{
		PathFinderInput pathFinderInput = new PathFinderInput(bot.getBotId(), moveCon);
		moveCon.update(wf, bot);
		pathFinderInput.getFieldInfo().updateWorldFrame(wf);
		pathFinder.getAdjustableParams().setFastApprox(true);
		IPath path = pathFinder.calcPath(pathFinderInput);
		
		return path;
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param wf
	 * @return
	 */
	public IPath calcPath(final ITrackedBot bot, final IVector2 dest, final WorldFrame wf)
	{
		MovementCon moveCon = new MovementCon();
		moveCon.updateDestination(dest);
		return calcPath(bot, dest, wf, moveCon);
	}
	
	
	/**
	 * @param bot
	 * @param path
	 * @param finalOrientation
	 * @return
	 */
	public ITrajectory<IVector3> calcSpline(final ITrackedBot bot, final IPath path, final double finalOrientation)
	{
		SplineGenerator gen = new SplineGenerator();
		return gen.createSpline(bot, path.getPathPoints(), finalOrientation, 0);
	}
	
	
	/**
	 * @param spline
	 * @return
	 */
	public double calcTravelTime(final ITrajectory<IVector3> spline)
	{
		return spline.getTotalTime();
	}
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param wf
	 * @param finalOrientation
	 * @return
	 */
	public double calcTravelTime(final ITrackedBot bot, final IVector2 dest, final WorldFrame wf,
			final double finalOrientation)
	{
		IPath path = calcPath(bot, dest, wf);
		ITrajectory<IVector3> spline = calcSpline(bot, path, finalOrientation);
		double travelTime = calcTravelTime(spline);
		return travelTime;
	}
}
