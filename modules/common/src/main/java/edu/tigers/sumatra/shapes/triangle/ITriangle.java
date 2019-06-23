/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.triangle;

import java.util.List;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.I2DShape;


/**
 * @author MarkG
 */
public interface ITriangle extends I2DShape
{
	
	/**
	 * Get all four points of the rectangle.
	 * Starting at topLeft, going counter clockwise.
	 * 
	 * @return List of corner points.
	 */
	List<IVector2> getCorners();
}
