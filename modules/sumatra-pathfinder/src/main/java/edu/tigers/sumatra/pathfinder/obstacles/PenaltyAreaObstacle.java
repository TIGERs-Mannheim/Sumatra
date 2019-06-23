/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A penalty area obstacle with efficient data storage and caching for painting
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class PenaltyAreaObstacle implements IObstacle
{
	private IPenaltyArea penaltyArea;
	private transient List<IDrawableShape> shapes;
	
	
	@SuppressWarnings("unused")
	private PenaltyAreaObstacle()
	{
	}
	
	
	/**
	 * @param penaltyArea the penalty area
	 */
	public PenaltyAreaObstacle(final IPenaltyArea penaltyArea)
	{
		this.penaltyArea = penaltyArea;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return penaltyArea.isPointInShapeOrBehind(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		if (shapes == null)
		{
			shapes = penaltyArea.getDrawableShapes();
			setColor(Color.BLACK);
		}
		shapes.forEach(s -> s.paintShape(g, tool, invert));
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		if (shapes != null)
		{
			shapes.forEach(s -> s.setColor(color));
		}
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return penaltyArea.withMargin(margin).isPointInShapeOrBehind(point);
	}
	
	
	@Override
	public void setStrokeWidth(final double strokeWidth)
	{
		if (shapes != null)
		{
			shapes.forEach(s -> s.setStrokeWidth(strokeWidth));
		}
	}
}
