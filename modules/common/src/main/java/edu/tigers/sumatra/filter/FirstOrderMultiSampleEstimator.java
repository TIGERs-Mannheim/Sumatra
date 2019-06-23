/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.filter;

import java.util.Optional;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Estimate a first order function by using a circular buffer of multiple samples.
 * 
 * <pre>
 * y = b + m * x
 * </pre>
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class FirstOrderMultiSampleEstimator
{
	private final int numSamples;
	
	private final RealMatrix matA;
	private final RealVector b;
	
	private int index;
	private int samplesUsed;
	private IVector2 bestEstimate;
	
	
	/**
	 * Constructor.
	 * 
	 * @param numSamples
	 */
	public FirstOrderMultiSampleEstimator(final int numSamples)
	{
		this.numSamples = numSamples;
		b = new ArrayRealVector(numSamples);
		matA = new Array2DRowRealMatrix(numSamples, 2);
		
		for (int i = 0; i < numSamples; i++)
		{
			matA.setEntry(i, 0, 1.0);
		}
	}
	
	
	/**
	 * Add a new sample.
	 * 
	 * @param x
	 * @param y
	 */
	public void addSample(final double x, final double y)
	{
		b.setEntry(index, y);
		matA.setEntry(index, 1, x);
		
		index = (index + 1) % numSamples;
		
		if (samplesUsed < numSamples)
		{
			samplesUsed++;
		}
		
		compute();
	}
	
	
	@SuppressWarnings("squid:S1166") // Exception from solver not logged
	private void compute()
	{
		RealVector result;
		
		if (samplesUsed < 2)
		{
			return;
		}
		
		if (samplesUsed < numSamples)
		{
			DecompositionSolver solver = new QRDecomposition(matA.getSubMatrix(0, samplesUsed - 1, 0, 1)).getSolver();
			try
			{
				result = solver.solve(b.getSubVector(0, samplesUsed));
			} catch (SingularMatrixException e)
			{
				return;
			}
		} else
		{
			DecompositionSolver solver = new QRDecomposition(matA).getSolver();
			try
			{
				result = solver.solve(b);
			} catch (SingularMatrixException e)
			{
				return;
			}
		}
		
		bestEstimate = Vector2.fromReal(result);
	}
	
	
	/**
	 * Clear all samples and the best estimate.
	 */
	public void reset()
	{
		samplesUsed = 0;
		index = 0;
		bestEstimate = null;
	}
	
	
	/**
	 * @return the bestEstimate (x indicates offset, y indicates slope)
	 */
	public Optional<IVector2> getBestEstimate()
	{
		return Optional.ofNullable(bestEstimate);
	}
}
