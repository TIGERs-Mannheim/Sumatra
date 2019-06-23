/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Arc;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircularObstacle extends Arc
{
	/** margin is used to for nearestPointOutside */
	private final float	margin;
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @param margin
	 */
	private CircularObstacle(final IVector2 center, final float radius, final float startAngle, final float rotation,
			final float margin)
	{
		super(center, radius, startAngle, rotation);
		this.margin = margin;
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return
	 */
	public static CircularObstacle circle(final IVector2 center, final float radius)
	{
		return new CircularObstacle(center, radius, 0, AngleMath.PI_TWO, 0);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param margin
	 * @return
	 */
	public static CircularObstacle circleWithMargin(final IVector2 center, final float radius, final float margin)
	{
		return new CircularObstacle(center, radius, 0, AngleMath.PI_TWO, margin);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @return
	 */
	public static CircularObstacle arc(final IVector2 center, final float radius, final float startAngle,
			final float rotation)
	{
		return new CircularObstacle(center, radius, startAngle, rotation, 0);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @param margin
	 * @return
	 */
	public static CircularObstacle arcWithMargin(final IVector2 center, final float radius, final float startAngle,
			final float rotation, final float margin)
	{
		return new CircularObstacle(center, radius, startAngle, rotation, margin);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		IVector2 superNearestOutside = super.nearestPointOutside(point);
		return GeoMath.stepAlongLine(superNearestOutside, center(), -margin);
	}
}
