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
 * testing initialize-function with 2d
 * 
 * @author Birgit
 * 
 */
public class Constructor2D extends APerformanceTest
{
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor2D");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;
		testMatrixDoubleArrayArray(times);
		testMatrixDoubleArrayArrayIntInt(times);
		testJamaMatrixDoubleArrayArray(times);
		testJamaMatrixDoubleArrayArrayIntInt(times);
		

		output = true;
		times = timesInit;
		System.out.println("Number of runs: " + times);
		testMatrixDoubleArrayArray(times);
		testMatrixDoubleArrayArrayIntInt(times);
		testJamaMatrixDoubleArrayArray(times);
		testJamaMatrixDoubleArrayArrayIntInt(times);
	}
	

	public void testMatrixDoubleArrayArrayIntInt(int times)
	{
		if (output)
			System.out.println("testMatrixDoubleArrayArrayIntInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } }, 3, 3);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testMatrixDoubleArrayArray(int times)
	{
		if (output)
			System.out.println("testMatrixDoubleArrayArray");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } });
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaMatrixDoubleArrayArrayIntInt(int times)
	{
		if (output)
			System.out.println("testJamaMatrixDoubleArrayArrayIntInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } }, 3, 3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaMatrixDoubleArrayArray(int times)
	{
		if (output)
			System.out.println("testJamaMatrixDoubleArrayArray");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } });
			saveJamaMatrix(A);
		}
		endTimer();
	}
}
