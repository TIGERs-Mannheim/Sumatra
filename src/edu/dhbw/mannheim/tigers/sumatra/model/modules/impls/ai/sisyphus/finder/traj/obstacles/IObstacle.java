/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IObstacle extends IDrawableShape
{
	/**
	 * @param point
	 * @param t the time in future
	 * @return
	 */
	boolean isPointCollidingWithObstacle(IVector2 point, float t);
	
	
	/**
	 * @param point
	 * @param t
	 * @return
	 */
	IVector2 nearestPointOutsideObstacle(IVector2 point, float t);
	
	
	/**
	 * @param curBotPos
	 * @param rnd
	 * @param subPoints
	 */
	void generateObstacleAvoidancePoints(IVector2 curBotPos, Random rnd, List<IVector2> subPoints);
}
