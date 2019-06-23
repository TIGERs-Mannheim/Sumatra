/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;


/**
 * Circle interface.
 * 
 * @author Malte
 * 
 */
public interface ICircle extends I2DShape
{
	/**
	 * 
	 * @return
	 */
	float radius();
	
	
	/**
	 * 
	 * @return
	 */
	IVector2 center();
	
	
	/**
	 * Get the intersection points of the circle and line
	 * 
	 * @param line
	 * @return
	 */
	List<IVector2> lineIntersections(ILine line);
	
	
	/**
	 * 
	 * @param point
	 * @param margin like the margin in css, the area around the shape with the thickness of this value
	 * @return
	 */
	boolean isPointInShape(IVector2 point, float margin);
	
	
	/**
	 * Nearest point on circle which is the intersection point between center and given point
	 * 
	 * @param point
	 * @return
	 */
	IVector2 nearestPointOnCircle(IVector2 point);
}
