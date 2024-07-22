/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * This interface represents a bounded line segment. It extends from a starting point A to an ending point B. A line
 * segment has a well-defined length.
 *
 * @author Lukas Magel
 */
public interface ILineSegment extends ILineBase, IBoundedPath
{
	@Override
	ILineSegment copy();


	/**
	 * Converts this instance into a {@link IHalfLine}. The resulting half-line extends from the start (i.e.
	 * {@link #getPathStart()} in the direction of the {@link #directionVector()}. Please note that the resulting
	 * {@link ILine} instance is only valid if this instance is also valid.
	 *
	 * @return A half-line instance which is centered at {@link #getPathStart()} and points in the same direction as this
	 * line segment
	 */
	IHalfLine toHalfLine();


	@Override
	default IVector2 supportVector()
	{
		return getPathStart();
	}


	/**
	 * Create a new line segment with a positive or negative margin.
	 * If the margin is negative and the line segment length is less than |margin*2|, the behavior is undefined.
	 *
	 * @param margin the margin to apply to both ends
	 * @return a new line segment
	 */
	ILineSegment withMargin(double margin);


	/**
	 * Returns a list of points on a line. All points are separated by
	 * stepSize. Start and end of the line are always in the list.
	 *
	 * @param stepSize absolute with of each steps
	 * @return a list of positions on the line, all separated by stepSize
	 */
	List<IVector2> getSteps(double stepSize);


	/**
	 * Smallest distance between this line segment and the given.
	 *
	 * @param line
	 * @return
	 */
	double distanceTo(final ILineSegment line);
}
