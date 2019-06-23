/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2010
 * Author(s): 
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import org.junit.Test;


/**
 * This class test the performance of JaMa and Matrix;
 * both in double
 * @author 
 * 
 */
public class Transpose extends APerformanceTest
{
	/**
	 * Testing the transpose-functions with performance-fokus
	 * 
	 * alloc Matrix A (random)
	 * transpose it
	 */
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Transpose");
		System.out.println("##########################################################");
		
		output = false;
		int size = 2;
		int times = 10;
		JaMa(size, times);
		MatrixNew(size, times);

		
		output = true;
		times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		JaMa(size, times);
		MatrixNew(size, times);

	}
	
	public void MatrixNew(int size, int times)
	{
		if (output)
			System.out.println("MatrixNew");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix A = createRandomMatrix(size);
			A.transpose();
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void JaMa(int size, int times)
	{
		if (output)
			System.out.println("JaMa");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = createRandomMatrixJaMa(size);
			A.transpose();
			saveJamaMatrix(A);
		}
		endTimer();
	}
}
