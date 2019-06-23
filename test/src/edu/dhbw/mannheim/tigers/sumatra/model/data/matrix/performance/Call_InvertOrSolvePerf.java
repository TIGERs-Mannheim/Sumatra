/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;


/**
 * Class to find out, which is the fastest way:
 * A *X = B or A = B * X^-1
 * and control, that jama and matrix bring the same results
 * @author Birgit
 * 
 */
public class Call_InvertOrSolvePerf extends APerformanceTest
{
	final int	size	= 2;
	final int	times	= 10;
	
	Matrix		MChol;
	Matrix		MInv;
	Matrix		MLR;
	Jama.Matrix	JChol;
	Jama.Matrix	JInv;
	Jama.Matrix	JLR;
	
	double[][]	a3		= new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } };
	double[][]	x3		= new double[][] { { 8, 9, 0 }, { -3, 5, 0 }, { 2, 1, 1 } };
	double[][]	b3		= new double[][] { { 13, 10, -3 }, { -6, 5, 4 }, { -18, 2, 9 } };
	
	
	/**
	 */
	@Ignore
	@Test
	public void vl()
	{
		final Matrix A = new Matrix(a3);
		final Matrix B = new Matrix(b3);
		final Matrix X = new Matrix(x3);
		
		Matrix Sol = A.times(X);
		assertTrue(equals(Sol, B));
		
		Sol = A.solveCholesky(B);
		assertTrue(equals(Sol, X));
		
		Sol = A.inverse().times(B);
		assertTrue(equals(Sol, X));
		
		Sol = B.times(A.inverse());
		assertTrue(equals(Sol, X));
		
		
	}
	
	
	/**
	 */
	@BeforeClass
	public static void beforeClass()
	{
		System.out.println("##########################################################");
		System.out.println("Call_InvertOrSolve");
		System.out.println("##########################################################");
		System.out.println("--> Many tests deactivated");
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
	
	
	/**
	 */
	@Test
	public void test()
	{
		output = false;
		JaMaInv(size, times);
		MatrixInv(size, times);
		JaMaChol(size, times);
		MatrixChol(size, times);
		JaMaLR(size, times);
		MatrixLR(size, times);
	}
	
	
	/**
	 */
	@Ignore
	@Test
	public void testMany()
	{
		output = true;
		final int times = timesEasyOps / 5;
		System.out.println("Number of runs: " + times);
		JaMaInv(size, times);
		MatrixInv(size, times);
		JaMaChol(size, times);
		MatrixChol(size, times);
		JaMaLR(size, times);
		MatrixLR(size, times);
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
		final Matrix A = getMA(size);
		final Matrix B = getMB(size);
		Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.solveCholesky(B);
		}
		endTimer();
		MChol = X;
		assertTrue(equals(MChol, getMX(size)));
		
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void JaMaChol(int size, int times)
	{
		if (output)
		{
			System.out.println("JaMaChol");
		}
		final Jama.Matrix A = getJA(size);
		final Jama.Matrix B = getJB(size);
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
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void MatrixLR(int size, int times)
	{
		if (output)
		{
			System.out.println("MatrixLR");
		}
		final Matrix A = getMA(size);
		final Matrix B = getMB(size);
		Matrix X = null;
		startTimer();
		for (int i = 0; i < times; i++)
		{
			X = A.solveLR(B);
			
		}
		endTimer();
		MLR = X;
		assertTrue(equals(MLR, getMX(size)));
		
	}
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void JaMaLR(int size, int times)
	{
		if (output)
		{
			System.out.println("JaMaLR");
		}
		final Jama.Matrix A = getJA(size);
		final Jama.Matrix B = getJB(size);
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
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void MatrixInv(int size, int times)
	{
		
		if (output)
		{
			System.out.println("MatrixInv");
		}
		final Matrix B = getMB(size);
		final Matrix A = getMA(size);
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
	
	
	/**
	 * @param size
	 * @param times
	 */
	public void JaMaInv(int size, int times)
	{
		if (output)
		{
			System.out.println("JamaInv");
		}
		final Jama.Matrix B = getJB(size);
		final Jama.Matrix A = getJA(size);
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
	
	
	/**
	 * @param size
	 * @return
	 */
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