/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A penalty area obstacle with efficient data storage and caching for painting
 */
@Persistent
public class PenaltyAreaObstacle extends AObstacle
{
	private IPenaltyArea penaltyArea;


	private PenaltyAreaObstacle()
	{
		setCritical(true);
	}


	/**
	 * @param penaltyArea the penalty area
	 */
	public PenaltyAreaObstacle(final IPenaltyArea penaltyArea)
	{
		this();
		this.penaltyArea = penaltyArea;
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return penaltyArea.isPointInShapeOrBehind(point);
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return penaltyArea.withMargin(margin).isPointInShapeOrBehind(point);
	}


	@Override
	protected void initializeShapes()
	{
		shapes.addAll(penaltyArea.getDrawableShapes());
		shapes.forEach(s -> s.setColor(Color.BLACK));
	}
}
