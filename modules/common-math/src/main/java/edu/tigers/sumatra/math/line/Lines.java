/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import java.util.List;
import java.util.Optional;


/**
 * Factory for creating new lines. It is intended that you only get interfaces.
 * You will never know the actual implementation and you won't be able to create them on your own.
 * This way, it is easier to maintain the implementation without touching a lot of code.
 *
 * @author Lukas Magel
 */
public final class Lines
{

	private Lines()
	{

	}


	/**
	 * Create a new line segment which spans from the specified point {@code start} to the second point {@code end}.
	 *
	 * @param start The point from which the line segment extends
	 * @param end   The point which the line segment extends to
	 * @return A new line segment instance
	 * @throws IllegalArgumentException If the points {@code start} and {@code end} are identical. Please perform a check
	 *                                  in your code before you call this method!
	 */
	public static ILineSegment segmentFromPoints(final IVector2 start, final IVector2 end)
	{
		return LineSegment.fromPoints(start, end);
	}


	/**
	 * Create a new line instance which is defined by a single {@code supportVector}, i.e. the starting point, and the
	 * offset/displacement from start to end. This means that the end point is calculated as follows:
	 * {@code supportVector + displacement}.
	 *
	 * @param supportVector The origin of this line segment
	 * @param offsetVector  The offset/displacement between start and end of this line {@code end - start}
	 * @return A new line instance
	 * @throws IllegalArgumentException If the {@code displacement} has a length of zero
	 */
	public static ILineSegment segmentFromOffset(final IVector2 supportVector, final IVector2 offsetVector)
	{
		return LineSegment.fromOffset(supportVector, offsetVector);
	}


	/**
	 * Create a new {@link IHalfLine} instance which extends from the specified {@code supportVector} in the direction of
	 * {@code directionVector} indefinitely.
	 *
	 * @param supportVector   The support vector which defines the starting point of the created half-line
	 * @param directionVector The direction vector which defines the direction in which the half-line extends
	 * @return A new {@code IHalfLine} instance
	 * @throws IllegalArgumentException If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *                                  call this method!
	 */
	public static IHalfLine halfLineFromDirection(final IVector2 supportVector, final IVector2 directionVector)
	{
		return HalfLine.fromDirection(supportVector, directionVector);
	}


	/**
	 * Create a new {@link IHalfLine} instance which extends from the specified {@code start} in the direction of
	 * {@code end} indefinitely.
	 *
	 * @param start The point from which the line extends
	 * @param end   The point which the line extends to
	 * @return A new {@code IHalfLine} instance
	 * @throws IllegalArgumentException If the {@code start} and {@code end} are equal. Please perform a check in your code before you
	 *                                  call this method!
	 */
	public static IHalfLine halfLineFromPoints(final IVector2 start, final IVector2 end)
	{
		return HalfLine.fromDirection(start, end.subtractNew(start));
	}


	/**
	 * Creates supportPointA new line instance which runs through the two specified points {@code supportPointA} and
	 * {@code supportPointB}. The direction vector of the created line points from {@code supportPointA} to
	 * {@code supportPointB}.
	 *
	 * @param supportPointA The first point to define the line
	 * @param supportPointB The seconds point to define the line
	 * @return A new line instance which runs through both points
	 * @throws IllegalArgumentException If the support points are identical, i.e. {@code supportPointA.equals(supportPointB}
	 */
	public static ILine lineFromPoints(final IVector2 supportPointA, final IVector2 supportPointB)
	{
		return Line.fromPoints(supportPointA, supportPointB);
	}


	/**
	 * Creates a new line instance which uses the specified {@code supportVector} and {@code directionVector}.
	 *
	 * @param supportVector   The support vector to use for the line
	 * @param directionVector The direction vector to use for the line
	 * @return A new line instance which is defined by the two parameters
	 * @throws IllegalArgumentException If the {@code directionVector} has a length of zero. Please perform a check in your code before you
	 *                                  call this method!
	 */
	public static ILine lineFromDirection(final IVector2 supportVector, final IVector2 directionVector)
	{
		return Line.fromDirection(supportVector, directionVector);
	}


	/**
	 * Calculates a regression line through all points.
	 *
	 * @param points
	 * @return Optional Line.
	 */
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public static Optional<ILineSegment> regressionLineFromPointsList(final List<IVector2> points)
	{
		int numPoints = points.size();

		if (numPoints < 2)
		{
			return Optional.empty();
		}

		RealMatrix matA = new Array2DRowRealMatrix(numPoints, 2);
		RealVector b = new ArrayRealVector(numPoints);

		for (int i = 0; i < numPoints; i++)
		{
			matA.setEntry(i, 0, points.get(i).x());
			matA.setEntry(i, 1, 1.0);

			b.setEntry(i, points.get(i).y());
		}

		DecompositionSolver solver = new QRDecomposition(matA).getSolver();
		RealVector x;
		try
		{
			x = solver.solve(b);
		} catch (SingularMatrixException e)
		{
			return Optional.empty();
		}

		double slope = x.getEntry(0);
		double offset = x.getEntry(1);

		double x1 = points.get(0).x();
		double x2 = points.get(points.size() - 1).x();

		IVector2 p1 = Vector2f.fromXY(x1, (x1 * slope) + offset);
		IVector2 p2 = Vector2f.fromXY(x2, (x2 * slope) + offset);

		return Optional.of(LineSegment.fromPoints(p1, p2));
	}
}
