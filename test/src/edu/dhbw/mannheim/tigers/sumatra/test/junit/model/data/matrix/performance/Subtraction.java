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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This class test the performance of JaMa and Matrix;
 * both in double
 * @author
 * 
 */
public class Subtraction extends APerformanceTest
{
	int	size	= 2;
	int	times	= 10;
	
	
	/**
	 * Testing the sub-functions with performance-fokus
	 * 
	 * alloc Matrix A (random)
	 * alloc Matrix B (random)
	 * subtract them (random)
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Subtraction");
		System.out.println("##########################################################");
	}
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
		size = 2;
		times = 10;
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMany()
	{
		output = true;
		times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void MatrixNew(int size, int times)
	{
		if (output)
		{
			System.out.println("MatrixNew");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix A = createRandomMatrix(size);
			final edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix B = createRandomMatrix(size);
			A.minus(B, false);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void MatrixOld(int size, int times)
	{
		if (output)
		{
			System.out.println("MatrixOld");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix A = createRandomMatrix(size);
			final edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix B = createRandomMatrix(size);
			A.minus(B, true);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void JaMa(int size, int times)
	{
		if (output)
		{
			System.out.println("JaMa");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = createRandomMatrixJaMa(size);
			final Jama.Matrix B = createRandomMatrixJaMa(size);
			A.minus(B);
			saveJamaMatrix(A);
		}
		endTimer();
	}
}
