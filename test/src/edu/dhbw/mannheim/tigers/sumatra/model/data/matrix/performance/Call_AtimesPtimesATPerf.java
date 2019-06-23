/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * Test the performance of A*_P_*AT;
 * "_P_" means, that it is not allowed, to change the Matrix P
 * @author Birgit
 * 
 */
public class Call_AtimesPtimesATPerf extends APerformanceTest
{
	int	times	= 10;
	
	
	/**
	 * 
	 */
	public Call_AtimesPtimesATPerf()
	{
		super();
	}
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("AtimesPtimesAT");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
	}
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
		Jama(times);
		Matrix(times);
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMany()
	{
		output = true;
		System.out.println("Number of runs: " + timesEasyOps);
		Jama(timesEasyOps);
		Matrix(timesEasyOps);
	}
	
	
	private void Matrix(int times)
	{
		if (output)
		{
			System.out.println("MatrixNew");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			final Matrix A = new Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			final Matrix P = new Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			
			A.times(P).times(A.transpose());
		}
		endTimer();
	}
	
	
	private void Jama(int times)
	{
		if (output)
		{
			System.out.println("JaMa");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			final Jama.Matrix A = new Jama.Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			final Jama.Matrix P = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			
			
			A.times(P).times(A.transpose());
		}
		endTimer();
	}
	
}
