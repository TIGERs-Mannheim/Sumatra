/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line.v2;

import java.util.Optional;


/**
 * Interface which represents an unbounded line. It has an initial starting point called the support vector and extends
 * indefinitely in both the positive and negative direction of the direction vector. As a result of this definition, two
 * instances of this interface with a support vector X and direction vector of {@code Y} and {@Code -Y} represent the
 * same logical line. To deal with this redundancy, the direction vector of a {@code ILine} instance is flipped if it is
 * located in the negative y-plane, i.e. has a negative y-component and the angle it spans with the x-axis
 * {@link ILine#getAngle()} is always greater or equal to zero.
 *
 * @author Lukas Magel
 */
public interface ILine extends ILineBase
{
	@Override
	ILine copy();
	
	
	/**
	 * Calculates x-coordinate of the point at which the line intersects the y-axis. Considering the standard definition
	 * {@code y = m * x + b} of a line, this function will return the {@code b} parameter.
	 * 
	 * @return
	 * 			The x-coordinate of the y intersection or an empty {@code Optional} if the line is not valid or parallel
	 *         to the y-axis.
	 */
	Optional<Double> getYIntercept();
	
	
	/**
	 * Calculates the x-value for a certain y-value. Considering the standard definition {@code y = m * x + b} of a line,
	 * this function would return {@code x = (y - b) / m}.
	 * 
	 * @param y
	 *           The y-value for which to calculate the x-value
	 * @return
	 * 			The x-value of the point on this line which corresponds to the specified y-value, or an empty optional if
	 *         the line is not valid or parallel to the y-axis.
	 */
	Optional<Double> getXValue(double y);
	
	
	/**
	 * Calculates the x-value for a certain y-value according to the standard definition {@code y = m * x + b} of a line.
	 *
	 * @param x
	 *           The x-value to calculate the y-value for
	 * @return
	 * 			The y-value of the point on the line which corresponds to the specified y-value, or an empty optional if
	 *         the line is not valid or parallel to the x-axis.
	 */
	Optional<Double> getYValue(double x);
	
	
	/**
	 * Returns a new line instance which is orthogonal to this instance. The direction vector is rotated 90 degrees
	 * counterclockwise and has the same length.
	 * 
	 * @return
	 * 			A new line instance which is orthogonal to the current one
	 */
	ILine getOrthogonalLine();
}
