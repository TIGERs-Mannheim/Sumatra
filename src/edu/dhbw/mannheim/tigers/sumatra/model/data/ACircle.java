/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.11.2010
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;



/**
 * Geometrical representation of a circle.
 * 
 * 
 * @author Malte
 * 
 */
public abstract class ACircle implements ICircle
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------	


	//private Logger log = Logger.getLogger(getClass());
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
		return radius() * radius() * AIMath.PI;
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
		Vector2 tmp = point.subtractNew(center());
		
		if (tmp.getLength2() <= radius())
		{
			return true;
		}
		return false;
	}
	
	/**
	 *   Checks if a given line is intercecting this circle.
	 *   Touching means intercecting!
	 *   
	 *   @author Dion
	 */
	@Override
	public boolean isLineIntersectingShape(ILine line)
	{
		//throw new NotImplementedException();
		float dx = line.directionVector().x();
		float dy = line.directionVector().y();
		float dr = line.directionVector().getLength2();
		Vector2 newSupport = line.supportVector().subtractNew(this.center());
		float det = newSupport.x()*(newSupport.y()+dy)
						- (newSupport.x()+dx)*newSupport.y();
		
		if(this.radius()*this.radius() * dr*dr - det*det < 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	
	/**
	 *   Calculates where a given line is intersecting this circle.
	 *   Touching means intersecting!<br>
	 *   
	 *   <a href="http://mathworld.wolfram.com/Circle-LineIntersection.html">
	 *   Mathmatical Theory here.</a>
	 *   
	 *   @param line line to check the intersection
	 *   @return Vector2-Array, which retruns null if no intersection exists
	 *   @author Dion
	 */
	public List<IVector2> lineIntersections(ILine line)
	{
		List<IVector2> result = new ArrayList<IVector2>();

		float dx = line.directionVector().x();
		float dy = line.directionVector().y();
		float dr = line.directionVector().getLength2();
		Vector2 newSupport = line.supportVector().subtractNew(this.center());
		float det = newSupport.x()*(newSupport.y()+dy)
						- (newSupport.x()+dx)*newSupport.y();
		
		float inRoot = this.radius()*this.radius() * dr*dr - det*det;
		
		if(inRoot < 0)
		{
			return result;
		}
				
		if(inRoot == 0)
		{
			Vector2 temp = new Vector2();
			temp.setX((det*dy)/(dr*dr));
			temp.setY((-det*dx)/(dr*dr));
			// because of moved coordinate system (newSupport):
			temp.add(this.center());

			result.add(temp);
			
			return result;
		}
		else
		{
			float sqRoot = AIMath.sqrt(inRoot);
			Vector2 temp1 = new Vector2();
			Vector2 temp2 = new Vector2();

			temp1.setX((det*dy+dx*sqRoot)/(dr*dr));
			temp1.setY((-det*dx+dy*sqRoot)/(dr*dr));
			temp2.setX((det*dy-dx*sqRoot)/(dr*dr));
			temp2.setY((-det*dx-dy*sqRoot)/(dr*dr));
			// because of moved coordinate system (newSupport):
			temp1.add(this.center());
			temp2.add(this.center());

			result.add(temp1);
			result.add(temp2);
			return result;
		}
	}

	
	/**
	 *   Returns the nearest point outside a shape to a given point inside the shape.
	 *   If the given point is outside the shape, return the point.
	 *
	 *   Touching is outside!
	 *   
	 *   @author Dion
	 */
	@Override
	public Vector2 nearestPointOutside(IVector2 point)
	{
		Vector2 direction = point.subtractNew(this.center());
		float factor = this.radius()/direction.getLength2();
		
		if(factor > 1)
		{
			direction.multiply(factor);
			direction.add(this.center());
			return direction;
		}
		return new Vector2(point);
	}
	
	@Override
	public String toString()
	{
		return "Center = "+center().toString()+"\nRadius = "+radius();
	}
}
