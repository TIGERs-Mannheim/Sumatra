/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.I2DShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


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
