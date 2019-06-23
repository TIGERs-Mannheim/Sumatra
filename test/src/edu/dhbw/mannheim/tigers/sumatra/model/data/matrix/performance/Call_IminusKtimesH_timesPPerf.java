/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 10, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * Benchmark the Matrix operations "Matrix" and "Jama".
 * 
 * @author Birgit
 * 
 */
public class Call_IminusKtimesH_timesPPerf extends APerformanceTest
{
	/**
	 */
	@Before
	public void start()
	{
		System.out.println("##########################################################");
		System.out.println("IminusKtimesH_timesP");
		System.out.println("##########################################################");
	}
	
	
	/**
	 */
	@Before
	public void warmup()
	{
		output = false;
		final int times = 10;
		jama(times);
		matrix(times);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void benchMatrix()
	{
		output = true;
		final int times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		matrix(times);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void benchJama()
	{
		output = true;
		final int times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		jama(times);
	}
	
	
	/**
	 * @param times
	 */
	private void matrix(int times)
	{
		if (output)
		{
			System.out.println("MatrixNew");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			final Matrix I = Matrix.identity(2, 2);
			final Matrix H = new Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			final Matrix P = new Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			final Matrix K = new Matrix(new double[][] { { 0, 5 }, { 3, Math.random() } });
			
			final Matrix X = (I.minus(K.times(H))).times(P);
			saveMatrix(X);
		}
		endTimer();
	}
	
	
	private void jama(int times)
	{
		if (output)
		{
			System.out.println("JaMa");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			final Jama.Matrix I = Jama.Matrix.identity(2, 2);
			final Jama.Matrix H = new Jama.Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			final Jama.Matrix P = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			final Jama.Matrix K = new Jama.Matrix(new double[][] { { 0, 5 }, { 3, Math.random() } });
			
			final Jama.Matrix X = (I.minus(K.times(H))).times(P);
			saveJamaMatrix(X);
		}
		endTimer();
	}
}
