/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * Test the performance of A*_P_*AT;
 * "_P_" means, that it is not allowed, to change the Matrix P
 * @author Birgit
 * 
 */
public class Call_AtimesPtimesAT extends APerformanceTest
{
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("AtimesPtimesAT");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;
		Jama(times);
		Matrix(times);
		

		output = true;
		times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		Jama(times);
		Matrix(times);
	}
	

	public void Matrix(int times)
	{
		if (output)
			System.out.println("MatrixNew");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			Matrix A = new Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			Matrix P = new Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			
			A.times(P).times(A.transpose());
		}
		endTimer();
	}
	

	public void Jama(int times)
	{
		if (output)
			System.out.println("JaMa");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			Jama.Matrix A = new Jama.Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			Jama.Matrix P = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			

			A.times(P).times(A.transpose());
		}
		endTimer();
	}
	
}
