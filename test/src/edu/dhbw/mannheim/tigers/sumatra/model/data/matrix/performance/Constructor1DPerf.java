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
 * 
 * Testing the add-functions with performance-fokus
 * 
 * alloc Matrix A (random)
 * alloc Matrix B (random)
 * add them (random)
 * 
 * @author Birgit
 * 
 */
public class Constructor1DPerf extends APerformanceTest
{
	
	int	times	= timesInit;
	
	
	/**
	 */
	@Before
	public void start()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor1D");
		System.out.println("##########################################################");
	}
	
	
	/**
	 */
	@Before
	public void warmup()
	{
		output = false;
		times = 10;
		testMatrixTrueDoubleArrayIntIntBoolean();
		testMatrixFalseDoubleArrayIntIntBoolean();
		testJamaMatrixDoubleArrayIntIntBoolean();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixTrue()
	{
		output = true;
		times = timesInit;
		System.out.println("testMatrixTrueDoubleArrayIntIntBoolean: Number of runs: " + times);
		testMatrixTrueDoubleArrayIntIntBoolean();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixFalse()
	{
		output = true;
		times = timesInit;
		System.out.println("testMatrixFalseDoubleArrayIntIntBoolean: Number of runs: " + times);
		testMatrixFalseDoubleArrayIntIntBoolean();
	}
	
	
	/**
	 */
	@Test
	public void testJamaMatrix()
	{
		output = true;
		System.out.println("testJamaMatrixDoubleArrayIntIntBoolean: Number of runs: " + times);
		testJamaMatrixDoubleArrayIntIntBoolean();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixTrueDoubleArrayIntIntBoolean()
	{
		if (output)
		{
			System.out.println("testMatrixTrueDoubleArrayIntIntBoolean");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3, 3, true);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixFalseDoubleArrayIntIntBoolean()
	{
		if (output)
		{
			System.out.println("testMatrixFalseDoubleArrayIntIntBoolean");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3, 3, false);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixDoubleArrayIntIntBoolean()
	{
		if (output)
		{
			System.out.println("testJamaMatrixDoubleArrayIntIntBoolean");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
}