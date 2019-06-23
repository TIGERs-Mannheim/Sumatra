/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 10, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * TODO Birgit, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Birgit
 * 
 */
public class Constructor1D extends APerformanceTest
{
	/**
	 * Testing the add-functions with performance-fokus
	 * 
	 * alloc Matrix A (random)
	 * alloc Matrix B (random)
	 * add them (random)
	 */
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor1D");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;
		testMatrixTrueDoubleArrayIntIntBoolean(times);
		testMatrixFalseDoubleArrayIntIntBoolean(times);
		testJamaMatrixDoubleArrayIntIntBoolean(times);
		

		output = true;
		times = timesInit;
		System.out.println("Number of runs: " + times);
		testMatrixTrueDoubleArrayIntIntBoolean(times);
		testMatrixFalseDoubleArrayIntIntBoolean(times);
		testJamaMatrixDoubleArrayIntIntBoolean(times);
	}
	

	public void testMatrixTrueDoubleArrayIntIntBoolean(int times)
	{
		if (output)
			System.out.println("testMatrixTrueDoubleArrayIntIntBoolean");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3, 3, true);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testMatrixFalseDoubleArrayIntIntBoolean(int times)
	{
		if (output)
			System.out.println("testMatrixFalseDoubleArrayIntIntBoolean");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3, 3, false);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaMatrixDoubleArrayIntIntBoolean(int times)
	{
		if (output)
			System.out.println("testJamaMatrixDoubleArrayIntIntBoolean");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(new double[] { 1, 2, 3, -4, -5, -6, 7, 8, 9 }, 3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
}