/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * Class to find out, which is the fastest way:
 * A *X = B or A = B * X^-1
 * and control, that jama and matrix bring the same results
 * @author Birgit
 * 
 */
public class Call_InvertOrSolve extends APerformanceTest
{
	
	Matrix		MChol;
	Matrix		MInv;
	Matrix		MLR;
	Jama.Matrix	JChol;
	Jama.Matrix	JInv;
	Jama.Matrix	JLR;
	
	double[][]	a3	= new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } };
	double[][]	x3	= new double[][] { { 8, 9, 0 }, { -3, 5, 0 }, { 2, 1, 1 } };
	double[][]	b3	= new double[][] { { 13, 10, -3 }, { -6, 5, 4 }, { -18, 2, 9 } };
	
	
	// @Test
	public void vl()
	{
		Matrix A = new Matrix(a3);
		Matrix B = new Matrix(b3);
		Matrix X = new Matrix(x3);
		
		Matrix Sol = A.times(X);
		assertTrue(equals(Sol, B));
		
		Sol = A.solve_Cholesky(B);
		assertTrue(equals(Sol, X));
		
		Sol = A.inverse().times(B);
		assertTrue(equals(Sol, X));
		
		Sol = B.times(A.inverse());
		assertTrue(equals(Sol, X));
		

	}
	

	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("Call_InvertOrSolve");
		System.out.println("##########################################################");
		
		output = false;
		int size = 2;
		int times = 10;
		JaMaInv(size, times);
		MatrixInv(size, times);
		JaMaChol(size, times);
		MatrixChol(size, times);
		JaMaLR(size, times);
		MatrixLR(size, times);
		

		output = true;
		times = timesEasyOps / 5;
		System.out.println("Number of runs: " + times);
		JaMaInv(size, times);
		MatrixInv(size, times);
		JaMaChol(size, times);
		MatrixChol(size, times);
		JaMaLR(size, times);
		MatrixLR(size, times);
		
		// TODO Birgt: add 6x6
		/*
		 * output = false;
		 * size = 6;
		 * times = 10;
		 * JaMaInv(size, times);
		 * MatrixInv(size, times);
		 * vgl(size);
		 * JaMaChol(size, times);
		 * MatrixChol(size, times);
		 * vgl(size);
		 * 
		 * 
		 * output = true;
		 * times = timesEasyOps;
		 * System.out.println("Number of runs: " + times);
		 * JaMaInv(size, times);
		 * MatrixInv(size, times);
		 * vgl(size);
		 * JaMaChol(size, times);
		 * MatrixChol(size, times);
		 * vgl(size);
		 */


	}
	

	public void MatrixChol(int size, int times)
	{
		if (output)
			System.out.println("MatrixChol");
		Matrix A = getMA(size);
		Matrix B = getMB(size);
		Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.solve_Cholesky(B);
		}
		endTimer();
		MChol = X;
		assertTrue(equals(MChol, getMX(size)));
		
	}
	

	public void JaMaChol(int size, int times)
	{
		if (output)
			System.out.println("JaMaChol");
		Jama.Matrix A = getJA(size);
		Jama.Matrix B = getJB(size);
		Jama.Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.chol().solve(B);
			
		}
		endTimer();
		JChol = X;
		assertTrue(equals(JChol, getJX(size)));
	}
	

	public void MatrixLR(int size, int times)
	{
		if (output)
			System.out.println("MatrixLR");
		Matrix A = getMA(size);
		Matrix B = getMB(size);
		Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.solve_LR(B);
			
		}
		endTimer();
		MLR = X;
		assertTrue(equals(MLR, getMX(size)));
		
	}
	

	public void JaMaLR(int size, int times)
	{
		if (output)
			System.out.println("JaMaLR");
		Jama.Matrix A = getJA(size);
		Jama.Matrix B = getJB(size);
		Jama.Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.lu().solve(B);
			
		}
		endTimer();
		JLR = X;
		assertTrue(equals(JLR, getJX(size)));
	}
	

	public void MatrixInv(int size, int times)
	{
		
		if (output)
			System.out.println("MatrixInv");
		Matrix B = getMB(size);
		Matrix A = getMA(size);
		Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.inverse().times(B);
			
		}
		endTimer();
		MInv = X;
		assertTrue(equals(MInv, getMX(size)));
		
	}
	

	public void JaMaInv(int size, int times)
	{
		if (output)
			System.out.println("JamaInv");
		Jama.Matrix B = getJB(size);
		Jama.Matrix A = getJA(size);
		Jama.Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.inverse().times(B);
			
		}
		endTimer();
		JInv = X;
		assertTrue(equals(JInv, getJX(size)));
	}
	

	private Matrix getMA(int size)
	{
		breakTimer();
		Matrix Mat = null;
		if (size == 2)
		{
			Mat = new Matrix(a3);
		} else if (size == 6)
		{
			
		}
		restartTimer();
		return Mat;
	}
	

	private Matrix getMB(int size)
	{
		breakTimer();
		Matrix Mat = null;
		if (size == 2)
		{
			Mat = new Matrix(b3);
		} else if (size == 6)
		{
			
		}
		restartTimer();
		return Mat;
	}
	

	private Matrix getMX(int size)
	{
		breakTimer();
		Matrix Mat = null;
		if (size == 2)
		{
			Mat = new Matrix(x3);
		} else if (size == 6)
		{
			
		}
		restartTimer();
		return Mat;
	}
	

	private Jama.Matrix getJA(int size)
	{
		breakTimer();
		Jama.Matrix Mat = null;
		if (size == 2)
		{
			Mat = new Jama.Matrix(a3);
		} else if (size == 6)
		{
			
		}
		restartTimer();
		return Mat;
	}
	

	private Jama.Matrix getJB(int size)
	{
		breakTimer();
		Jama.Matrix Mat = null;
		if (size == 2)
		{
			Mat = new Jama.Matrix(b3);
		} else if (size == 6)
		{
			
		}
		restartTimer();
		return Mat;
	}
	

	private Jama.Matrix getJX(int size)
	{
		breakTimer();
		Jama.Matrix Mat = null;
		;
		if (size == 2)
		{
			Mat = new Jama.Matrix(x3);
		} else if (size == 6)
		{
		}
		restartTimer();
		return Mat;
	}
}