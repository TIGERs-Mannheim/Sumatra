/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.kick.presenter.sample.kick;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import java.util.List;
import java.util.Optional;


@Value
public class Poly2Fit
{
	double quadratic;
	double linear;
	double offset;
	double averageError; // sum of absolute differences


	public double getYValue(double x)
	{
		return quadratic * x * x + linear * x + offset;
	}


	/**
	 * Calculates a second order poly line through all points.
	 *
	 * @param points
	 * @return Optional Line.
	 */
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public static Optional<Poly2Fit> fromPointsList(final List<IVector2> points)
	{
		int numPoints = points.size();

		if (numPoints < 3)
		{
			return Optional.empty();
		}

		RealMatrix matA = new Array2DRowRealMatrix(numPoints, 3);
		RealVector b = new ArrayRealVector(numPoints);

		for (int i = 0; i < numPoints; i++)
		{
			matA.setEntry(i, 0, points.get(i).x() * points.get(i).x());
			matA.setEntry(i, 1, points.get(i).x());
			matA.setEntry(i, 2, 1.0);

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

		double quadratic = x.getEntry(0);
		double slope = x.getEntry(1);
		double offset = x.getEntry(2);

		double averageError = points.stream()
				.mapToDouble(p -> Math.abs(quadratic * p.x() * p.x() + slope * p.x() + offset - p.y()))
				.average().orElse(Double.NaN);

		return Optional.of(new Poly2Fit(quadratic, slope, offset, averageError));
	}
}
