/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 23, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class FieldBorderObstacle implements IObstacle
{
	private final DrawableRectangle field;
	
	
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private FieldBorderObstacle()
	{
		field = null;
	}
	
	
	/**
	 * @param field
	 */
	public FieldBorderObstacle(final Rectangle field)
	{
		this.field = new DrawableRectangle(field);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		field.paintShape(g, tool, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return !field.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return field.nearestPointInside(point, Geometry.getBotRadius());
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> obstacles)
	{
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		field.setColor(color);
	}
}
