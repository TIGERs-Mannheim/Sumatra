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
 * test the performance of creating matrix
 * @author Birgit
 * 
 */
public class Constructor0D extends APerformanceTest
{


	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor0D");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;
		testIdentity(times);
		testMatrixInt(times);
		testMatrixIntInt(times);
		testMatrixIntIntDouble(times);
		testJamaIdentity(times);
		testJamaMatrixInt(times);
		testJamaMatrixIntInt(times);
		testJamaMatrixIntIntDouble(times);
		

		output = true;
		times = timesInit;
		System.out.println("Number of runs: " + times);
		testIdentity(times);
		testMatrixInt(times);
		testMatrixIntInt(times);
		testMatrixIntIntDouble(times);
		testJamaIdentity(times);
		testJamaMatrixInt(times);
		testJamaMatrixIntInt(times);
		testJamaMatrixIntIntDouble(times);

	}
	
	public void testMatrixIntIntDouble(int times)
	{
		if (output)
			System.out.println("testMatrixIntIntDouble");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(3, 4, 1234.6);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testMatrixInt(int times)
	{
		if (output)
			System.out.println("testMatrixInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(3);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testMatrixIntInt(int times)
	{
		if (output)
			System.out.println("testMatrixIntInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = new Matrix(3, 4);
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void testIdentity(int times)
	{
		if (output)
			System.out.println("testIdentity");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = Matrix.Identity(3,4);
			saveMatrix(A);
		}
		endTimer();
	}
	
	public void testJamaMatrixIntIntDouble(int times)
	{
		if (output)
			System.out.println("testJamaMatrixIntIntDouble");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(3, 4, 1234.6);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaMatrixInt(int times)
	{
		if (output)
			System.out.println("testJamaMatrixInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(3,3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaMatrixIntInt(int times)
	{
		if (output)
			System.out.println("testJamaMatrixIntInt");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = new Jama.Matrix(3, 4);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	

	public void testJamaIdentity(int times)
	{
		if (output)
			System.out.println("testJamaIdentity");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Jama.Matrix A = Jama.Matrix.identity(3, 4);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
}
