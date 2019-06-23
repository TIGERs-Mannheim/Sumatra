/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 20, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableEllipse;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.ellipse.Ellipse;
import edu.tigers.sumatra.shapes.ellipse.IEllipse;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class EllipseObstacle extends Ellipse implements IObstacle
{
	private Color color = Color.red;
	
	
	/**
	 * 
	 */
	protected EllipseObstacle()
	{
		super();
	}
	
	
	/**
	 * @param ellipse
	 */
	public EllipseObstacle(final IEllipse ellipse)
	{
		super(ellipse);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	public EllipseObstacle(final IVector2 center, final double radiusX, final double radiusY, final double turnAngle)
	{
		super(center, radiusX, radiusY, turnAngle);
	}
	
	
	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 */
	public EllipseObstacle(final IVector2 center, final double radiusX, final double radiusY)
	{
		super(center, radiusX, radiusY);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		DrawableEllipse de = new DrawableEllipse(this);
		de.setFill(true);
		de.setColor(color);
		de.paintShape(g, tool, invert);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
