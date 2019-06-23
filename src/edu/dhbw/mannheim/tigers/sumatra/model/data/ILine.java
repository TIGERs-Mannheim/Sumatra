/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

/**
 * Line interface. Keep in mind that a Line has
 * no start or end. Its length is infinite!
 * 
 * @author Malte
 * 
 */
public interface ILine
{
	public IVector2 supportVector();
	
	public IVector2 directionVector();
	
	/**
	 * Returns the slope of this line.
	 * Care using this function!
	 * If line is parallel to y-axis, exception is thrown.
	 * 
	 * @author Timo, Frieder
	 * @throws MathException If <code>{@link #isVertical()}</code>!!!
	 */
	public float getSlope() throws MathException;
	
	/**
	 * Returns the yIntercept of this Line.
	 * Care using this function! 
	 * If line is parallel to y-axis, exeption is thrown.
	 * 
	 * @author Timo, Frieder
	 * @throws MathException If <code>{@link #isVertical()}</code>!!!
	 */
	public float getYIntercept() throws MathException;
	
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
	public float getYValue(float x) throws MathException;
	
	/**
	 * Returns the x value to a given y input. <br>
	 * x = (y - n) / m <br>
	 * Beware of using when your line is horizontal!
	 * 
	 * @param y
	 * @return x
	 * @author Malte
	 * @throws MathException If <code>{@link #isHorizontal()}</code> or  <code>{@link #isVertical()}</code>!!!
	 */
	public float getXValue(float y) throws MathException;
	
	/**
	 * Returns an orthogonal Line to the given line. 
	 * Its direction vector is turned 90° clockwise and normalized.
	 * 
	 * @author Malte
	 */
	public ILine getOrthogonalLine();
	
	public boolean isHorizontal();
	
	public boolean isVertical();
	
	/**
	 * Checks, if a given point is on the left side of the line.
	 * Left and right are defined by the view direction of the direction vector.
	 * If the point is on the line, the answer is true.
	 * <pre>
	 * 
	 *    ^
	 *    |
	 *    |   +
	 *    |
	 *    |
	 * 
	 * Would be false!
	 * </pre>
	 * 
	 * @author Malte
	 */
	public boolean isPointOnTheLeft(IVector2 point);
}
