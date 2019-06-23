/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class CircleObstacle extends Circle implements IObstacle
{
	private Color color = Color.red;
	
	
	protected CircleObstacle()
	{
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param circle
	 */
	public CircleObstacle(final ICircle circle)
	{
		super(circle);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		double rangeWidth = 400;
		ICircle circleWithMargin = new Circle(center(), radius() + (rangeWidth / 2.0));
		// if (!circleWithMargin.isPointInShape(curBotPos))
		// {
		// List<IVector2> intersections = circleWithMargin.tangentialIntersections(curBotPos);
		// IVector2 p = intersections.get(rnd.nextInt(intersections.size()));
		// double distp2Pos = GeoMath.distancePP(curBotPos, p);
		// double minDist = distp2Pos + radius();
		// double maxDist = minDist + 2000;
		// double angleRange = Math.atan((rangeWidth / 2.0) / distp2Pos);
		// double baseAngle = p.subtractNew(curBotPos).getAngle();
		//
		// for (int i = 0; i < 5; i++)
		// {
		// double len = (rnd.nextDouble() * (maxDist - minDist)) + minDist;
		// double angle = (baseAngle + (rnd.nextDouble() * angleRange * 2)) - angleRange;
		// subPoints.add(curBotPos.addNew(new Vector2(angle).scaleTo(len)));
		// }
		// }
		// for (int i = 0; i < 5; i++)
		// {
		// IVector2 dir = new Vector2(rnd.nextDouble() * AngleMath.PI_TWO).scaleTo(radius() + rangeWidth);
		// subPoints.add(center().addNew(dir));
		// }
		
		if (!circleWithMargin.isPointInShape(curBotPos))
		{
			List<IVector2> intersections = circleWithMargin.tangentialIntersections(curBotPos);
			for (IVector2 intersec : intersections)
			{
				double dist = GeoMath.distancePP(curBotPos, intersec);
				if (dist < 1000)
				{
					subPoints.add(GeoMath.stepAlongLine(curBotPos, intersec, 1000));
				} else
				{
					subPoints.add(intersec);
				}
			}
		}
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return nearestPointOutside(point);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		// --- from SSLVision-mm to java2d-coordinates ---
		final IVector2 center = tool.transformToGuiCoordinates(center(), invert);
		final double radius = tool.scaleXLength(radius());
		
		g.setColor(color);
		g.setStroke(new BasicStroke(1));
		g.drawOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
		g.fillOval((int) (center.x() - radius), (int) (center.y() - radius), (int) radius * 2, (int) radius * 2);
	}
	
	
	@Override
	public void setColor(final Color color)
	{
		this.color = color;
	}
}
