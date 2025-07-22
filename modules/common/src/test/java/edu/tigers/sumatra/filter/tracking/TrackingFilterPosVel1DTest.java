/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2016
 * Author(s): AndreR <andre@ryll.cc>
 * *********************************************************
 */
package edu.tigers.sumatra.filter.tracking;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author AndreR <andre@ryll.cc>
 */
class TrackingFilterPosVel1DTest
{
	@Test
	void constantPositionTest()
	{
		final int numSamples = 100;
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(2);
		TrackingFilterPosVel1D filter = new TrackingFilterPosVel1D(initialState, 1, 0.1, 2, 0);
		
		RealMatrix result = new Array2DRowRealMatrix(numSamples, 4); // [pos vel unc inno]
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double pos = gen.nextGaussian();
			
			filter.predict(time);
			filter.correct(pos);
			
			result.setEntry(i, 0, filter.getPositionEstimate());
			result.setEntry(i, 1, filter.getVelocityEstimate());
			result.setEntry(i, 2, filter.getPositionUncertainty());
			result.setEntry(i, 3, filter.getPositionInnovation());
		}
		
		double[] lastRow = result.getRow(result.getRowDimension() - 1);

		assertEquals(0, lastRow[0], 10);
		assertEquals(0, lastRow[1], 100);
		assertEquals(0, lastRow[2], 10);
		assertEquals(0, lastRow[3], 10);
	}
	
	
	@Test
	void constantVelocityTest()
	{
		final int numSamples = 101;
		final double velocity = 1000; // [mm/s]
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(2);
		TrackingFilterPosVel1D filter = new TrackingFilterPosVel1D(initialState, 1, 0.1, 2, 0);
		
		RealMatrix result = new Array2DRowRealMatrix(numSamples, 4); // [pos vel unc inno]
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double pos = (time * 1e-9 * velocity) + gen.nextGaussian();
			
			filter.predict(time);
			filter.correct(pos);
			
			result.setEntry(i, 0, filter.getPositionEstimate());
			result.setEntry(i, 1, filter.getVelocityEstimate());
			result.setEntry(i, 2, filter.getPositionUncertainty());
			result.setEntry(i, 3, filter.getPositionInnovation());
		}
		
		double[] lastRow = result.getRow(result.getRowDimension() - 1);

		assertEquals(velocity, lastRow[0], 5);
		assertEquals(velocity, lastRow[1], 50);
		assertEquals(0, lastRow[2], 10);
		assertEquals(0, lastRow[3], 10);
	}
	
	
	@Test
	void missingUpdatesTest()
	{
		final int numSamples = 100;
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(2);
		TrackingFilterPosVel1D filter = new TrackingFilterPosVel1D(initialState, 1, 0.1, 2, 0);
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double pos = gen.nextGaussian();
			double lastUnc = filter.getPositionUncertainty();
			
			if ((i > 50) && (i < 70))
			{
				filter.predict(time);
				assertTrue(filter.getPositionUncertainty() > lastUnc);
			} else if (i == 70)
			{
				filter.predict(time);
				filter.correct(pos);
				assertTrue(filter.getPositionUncertainty() < lastUnc);
			} else
			{
				filter.predict(time);
				filter.correct(pos);
			}
		}
	}
}
