/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.ICircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class TrajAwareRobotObstacle implements IObstacle
{
	private final TrajPath	path;
	private final float		radius;
	private final float		startTime;
	
	
	@SuppressWarnings("unused")
	private TrajAwareRobotObstacle()
	{
		path = null;
		radius = 0;
		startTime = 0;
	}
	
	
	/**
	 * @param path
	 * @param radius
	 */
	public TrajAwareRobotObstacle(final TrajPath path, final float radius)
	{
		super();
		this.path = path;
		this.radius = radius;
		startTime = path.getCurrentTime();
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		IVector2 pos = path.getPosition(t);
		ICircle circle = new Circle(pos, radius);
		return circle.isPointInShape(point);
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		IVector2 pos = path.getPosition(t);
		ICircle circle = new Circle(pos, radius);
		return circle.nearestPointOutside(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		IVector2 pos = path.getPosition(0);
		Circle circle = new Circle(pos, radius);
		circle.generateObstacleAvoidancePoints(curBotPos, rnd, subPoints);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IFieldPanel fieldPanel, final boolean invert)
	{
		final float guiRadius = fieldPanel.scaleXLength(radius);
		
		for (float t = startTime; t < path.getTotalTime(); t += 0.2f)
		{
			IVector2 ballPos = path.getPosition(t);
			final IVector2 center = fieldPanel.transformToGuiCoordinates(ballPos, invert);
			
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(1));
			g.drawOval((int) (center.x() - guiRadius), (int) (center.y() - guiRadius), (int) guiRadius * 2,
					(int) guiRadius * 2);
			if (t > 0)
			{
				g.drawString(String.format("%.1f", t), (center.x() - guiRadius), (center.y() - guiRadius));
			}
		}
	}
}
