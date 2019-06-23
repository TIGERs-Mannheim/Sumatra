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

import edu.tigers.sumatra.ai.sisyphus.PathFinderInput;
import edu.tigers.sumatra.ai.sisyphus.spline.EDecision;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;


/**
 * ever pathplanninginterval ms a new path is calculated.
 * However, the bots drive much better if the path is not updated that often. So it needs to be decided if the path and
 * the spline will be updated or not.
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public interface IUpdateSplineDecisionMaker
{
	/**
	 * every DecisionMaker can decide whether the path should be updated or not
	 * 
	 * @param localPathFinderInput
	 * @param oldSpline
	 * @param newSpline
	 * @param shapes
	 * @param curTime TODO
	 * @return
	 */
	EDecision check(PathFinderInput localPathFinderInput, ITrajectory<IVector3> oldSpline,
			ITrajectory<IVector3> newSpline,
			List<IDrawableShape> shapes, double curTime);
}
