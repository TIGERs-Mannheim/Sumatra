/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.filter;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.SplineGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.UpdateSplineDecisionMakerFactory;


/**
 * Path filter for hermite splines
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class HermiteSplinePathFilter implements IPathFilter
{
	private final UpdateSplineDecisionMakerFactory	updateSplineDecision	= new UpdateSplineDecisionMakerFactory();
	
	private ISpline											currentSpline			= null;
	private long												currentPathId			= 0;
	
	
	/**
	 */
	public HermiteSplinePathFilter()
	{
	}
	
	
	@Override
	public boolean accept(final PathFinderInput pathFinderInput, final IPath newPath, final IPath currentPath)
	{
		SplineGenerator gen = new SplineGenerator();
		TrackedTigerBot bot = pathFinderInput.getFieldInfo().getwFrame().getBot(pathFinderInput.getBotId());
		ISpline newSpline = gen.createSpline(bot, newPath.getPathPoints(), newPath.getTargetOrientation(),
				pathFinderInput.getMoveCon().getSpeed());
		if ((currentPath.getUniqueId() != currentPathId) || (currentSpline == null))
		{
			currentSpline = gen.createSpline(bot, currentPath.getPathPoints(), currentPath.getTargetOrientation(),
					pathFinderInput.getMoveCon().getSpeed());
			currentPathId = currentPath.getUniqueId();
		}
		
		switch (updateSplineDecision.check(pathFinderInput, currentSpline, newSpline))
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
				if (newPath.isRambo() && !currentPath.isRambo())
				{
					return true;
				} else if (!newPath.isRambo())
				{
					return true;
				}
				break;
			
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
