/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus.finder.traj.obstacles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.shapes.circle.ICircle;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class CircularObstacle extends Arc implements IObstacle
{
	/** margin is used to for nearestPointOutside */
	private final double margin;
	
	
	protected CircularObstacle()
	{
		super();
		margin = 0;
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @param margin
	 */
	private CircularObstacle(final IVector2 center, final double radius, final double startAngle, final double rotation,
			final double margin)
	{
		super(center, radius, startAngle, rotation);
		this.margin = margin;
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @return
	 */
	public static CircularObstacle circle(final IVector2 center, final double radius)
	{
		return new CircularObstacle(center, radius, 0, AngleMath.PI_TWO, 0);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param margin
	 * @return
	 */
	public static CircularObstacle circleWithMargin(final IVector2 center, final double radius, final double margin)
	{
		return new CircularObstacle(center, radius, 0, AngleMath.PI_TWO, margin);
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
		return new CircularObstacle(center, radius, startAngle, rotation, 0);
	}
	
	
	/**
	 * @param center
	 * @param radius
	 * @param startAngle
	 * @param rotation
	 * @param margin
	 * @return
	 */
	public static CircularObstacle arcWithMargin(final IVector2 center, final double radius, final double startAngle,
			final double rotation, final double margin)
	{
		return new CircularObstacle(center, radius, startAngle, rotation, margin);
	}
	
	
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		IVector2 superNearestOutside = super.nearestPointOutside(point);
		return GeoMath.stepAlongLine(superNearestOutside, center(), -margin);
	}
	
	
	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		final IVector2 transBotPos = tool.transformToGuiCoordinates(center(), invert);
		double radius = tool.scaleXLength(radius());
		int drawingX = (int) (transBotPos.x() - radius);
		int drawingY = (int) (transBotPos.y() - radius);
		
		double startAngle = AngleMath
				.rad2deg((getStartAngle() + tool.getFieldTurn().getAngle()) - (AngleMath.PI_HALF * (invert ? -1 : 1)));
		double extendAngle = AngleMath.rad2deg(getRotation());
		Shape arcShape = new Arc2D.Double(drawingX, drawingY, radius * 2, radius * 2, startAngle,
				extendAngle, Arc2D.PIE);
		g.setColor(Color.RED);
		g.draw(arcShape);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final double t, final double margin)
	{
		return isPointInShape(point, margin);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		double rangeWidth = 500;
		ICircle circleWithMargin = new Circle(center(), radius() + (rangeWidth / 2.0));
		if (!circleWithMargin.isPointInShape(curBotPos))
		{
			List<IVector2> intersections = circleWithMargin.tangentialIntersections(curBotPos);
			IVector2 p = intersections.get(rnd.nextInt(intersections.size()));
			double distp2Pos = GeoMath.distancePP(curBotPos, p);
			double minDist = distp2Pos + radius();
			double maxDist = minDist + 2000;
			double angleRange = Math.atan((rangeWidth / 2.0) / distp2Pos);
			double baseAngle = p.subtractNew(curBotPos).getAngle();
			
			for (int i = 0; i < 5; i++)
			{
				double len = (rnd.nextDouble() * (maxDist - minDist)) + minDist;
				double angle = (baseAngle + (rnd.nextDouble() * angleRange * 2)) - angleRange;
				subPoints.add(curBotPos.addNew(new Vector2(angle).scaleTo(len)));
			}
		}
		// for (int i = 0; i < 5; i++)
		// {
		// IVector2 dir = new Vector2(rnd.nextDouble() * AngleMath.PI_TWO).scaleTo(radius() + rangeWidth);
		// subPoints.add(center().addNew(dir));
		// }
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final double t)
	{
		return nearestPointOutside(point);
	}
}
