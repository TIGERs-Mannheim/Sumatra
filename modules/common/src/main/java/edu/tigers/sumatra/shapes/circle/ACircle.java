/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.circle;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Geometrical representation of a circle.
 * 
 * @author Malte
 */
@Persistent
public abstract class ACircle implements ICircle
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
	public double getArea()
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
	public boolean isPointInShape(final IVector2 point, final double margin)
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
		
		if (line.directionVector().isZeroVector())
		{
			return result;
		}
		
		final double dx = line.directionVector().x();
		final double dy = line.directionVector().y();
		final double dr = line.directionVector().getLength2();
		final Vector2 newSupport = line.supportVector().subtractNew(center());
		final double det = (newSupport.x() * (newSupport.y() + dy)) - ((newSupport.x() + dx) * newSupport.y());
		
		final double inRoot = (radius() * radius() * dr * dr) - (det * det);
		
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
		final double sqRoot = Math.sqrt(inRoot);
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
		final double factor = radius() / direction.getLength2();
		
		if (Double.isFinite(factor))
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
	
	
	/**
	 * Get the intersection points of the two tangential lines that cross the external points.
	 * 
	 * @see <a href="https://de.wikipedia.org/wiki/Kreistangente">Kreistangente</a>
	 * @param externalPoint
	 * @return
	 */
	@Override
	public List<IVector2> tangentialIntersections(final IVector2 externalPoint)
	{
		IVector2 dir = externalPoint.subtractNew(center());
		double d = dir.getLength2();
		assert radius() <= d;
		double alpha = Math.acos(radius() / d);
		double beta = dir.getAngle();
		
		List<IVector2> points = new ArrayList<IVector2>(2);
		points.add(center().addNew(new Vector2(beta + alpha).scaleTo(radius())));
		points.add(center().addNew(new Vector2(beta - alpha).scaleTo(radius())));
		return points;
	}
}
