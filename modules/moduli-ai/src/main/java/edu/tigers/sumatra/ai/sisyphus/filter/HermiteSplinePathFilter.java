/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.filter;

import java.util.List;

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.spline.SplineGenerator;
import edu.tigers.sumatra.ai.sisyphus.spline.UpdateSplineDecisionMakerFactory;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.shapes.path.IPath;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Path filter for hermite splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class HermiteSplinePathFilter implements IPathFilter
{
	private final UpdateSplineDecisionMakerFactory	updateSplineDecision	= new UpdateSplineDecisionMakerFactory();
																								
	private ITrajectory<IVector3>							currentSpline			= null;
	private IPath												currentPath				= null;
	private long												startTime;
																	
																	
	/**
	 */
	public HermiteSplinePathFilter()
	{
	}
	
	
	@Override
	public boolean accept(final PathFinderInput pathFinderInput, final IPath newPath, final IPath currentPath)
	{
		SplineGenerator gen = new SplineGenerator();
		ITrackedBot bot = pathFinderInput.getFieldInfo().getwFrame().getBot(pathFinderInput.getBotId());
		ITrajectory<IVector3> newSpline = gen.createSpline(bot, newPath.getPathPoints(), newPath.getTargetOrientation(),
				0);
		if (!currentPath.equals(this.currentPath) || (currentSpline == null))
		{
			currentSpline = gen.createSpline(bot, currentPath.getPathPoints(), currentPath.getTargetOrientation(), 0);
			this.currentPath = currentPath;
			startTime = pathFinderInput.getFieldInfo().getwFrame().getTimestamp();
		}
		
		double curTime = (pathFinderInput.getFieldInfo().getwFrame().getTimestamp() - startTime) / 1e9;
		switch (updateSplineDecision.check(pathFinderInput, currentSpline, newSpline, curTime))
		{
			case ENFORCE:
				return true;
			case OPTIMIZATION_FOUND:
				// if (!pathFinderInput.getMoveCon().isOptimizationWanted())
				// {
				// break;
				// }
			case COLLISION_AHEAD:
			case VIOLATION:
				return true;
			case NO_VIOLATION:
				break;
		}
		return false;
	}
	
	
	@Override
	public void getDrawableShapes(final List<IDrawableShape> shapes)
	{
		shapes.addAll(updateSplineDecision.getShapes());
	}
}
