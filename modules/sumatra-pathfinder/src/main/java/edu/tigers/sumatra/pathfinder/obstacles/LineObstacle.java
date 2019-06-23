/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles;

import java.awt.*;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class LineObstacle implements IObstacle
{
	
	private final ILine	lineSegment;
	private final double	radius;
	
	
	@SuppressWarnings("unused")
	private LineObstacle()
	{
		lineSegment = Line.fromDirection(Vector2.ZERO_VECTOR, Vector2.X_AXIS);
		radius = 0;
	}
	
	
	/**
	 * New instance
	 * 
	 * @param lineSegment the base line
	 * @param radius the radius around the line
	 */
	public LineObstacle(final ILine lineSegment, final double radius)
	{
		this.lineSegment = lineSegment;
		this.radius = radius;
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointCollidingWithObstacle(point, t, 0);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return lineSegment.nearestPointOnLineSegment(point).distanceTo(point) < radius + margin;
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		g.setColor(Color.black);
		new DrawableCircle(lineSegment.getStart(), radius, Color.black).paintShape(g, tool, invert);
		new DrawableCircle(lineSegment.getEnd(), radius, Color.black).paintShape(g, tool, invert);
		
		IVector2 p0 = lineSegment.getStart().addNew(lineSegment.directionVector().getNormalVector().scaleTo(radius));
		IVector2 p1 = lineSegment.getEnd().addNew(lineSegment.directionVector().getNormalVector().scaleTo(radius));
		IVector2 p2 = lineSegment.getStart().addNew(lineSegment.directionVector().getNormalVector().scaleTo(-radius));
		IVector2 p3 = lineSegment.getEnd().addNew(lineSegment.directionVector().getNormalVector().scaleTo(-radius));
		new DrawableLine(Line.fromPoints(p0, p1)).paintShape(g, tool, invert);
		new DrawableLine(Line.fromPoints(p2, p3)).paintShape(g, tool, invert);
	}
}
