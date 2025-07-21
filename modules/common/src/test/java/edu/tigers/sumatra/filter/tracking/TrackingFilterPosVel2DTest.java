/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.11.2016
 * Author(s): AndreR <andre@ryll.cc>
 * *********************************************************
 */
package edu.tigers.sumatra.filter.tracking;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
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
class TrackingFilterPosVel2DTest
{
	@Test
	void constantPositionTest()
	{
		final int numSamples = 100;
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(4);
		TrackingFilterPosVel2D filter = new TrackingFilterPosVel2D(initialState, 1, 0.1, 2, 0);
		
		RealMatrix result = new Array2DRowRealMatrix(numSamples, 8); // [pos vel unc inno]
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double posX = gen.nextGaussian();
			double posY = gen.nextGaussian();
			
			filter.predict(time);
			filter.correct(Vector2.fromXY(posX, posY));
			
			result.setEntry(i, 0, filter.getPositionEstimate().x());
			result.setEntry(i, 1, filter.getPositionEstimate().y());
			result.setEntry(i, 2, filter.getVelocityEstimate().x());
			result.setEntry(i, 3, filter.getVelocityEstimate().y());
			result.setEntry(i, 4, filter.getPositionUncertainty().x());
			result.setEntry(i, 5, filter.getPositionUncertainty().y());
			result.setEntry(i, 6, filter.getPositionInnovation().x());
			result.setEntry(i, 7, filter.getPositionInnovation().y());
		}
		
		double[] lastRow = result.getRow(result.getRowDimension() - 1);

		assertEquals(0, lastRow[0], 10);
		assertEquals(0, lastRow[2], 100);
		assertEquals(0, lastRow[4], 10);
		assertEquals(0, lastRow[6], 10);
	}
	
	
	@Test
	void constantVelocityTest()
	{
		final int numSamples = 101;
		final double velocity = 1000; // [mm/s]
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(4);
		TrackingFilterPosVel2D filter = new TrackingFilterPosVel2D(initialState, 1, 0.1, 2, 0);
		
		RealMatrix result = new Array2DRowRealMatrix(numSamples, 8); // [pos vel unc inno]
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double posX = (time * 1e-9 * velocity) + gen.nextGaussian();
			double posY = (time * 1e-9 * velocity) + gen.nextGaussian();
			
			filter.predict(time);
			filter.correct(Vector2.fromXY(posX, posY));
			
			result.setEntry(i, 0, filter.getPositionEstimate().x());
			result.setEntry(i, 1, filter.getPositionEstimate().y());
			result.setEntry(i, 2, filter.getVelocityEstimate().x());
			result.setEntry(i, 3, filter.getVelocityEstimate().y());
			result.setEntry(i, 4, filter.getPositionUncertainty().x());
			result.setEntry(i, 5, filter.getPositionUncertainty().y());
			result.setEntry(i, 6, filter.getPositionInnovation().x());
			result.setEntry(i, 7, filter.getPositionInnovation().y());
		}
		
		double[] lastRow = result.getRow(result.getRowDimension() - 1);

		assertEquals(velocity, lastRow[0], 5);
		assertEquals(velocity, lastRow[2], 50);
		assertEquals(0, lastRow[4], 10);
		assertEquals(0, lastRow[6], 10);
	}
	
	
	@Test
	void missingUpdatesTest()
	{
		final int numSamples = 100;
		Random gen = new Random(0);
		
		RealVector initialState = new ArrayRealVector(4);
		TrackingFilterPosVel2D filter = new TrackingFilterPosVel2D(initialState, 1, 0.1, 2, 0);
		
		for (int i = 0; i < numSamples; i++)
		{
			long time = (long) (i * 0.01 * 1e9);
			double posX = gen.nextGaussian();
			double posY = gen.nextGaussian();
			IVector2 lastUnc = filter.getPositionUncertainty();
			
			if ((i > 50) && (i < 70))
			{
				filter.predict(time);
				assertTrue(filter.getPositionUncertainty().x() > lastUnc.x());
				assertTrue(filter.getPositionUncertainty().y() > lastUnc.y());
			} else if (i == 70)
			{
				filter.predict(time);
				filter.correct(Vector2.fromXY(posX, posY));
				assertTrue(filter.getPositionUncertainty().x() < lastUnc.x());
				assertTrue(filter.getPositionUncertainty().y() < lastUnc.y());
			} else
			{
				filter.predict(time);
				filter.correct(Vector2.fromXY(posX, posY));
			}
		}
	}
}
