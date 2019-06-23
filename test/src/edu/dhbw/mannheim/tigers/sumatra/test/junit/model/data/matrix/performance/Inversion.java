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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * This class test the performance of JaMa and Matrix;
 * both in double
 * @author
 * 
 */
public class Inversion extends APerformanceTest
{
	/**
	 * Testing the add-functions with performance-fokus
	 * 
	 * alloc Matrix A (random)
	 * alloc Matrix B (random)
	 * add them (random)
	 */
	
	// vars, to controll the random-value
	int	count0	= 0;
	int	count1	= 0;
	int	count2	= 0;
	int	count3	= 0;
	
	int	size		= 2;
	int	times		= 10;
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Inversion");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
	}
	
	
	/**
	 */
	@Test
	public void testSize2()
	{
		size = 2;
		times = 10;
		
		output = false;
		
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testSize2Many()
	{
		size = 2;
		times = timesEasyOps;
		
		output = true;
		System.out.println("Number of runs: " + times + "size" + size);
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
	}
	
	
	/**
	 */
	@Test
	public void testSize6()
	{
		output = false;
		size = 6;
		times = 10;
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
	}
	
	
	/**
	 */
	@Test
	@Ignore
	public void testSize6Many()
	{
		output = true;
		size = 6;
		times = timesEasyOps / 10;
		System.out.println("Number of runs: " + times + "size" + size);
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
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
			final Matrix A = createInvertableMatrix(size);
			A.inverse(true);
			saveMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void MatrixChol(int size, int times)
	{
		if (output)
		{
			System.out.println("MatrixChol");
		}
		startTimer();
		for (int i = 0; i < times; i++)
		{
			final Matrix A = createInvertableMatrix(size);
			A.inverseByCholesky();
			saveMatrix(A);
		}
		endTimer();
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
			final Matrix A = createInvertableMatrix(size);
			A.inverse(false);
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
			final Jama.Matrix A = createInvertableMatrixJaMa(size);
			A.inverse();
			saveJamaMatrix(A);
		}
		endTimer();
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	
	public Jama.Matrix createInvertableMatrixJaMa(int size)
	{
		breakTimer();
		final double[][] array = createInvertableDouble(size);
		final Jama.Matrix A = new Jama.Matrix(array);
		
		restartTimer();
		return A;
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	public Matrix createInvertableMatrix(int size)
	{
		final long fctStart = System.currentTimeMillis();
		final double[][] array = createInvertableDouble(size);
		final Matrix A = new Matrix(array);
		
		final long fctStop = System.currentTimeMillis();
		final long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	public Matrix createInvertableCheckMatrix(int size)
	{
		final long fctStart = System.currentTimeMillis();
		final double[][] array = createInvertableDouble(size);
		final Matrix A = new Matrix(array);
		
		final long fctStop = System.currentTimeMillis();
		final long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	
	
	/**
	 * @param size
	 * @return
	 */
	public double[][] createInvertableDouble(int size)
	{
		if (size == 2)
		{
			return createInvertableDouble2x2();
		} else if (size == 6)
		{
			return createInvertableDouble6x6();
		} else
		{
			System.out.println("InvertMatrix is only implemented for 2x2-matrix");
			return null;
		}
	}
	
	
	private double[][] createInvertableDouble6x6()
	{
		return new double[][] { { -3, -1, 3, 0, -1, -6 }, { 3, 0, -5, 0, 0, -1 }, { 0, 0, 0, 3, 0, 0 },
				{ 0, 0, 0, 0, 0, 2 }, { 0, 1, 0, 0, 0, -1 }, { -2, -3, 0, 0, -5, 0 } };
	}
	
	
	private double[][] createInvertableDouble2x2()
	{
		final double[][] arr = new double[2][2];
		// Random r = new Random();
		// int val = r.nextInt() * 4;
		final int val = 0;
		// System.out.println(val);
		switch (val)
		{
			case 0:
				arr[0][0] = -5;
				arr[0][1] = 1;
				arr[1][0] = -1;
				arr[1][1] = 1;
				count0++;
				break;
			// case 1:
			// arr[0][0] = 1;
			// arr[0][1] = -1;
			// arr[1][0] = -4;
			// arr[1][1] = -1;
			// count1++;
			// break;
			// case 2:
			// arr[0][0] = 3;
			// arr[0][1] = -5;
			// arr[1][0] = -5;
			// arr[1][1] = -2;
			// count2++;
			// break;
			// case 3:
			// arr[0][0] = 0;
			// arr[0][1] = 1;
			// arr[1][0] = 5;
			// arr[1][1] = 2;
			// count3++;
			// break;
			default:
				System.out.println("Something is going wrong");
				break;
		
		}
		return arr;
	}
}
