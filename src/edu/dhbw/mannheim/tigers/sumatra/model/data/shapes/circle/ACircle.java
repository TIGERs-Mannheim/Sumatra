/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


/**
 * Geometrical representation of a circle.
 * 
 * 
 * @author Malte
 * 
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
	public float getArea()
	{
		return radius() * radius() * AngleMath.PI;
	}
	
	
	/**
	 * 
	 * Test if the point is in or on the given circle.
	 * 
	 * @author Steffen
	 * @author Dion
	 * 
	 */
	@Override
	public boolean isPointInShape(IVector2 point)
	{
		return isPointInShape(point, 0);
	}
	
	
	@Override
	public boolean isPointInShape(IVector2 point, float margin)
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
	public boolean isLineIntersectingShape(ILine line)
	{
		return !lineIntersections(line).isEmpty();
	}
	
	
	/**
	 * Calculates where a given line is intersecting this circle.
	 * Touching means intersecting!<br>
	 * 
	 * <a href="http://mathworld.wolfram.com/Circle-LineIntersection.html">
	 * Mathmatical Theory here.</a>
	 * 
	 * @param line line to check the intersection
	 * @return all intersection points, can be 0, 1 or 2
	 * @author Dion
	 */
	@Override
	public List<IVector2> lineIntersections(ILine line)
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
	 * 
	 * Touching is outside!
	 * 
	 * @author Dion
	 */
	@Override
	public Vector2 nearestPointOutside(IVector2 point)
	{
		final Vector2 direction = point.subtractNew(center());
		final float factor = radius() / direction.getLength2();
		
		if (factor > 1)
		{
			direction.multiply(factor);
			direction.add(center());
			return direction;
		}
		return new Vector2(point);
	}
	
	
	@Override
	public IVector2 nearestPointOnCircle(IVector2 point)
	{
		final Vector2 direction = point.subtractNew(center());
		final float factor = radius() / direction.getLength2();
		
		direction.multiply(factor);
		direction.add(center());
		return direction;
	}
	
	
	@Override
	public String toString()
	{
		return "Center = " + center().toString() + "\nRadius = " + radius();
	}
}
