/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 1, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance;

import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * 
 * @author Birgit
 * 
 */
public class SolvingLinearEquationSystemsPerf extends APerformanceTest
{
	/** */
	public Matrix			A	= null;
	/** */
	public Jama.Matrix	JA	= null;
	/** */
	public Matrix			B	= null;
	/** */
	public Jama.Matrix	JB	= null;
	
	
	/**
	 * 
	 */
	@Before
	public void start()
	{
		System.out.println("##########################################################");
		System.out.println(this.toString());
		System.out.println("##########################################################");
	}
	
	
	/**
	 * 
	 */
	@Before
	public void warmup()
	{
		output = false;
		final int times = 2;
		LR_Jama(times);
		LR_Matrix(times);
		Cholesky_Jama(times);
		Cholesky_Matrix(times);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testLR_Jama()
	{
		output = true;
		final int times = timesEasyOps / 5;
		System.out.println("LR_Jama: Number of runs: " + times);
		LR_Jama(times);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testLR_Matrix()
	{
		output = true;
		final int times = timesEasyOps / 5;
		System.out.println("LR_Matrix: Number of runs: " + times);
		LR_Matrix(times);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testCholesky_Jama()
	{
		output = true;
		final int times = timesEasyOps / 5;
		System.out.println("Cholesky_Jama: Number of runs: " + times);
		Cholesky_Jama(times);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testCholesky_Matrix()
	{
		output = true;
		final int times = timesEasyOps / 5;
		System.out.println("Number of runs: " + times);
		Cholesky_Matrix(times);
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void LR_Matrix(int times)
	{
		if (output)
		{
			System.out.println("LR_Matrix");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			fillDataMatrix();
			
			A.solveLR(B);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void Cholesky_Matrix(int times)
	{
		if (output)
		{
			System.out.println("Cholesky_Matrix");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			fillDataMatrix();
			A.solveCholesky(B);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void LR_Jama(int times)
	{
		if (output)
		{
			System.out.println("LR_Jama");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			fillDataJamaMatrix();
			final Jama.Matrix X = JA.lu().solve(JB);
			saveJamaMatrix(X);
		}
		endTimer();
	}
	
	
	/**
	 * 
	 * @param times
	 */
	public void Cholesky_Jama(int times)
	{
		if (output)
		{
			System.out.println("Cholesky_Jama");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			fillDataJamaMatrix();
			final Jama.Matrix X = JA.chol().solve(JB);
			saveJamaMatrix(X);
		}
		endTimer();
	}
	
	
	/**
	 * 
	 */
	public void fillDataMatrix()
	{
		breakTimer();
		
		A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		
		restartTimer();
	}
	
	
	/**
	 * 
	 */
	public void fillDataJamaMatrix()
	{
		breakTimer();
		
		JA = new Jama.Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		JB = new Jama.Matrix(new double[] { 3, 6, 4, 7 }, 4);
		
		restartTimer();
	}
	
}
