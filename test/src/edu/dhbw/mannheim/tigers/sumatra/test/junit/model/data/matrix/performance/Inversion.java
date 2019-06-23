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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


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
	
	
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Inversion");
		System.out.println("##########################################################");
		
		output = false;
		int size = 2;
		int times = 10;
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
		
		output = true;
		times = timesEasyOps;
		System.out.println("Number of runs: " + times+"size"+size);
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
		
		output = false;
		size = 6;
		times = 10;
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);

		output = true;
		times = timesEasyOps/10;
		System.out.println("Number of runs: " + times+"size"+size);
		JaMa(size, times);
		MatrixOld(size, times);
		MatrixNew(size, times);
		MatrixChol(size, times);
		
	}
	

	public void MatrixOld(int size, int times)
	{
		if (output)
			System.out.println("MatrixOld");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = createInvertableMatrix(size);
			A.inverse(true);
			saveMatrix(A);
		}
		endTimer();
	}
	
	public void MatrixChol(int size, int times)
	{
		if (output)
			System.out.println("MatrixChol");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = createInvertableMatrix(size);
			A.inverseByCholesky();
			saveMatrix(A);
		}
		endTimer();
	}
	

	public void MatrixNew(int size, int times)
	{
		if (output)
			System.out.println("MatrixNew");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			Matrix A = createInvertableMatrix(size);
			A.inverse(false);
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
			Jama.Matrix A = createInvertableMatrixJaMa(size);
			A.inverse();
			saveJamaMatrix(A);
		}
		endTimer();
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	
	public Jama.Matrix createInvertableMatrixJaMa(int size)
	{
		breakTimer();
		double[][] array = createInvertableDouble(size);
		Jama.Matrix A = new Jama.Matrix(array);
		
		restartTimer();
		return A;
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	public Matrix createInvertableMatrix(int size)
	{
		long fctStart = System.currentTimeMillis();
		double[][] array = createInvertableDouble(size);
		Matrix A = new Matrix(array);
		
		long fctStop = System.currentTimeMillis();
		long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	public edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix createInvertableCheckMatrix(int size)
	{
		long fctStart = System.currentTimeMillis();
		double[][] array = createInvertableDouble(size);
		edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix A = new edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix(
				array);
		
		long fctStop = System.currentTimeMillis();
		long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	

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
		return  new double[][] { { -3, -1, 3, 0, -1, -6 }, { 3, 0, -5, 0, 0, -1 }, { 0, 0, 0, 3, 0, 0 },
				{ 0, 0, 0, 0, 0, 2 }, { 0, 1, 0, 0, 0, -1 }, { -2, -3, 0, 0, -5, 0 } };
	}
	

	private double[][] createInvertableDouble2x2()
	{
		double[][] arr = new double[2][2];
		
		int val = (int) (Math.random() * 4);
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
			case 1:
				arr[0][0] = 1;
				arr[0][1] = -1;
				arr[1][0] = -4;
				arr[1][1] = -1;
				count1++;
				break;
			case 2:
				arr[0][0] = 3;
				arr[0][1] = -5;
				arr[1][0] = -5;
				arr[1][1] = -2;
				count2++;
				break;
			case 3:
				arr[0][0] = 0;
				arr[0][1] = 1;
				arr[1][0] = 5;
				arr[1][1] = 2;
				count3++;
				break;
			default:
				System.out.println("Something is going wrong");
				break;
			
		}
		return arr;
	}
}
