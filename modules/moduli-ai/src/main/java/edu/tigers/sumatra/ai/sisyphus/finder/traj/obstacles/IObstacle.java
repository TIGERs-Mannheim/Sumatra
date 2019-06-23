/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 16, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.util.List;
import java.util.Random;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IObstacle extends IDrawableShape
{
	/**
	 * @param dest
	 * @return
	 */
	@Deprecated
	default IVector2 shiftDestination(final IVector2 dest)
	{
		if (isPointCollidingWithObstacle(dest, Double.MAX_VALUE))
		{
			return nearestPointOutsideObstacle(dest, Double.MAX_VALUE);
		}
		return dest;
	}
	
	
	/**
	 * @param point
	 * @param t the time in future
	 * @return
	 */
	boolean isPointCollidingWithObstacle(IVector2 point, double t);
	
	
	/**
	 * @param point
	 * @param t
	 * @param margin
	 * @return
	 */
	default boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return isPointCollidingWithObstacle(point, t);
	}
	
	
	/**
	 * @param point
	 * @param t
	 * @return
	 */
	IVector2 nearestPointOutsideObstacle(IVector2 point, double t);
	
	
	/**
	 * @param curBotPos
	 * @param rnd
	 * @param subPoints
	 */
	void generateObstacleAvoidancePoints(IVector2 curBotPos, Random rnd, List<IVector2> subPoints);
	
	
	/**
	 * @return
	 */
	default boolean isSensitiveToTouch()
	{
		return false;
	}
}
