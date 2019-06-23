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
 * test the performance of creating matrix
 * @author Birgit
 * 
 */
public class Constructor0DPerf extends APerformanceTest
{
	int	times	= 10;
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Constructor0D");
		System.out.println("##########################################################");
	}
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
		testIdentity();
		testMatrixInt();
		testMatrixIntInt();
		testMatrixIntIntDouble();
		testJamaIdentity();
		testJamaMatrixInt();
		testJamaMatrixIntInt();
		testJamaMatrixIntIntDouble();
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
		testIdentity();
		testMatrixInt();
		testMatrixIntInt();
		testMatrixIntIntDouble();
		testJamaIdentity();
		testJamaMatrixInt();
		testJamaMatrixIntInt();
		testJamaMatrixIntIntDouble();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixIntIntDouble()
	{
		if (output)
		{
			System.out.println("testMatrixIntIntDouble");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(3, 4, 1234.6);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixInt()
	{
		if (output)
		{
			System.out.println("testMatrixInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(3);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMatrixIntInt()
	{
		if (output)
		{
			System.out.println("testMatrixIntInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = new Matrix(3, 4);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testIdentity()
	{
		if (output)
		{
			System.out.println("testIdentity");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = Matrix.identity(3, 4);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixIntIntDouble()
	{
		if (output)
		{
			System.out.println("testJamaMatrixIntIntDouble");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(3, 4, 1234.6);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixInt()
	{
		if (output)
		{
			System.out.println("testJamaMatrixInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(3, 3);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaMatrixIntInt()
	{
		if (output)
		{
			System.out.println("testJamaMatrixIntInt");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = new Jama.Matrix(3, 4);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testJamaIdentity()
	{
		if (output)
		{
			System.out.println("testJamaIdentity");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Jama.Matrix A = Jama.Matrix.identity(3, 4);
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
}
