/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2010
 * Author(s): Birgit
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
 * @author Birgit
 * 
 */
public class AdditionPerf extends APerformanceTest
{
	final int	size	= 2;
	final int	times	= 10;
	
	
	/**
	 * 
	 */
	public AdditionPerf()
	{
		super();
	}
	
	
	/**
	 * Testing the add-functions with performance-fokus
	 * 
	 * alloc Matrix A (random)
	 * alloc Matrix B (random)
	 * add them (random)
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Addition");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
	}
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
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
		System.out.println("Number of runs: " + timesEasyOps);
		JaMa(size, timesEasyOps);
		MatrixOld(size, timesEasyOps);
		MatrixNew(size, timesEasyOps);
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
			A.plus(B, false);
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
			A.plus(B, true);
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
			A.plus(B);
			saveJamaMatrix(A);
		}
		endTimer();
	}
}
