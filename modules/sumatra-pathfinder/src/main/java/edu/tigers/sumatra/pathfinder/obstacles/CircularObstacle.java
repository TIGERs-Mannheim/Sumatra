/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;
import java.awt.geom.Arc2D;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class CircularObstacle implements IObstacle
{
	private final IArc arc;
	
	
	@SuppressWarnings("unused")
	private CircularObstacle()
	{
		super();
		arc = Arc.createArc(Vector2.ZERO_VECTOR, 0, 0, 0);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 */
	private CircularObstacle(final IVector2 center, final double radius, final double startAngle, final double rotation)
	{
		arc = Arc.createArc(center, radius, startAngle, rotation);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return
	 */
	public static CircularObstacle circle(final IVector2 center, final double radius)
	{
		return new CircularObstacle(center, radius, 0, AngleMath.PI_TWO);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @return
	 */
	public static CircularObstacle arc(final IVector2 center, final double radius, final double startAngle,
			final double rotation)
	{
		return new CircularObstacle(center, radius, startAngle, rotation);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final IVector2 transBotPos = tool.transformToGuiCoordinates(arc.center(), invert);
		double radius = tool.scaleXLength(arc.radius());
		int drawingX = (int) (transBotPos.x() - radius);
		int drawingY = (int) (transBotPos.y() - radius);
		
		double startAngle = AngleMath
				.rad2deg((arc.getStartAngle() + tool.getFieldTurn().getAngle()) - (AngleMath.PI_HALF * (invert ? -1 : 1)));
		double extendAngle = AngleMath.rad2deg(arc.getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, Arc2D.PIE);
		g.setColor(Color.black);
		g.draw(arcShape);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return arc.isPointInShape(point);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return arc.isPointInShape(point, margin);
	}
	
}
