/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.errt.ERRTFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.IPathFinder;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;


/**
 * Utility class for path estimation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PathEstimator
{
	// private final IPathFinder pathFinder = new IBAFinder();
	
	
	private final IPathFinder	pathFinder	= new ERRTFinder();
	
	
	/**
	 * @param bot
	 * @param dest
	 * @param wf
	 * @param moveCon
	 * @return
	 */
	public IPath calcPath(final TrackedTigerBot bot, final IVector2 dest, final SimpleWorldFrame wf,
			final MovementCon moveCon)
	{
		PathFinderInput pathFinderInput = new PathFinderInput(bot.getId(), moveCon);
		moveCon.update(wf, bot.getId());
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
	public IPath calcPath(final TrackedTigerBot bot, final IVector2 dest, final SimpleWorldFrame wf)
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
	public ISpline calcSpline(final TrackedTigerBot bot, final IPath path, final float finalOrientation)
	{
		SplineGenerator gen = new SplineGenerator();
		return gen.createSpline(bot, path.getPathPoints(), finalOrientation, 0);
	}
	
	
	/**
	 * @param spline
	 * @return
	 */
	public float calcTravelTime(final ISpline spline)
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
	public float calcTravelTime(final TrackedTigerBot bot, final IVector2 dest, final SimpleWorldFrame wf,
			final float finalOrientation)
	{
		IPath path = calcPath(bot, dest, wf);
		ISpline spline = calcSpline(bot, path, finalOrientation);
		float travelTime = calcTravelTime(spline);
		return travelTime;
	}
}
