/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.quadrilateral;

import java.util.List;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.triangle.ITriangle;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IQuadrilateral extends I2DShape
{
	/**
	 * @return the underlying two triangles
	 */
	List<ITriangle> getTriangles();
	
	
	/**
	 * @return a list of all corners in clockwise order, starting with
	 */
	List<IVector2> getCorners();
	
	
	/**
	 * @return all 4 edges of this quadrilateral
	 */
	List<ILine> getEdges();
}
