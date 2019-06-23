/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.triangle;

import java.util.List;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.vector.IVector2;


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
	
	
	/**
	 * @return corner A
	 */
	IVector2 getA();
	
	
	/**
	 * @return corner B
	 */
	IVector2 getB();
	
	
	/**
	 * @return corner C
	 */
	IVector2 getC();
	
	
	/**
	 * Create a new Triangle (a,b',c'), such that
	 * line b'c' is orthogonal to ax
	 *
	 * <pre>
	 *  b             c
	 *   \           /
	 * b' \----x----/ c'
	 *     \   |   /
	 *      \  |  /
	 *       \^|^/
	 *        \|/<--alpha
	 *         a
	 * </pre>
	 *
	 * @param x position in triangle
	 * @return a new triangle
	 */
	ITriangle shortenToPoint(final IVector2 x);
	
	
	/**
	 * @return iff both triangles share a side
	 */
	boolean isNeighbour(ITriangle triangle);
	
	double area();
}