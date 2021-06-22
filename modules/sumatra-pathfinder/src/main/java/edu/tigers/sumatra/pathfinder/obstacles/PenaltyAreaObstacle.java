/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A penalty area obstacle with efficient data storage and caching for painting
 */
public class PenaltyAreaObstacle extends AObstacle
{
	private final IPenaltyArea penaltyArea;


	/**
	 * @param penaltyArea the penalty area
	 */
	public PenaltyAreaObstacle(final IPenaltyArea penaltyArea)
	{
		this.penaltyArea = penaltyArea;
		setEmergencyBrakeFor(true);
	}


	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return penaltyArea.withMargin(margin).isPointInShapeOrBehind(point);
	}


	@Override
	protected void initializeShapes(final List<IDrawableShape> shapes)
	{
		shapes.addAll(penaltyArea.getDrawableShapes());
		shapes.forEach(s -> s.setColor(Color.BLACK));
	}


	@Override
	public double collisionPenalty(final IVector2 from, final IVector2 to)
	{
		return penaltyArea.intersectionArea(from, to);
	}
}
