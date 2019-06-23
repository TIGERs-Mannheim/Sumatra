/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * Line interface. Keep in mind that a Line has
 * no start or end. Its length is infinite!
 * 
 * @author Malte
 */
public interface ILine
{
	/**
	 * @return
	 */
	IVector2 supportVector();
	
	
	/**
	 * @return
	 */
	IVector2 directionVector();
	
	
	/**
	 * Returns the slope of this line.
	 * Care using this function!
	 * If line is parallel to y-axis, exception is thrown.
	 * 
	 * @author Timo, Frieder
	 * @return
	 * @throws MathException If <code>{@link #isVertical()}</code>!!!
	 */
	float getSlope() throws MathException;
	
	
	/**
	 * Returns the yIntercept of this Line.
	 * Care using this function!
	 * If line is parallel to y-axis, exeption is thrown.
	 * 
	 * @author Timo, Frieder
	 * @return
	 * @throws MathException If <code>{@link #isVertical()}</code>!!!
	 */
	float getYIntercept() throws MathException;
	
	
	/**
	 * Returns the y value to a given x input. <br>
	 * y = m * x + n<br>
	 * Beware of using when your line is vertical!
	 * 
	 * @param x
	 * @return y
	 * @author Malte
	 * @throws MathException If <code>{@link #isVertical()}</code>!!!
	 */
	float getYValue(float x) throws MathException;
	
	
	/**
	 * Returns the x value to a given y input. <br>
	 * x = (y - n) / m <br>
	 * Beware of using when your line is horizontal!
	 * 
	 * @param y
	 * @return x
	 * @author Malte
	 * @throws MathException If <code>{@link #isHorizontal()}</code>!!!
	 */
	float getXValue(float y) throws MathException;
	
	
	/**
	 * Returns an orthogonal Line to the given line.
	 * Its direction vector is turned 90� clockwise and normalized.
	 * 
	 * @author Malte
	 * @return
	 */
	ILine getOrthogonalLine();
	
	
	/**
	 * @return
	 */
	boolean isHorizontal();
	
	
	/**
	 * @return
	 */
	boolean isVertical();
	
	
	/**
	 * Checks if a point lies in the direction of the line.
	 * 
	 * @param point Point to check
	 * @return True if the point is in front of line.
	 * @author AndreR
	 */
	boolean isPointInFront(IVector2 point);
}
