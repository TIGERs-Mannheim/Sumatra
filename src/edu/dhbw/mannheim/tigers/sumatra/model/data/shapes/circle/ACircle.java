/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.rectangle.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent
public abstract class ACircle implements ICircle, IObstacle
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Only for code-reuse, not meant to be used by anyone anymore! =)
	 */
	ACircle()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float getArea()
	{
		return radius() * radius() * AngleMath.PI;
	}
	
	
	/**
	 * Test if the point is in or on the given circle.
	 * 
	 * @author Steffen
	 * @author Dion
	 */
	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		return isPointInShape(point, 0);
	}
	
	
	@Override
	public boolean isPointInShape(final IVector2 point, final float margin)
	{
		final Vector2 tmp = point.subtractNew(center());
		
		if (tmp.getLength2() <= (radius() + margin))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Checks if a given line is intercecting this circle.
	 * Touching means intercecting!
	 * 
	 * @author Dion
	 */
	@Override
	public boolean isLineIntersectingShape(final ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public boolean isLineSegmentIntersectingShape(final IVector2 start, final IVector2 end)
	{
		List<IVector2> intersecs = lineIntersections(Line.newLine(start, end));
		Rectangle rect = new Rectangle(start, end);
		int numInvalid = 0;
		for (IVector2 inters : intersecs)
		{
			if (!rect.isPointInShape(inters))
			{
				numInvalid++;
			}
		}
		return (intersecs.size() - numInvalid) > 0;
	}
	
	
	/**
	 * Calculates where a given line is intersecting this circle.
	 * Touching means intersecting!<br>
	 * <a href="http://mathworld.wolfram.com/Circle-LineIntersection.html">
	 * Mathmatical Theory here.</a>
	 * 
	 * @param line line to check the intersection
	 * @return all intersection points, can be 0, 1 or 2
	 * @author Dion
	 */
	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		final List<IVector2> result = new ArrayList<IVector2>();
		
		final float dx = line.directionVector().x();
		final float dy = line.directionVector().y();
		final float dr = line.directionVector().getLength2();
		final Vector2 newSupport = line.supportVector().subtractNew(center());
		final float det = (newSupport.x() * (newSupport.y() + dy)) - ((newSupport.x() + dx) * newSupport.y());
		
		final float inRoot = (radius() * radius() * dr * dr) - (det * det);
		
		if (inRoot < 0)
		{
			return result;
		}
		
		if (inRoot == 0)
		{
			final Vector2 temp = new Vector2();
			temp.setX((det * dy) / (dr * dr));
			temp.setY((-det * dx) / (dr * dr));
			// because of moved coordinate system (newSupport):
			temp.add(center());
			
			result.add(temp);
			
			return result;
		}
		final float sqRoot = (float) Math.sqrt(inRoot);
		final Vector2 temp1 = new Vector2();
		final Vector2 temp2 = new Vector2();
		
		temp1.setX(((det * dy) + (dx * sqRoot)) / (dr * dr));
		temp1.setY(((-det * dx) + (dy * sqRoot)) / (dr * dr));
		temp2.setX(((det * dy) - (dx * sqRoot)) / (dr * dr));
		temp2.setY(((-det * dx) - (dy * sqRoot)) / (dr * dr));
		// because of moved coordinate system (newSupport):
		temp1.add(center());
		temp2.add(center());
		
		result.add(temp1);
		result.add(temp2);
		return result;
	}
	
	
	/**
	 * Returns the nearest point outside a shape to a given point inside the shape.
	 * If the given point is outside the shape, return the point.
	 * Touching is outside!
	 * 
	 * @author Dion
	 */
	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		final Vector2 direction = point.subtractNew(center());
		final float factor = radius() / direction.getLength2();
		
		if (Float.isFinite(factor))
		{
			if (factor <= 1)
			{
				return point;
			}
			direction.multiply(factor);
			direction.add(center());
			return direction;
		}
		return point.addNew(new Vector2(radius(), 0));
	}
	
	
	@Override
	public boolean isPointCollidingWithObstacle(final IVector2 point, final float t)
	{
		return isPointInShape(point);
	}
	
	
	@Override
	public void generateObstacleAvoidancePoints(final IVector2 curBotPos, final Random rnd,
			final List<IVector2> subPoints)
	{
		float rangeWidth = 500;
		ICircle circleWithMargin = new Circle(center(), radius() + (rangeWidth / 2));
		if (!circleWithMargin.isPointInShape(curBotPos))
		{
			List<IVector2> intersections = GeoMath.tangentialIntersections(circleWithMargin, curBotPos);
			IVector2 p = intersections.get(rnd.nextInt(intersections.size()));
			float distp2Pos = GeoMath.distancePP(curBotPos, p);
			float minDist = distp2Pos + radius();
			float maxDist = minDist + 2000;
			float angleRange = (float) Math.atan((rangeWidth / 2) / distp2Pos);
			float baseAngle = p.subtractNew(curBotPos).getAngle();
			
			for (int i = 0; i < 5; i++)
			{
				float len = (rnd.nextFloat() * (maxDist - minDist)) + minDist;
				float angle = (baseAngle + (rnd.nextFloat() * angleRange * 2)) - angleRange;
				subPoints.add(curBotPos.addNew(new Vector2(angle).scaleTo(len)));
			}
		}
		// for (int i = 0; i < 5; i++)
		// {
		// IVector2 dir = new Vector2(rnd.nextFloat() * AngleMath.PI_TWO).scaleTo(radius() + rangeWidth);
		// subPoints.add(center().addNew(dir));
		// }
	}
	
	
	@Override
	public IVector2 nearestPointOutsideObstacle(final IVector2 point, final float t)
	{
		return nearestPointOutside(point);
	}
}
