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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * testing initialize-function with 2d
 * 
 * @author Birgit
 * 
 */
public class Constructor2DPerf extends APerformanceTest
{
	int	times	= 10;
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor2D");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
	}
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
		testMatrixDoubleArrayArray();
		testMatrixDoubleArrayArrayIntInt();
		testJamaMatrixDoubleArrayArray();
		testJamaMatrixDoubleArrayArrayIntInt();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMany()
	{
		output = true;
		times = timesInit;
		System.out.println("Number of runs: " + times);
		testMatrixDoubleArrayArray();
		testMatrixDoubleArrayArrayIntInt();
		testJamaMatrixDoubleArrayArray();
		testJamaMatrixDoubleArrayArrayIntInt();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixDoubleArrayArrayIntInt()
	{
		if (output)
		{
			System.out.println("testMatrixDoubleArrayArrayIntInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } }, 3, 3);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixDoubleArrayArray()
	{
		if (output)
		{
			System.out.println("testMatrixDoubleArrayArray");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } });
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixDoubleArrayArrayIntInt()
	{
		if (output)
		{
			System.out.println("testJamaMatrixDoubleArrayArrayIntInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } }, 3, 3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixDoubleArrayArray()
	{
		if (output)
		{
			System.out.println("testJamaMatrixDoubleArrayArray");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(new double[][] { { 1, 2, 3 }, { -4, -5, -6 }, { 7, 8, 9 } });
			saveJamaMatrix(A);
		}
		endTimer();
	}
}
