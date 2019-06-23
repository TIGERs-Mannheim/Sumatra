/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 31, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DrawablePath implements IDrawableShape
{
	private final List<IVector2>	path;
	private final Color				color;
	
	
	@SuppressWarnings("unused")
	private DrawablePath()
	{
		this(new ArrayList<>());
	}
	
	
	/**
	 * @param path
	 */
	public DrawablePath(final List<IVector2> path)
	{
		this(path, Color.red);
	}
	
	
	/**
	 * @param path
	 * @param color
	 */
	public DrawablePath(final List<IVector2> path, final Color color)
	{
		this.path = new ArrayList<>(path);
		this.color = color;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setColor(color);
		
		final GeneralPath drawPath = new GeneralPath();
		IVector2 startPos = path.get(0);
		final IVector2 transBotPos = tool.transformToGuiCoordinates(startPos, invert);
		final int robotX = (int) transBotPos.x();
		final int robotY = (int) transBotPos.y();
		drawPath.moveTo(robotX, robotY);
		
		for (int i = 0; i < path.size(); i++)
		{
			IVector2 point = path.get(i);
			final IVector2 transPathPoint = tool.transformToGuiCoordinates(point, invert);
			g.drawOval((int) transPathPoint.x() - 1, (int) transPathPoint.y() - 1, 3, 3);
			drawPath.lineTo((int) transPathPoint.x(), (int) transPathPoint.y());
		}
		g.draw(drawPath);
	}
	
}
