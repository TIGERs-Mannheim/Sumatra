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
public class Poly1Fit
{
	double linear;
	double offset;
	double averageError; // sum of absolute differences


	public double getYValue(double x)
	{
		return linear * x + offset;
	}


	/**
	 * Calculates a first order poly line through all points.
	 *
	 * @param points
	 * @return Optional Line.
	 */
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	public static Optional<Poly1Fit> fromPointsList(final List<IVector2> points)
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

		double averageError = points.stream()
				.mapToDouble(p -> Math.abs(slope * p.x() + offset - p.y()))
				.average().orElse(Double.NaN);

		return Optional.of(new Poly1Fit(slope, offset, averageError));
	}
}
