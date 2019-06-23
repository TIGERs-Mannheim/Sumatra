/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * Regular Rectangle interface.
 * All regular rectangles sides are either parallel to the X- or the Y-Axis.
 * Therefore, it is impossible to create skewed rectangles.
 * Every rectangle is defined by 2 points which are opposite vertices.
 * 
 * <pre>
 * topLeft                  topRight
 *       -------------------
 *       |     xExtend     |				
 *       |                 | yExtend
 *       |                 |
 *       -------------------
 * bottomLeft             bottomRight
 * </pre>
 * @author Malte
 * 
 */
public interface IRectangle extends I2DShape
{
	/**
	 * Absolute value of the length of the side
	 * that is parallel to the Y-Axis.
	 */
	public float yExtend();
	

	/**
	 * Absolute value of the length of the side
	 * that is parallel to the X-Axis.
	 */
	public float xExtend();
	

	/**
	 * Corner which is in the top-left regarding to a cartesian coordinate system.
	 */
	public IVector2 topLeft();
	

	/**
	 * Corner which is in the top-right regarding to a cartesian coordinate system.
	 */
	public IVector2 topRight();
	

	/**
	 * Corner which is in the bottom-left regarding to a cartesian coordinate system.
	 */
	public IVector2 bottomLeft();
	

	/**
	 * Corner which is in the bottom-right regarding to a cartesian coordinate system.
	 */
	public IVector2 bottomRight();
	

	/**
	 * Generates a random point within the shape.
	 */
	public IVector2 getRandomPointInShape();
	

	/**
	 * testing purpose, by GuntherB
	 */
	public IVector2 getMidPoint();
}
