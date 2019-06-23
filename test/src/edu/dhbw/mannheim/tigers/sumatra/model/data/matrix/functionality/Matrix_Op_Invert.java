/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.functionality;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance.APerformanceTest;


/**
 * Class to test the invert-function
 * @author Birgit
 * 
 */
public class Matrix_Op_Invert extends APerformanceTest
{
	/**
	 */
	@Test
	public void testDet()
	{
		// fail("Not yet implemented");
	}
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Test
	public void test2x2()
	{
		final double[] d2_2 = new double[] { -10, -4, -2, 0 };
		
		final Matrix A = new Matrix(d2_2, 2, 2, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { 0.0, -0.5, -0.25, 1.25 }, 2, 2, true);
		
		final Matrix X = A.inverse();
		
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, B));
	}
	
	
	/**
	 */
	@Test
	public void test3x3()
	{
		final double[] d3_3 = new double[] { 0.0, 0.0, 10.0, 10.0, -1.0, 0.0, -2.0, 1.0, 3.0 };
		final Matrix A = new Matrix(d3_3, 3, 3, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { -0.0375, 0.125, 0.125, -0.375, 0.25, 1.25, 0.1, 0.0, 0.0 }, 3, 3,
				true);
		
		final Matrix X = A.inverse();
		
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, B));
	}
	
	
	/**
	 */
	@Test
	public void test4x4()
	{
		final double[] d4_4 = new double[] { -2, 1, 3, 2, 1, -3, -1, 4, -1, 0, 2, 4, 2, -2, -2, -4 };
		final Matrix A = new Matrix(d4_4, 4, 4, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { -1.5, -1.0, 2.5, 0.75, -0.75, -0.5, 0.75, -0.125, -0.25, -0.5, 1.25,
				0.625, -0.25, 0.0, 0.25, -0.125 }, 4, 4, true);
		
		final Matrix X = A.inverse();
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, B));
	}
	
	
	/**
	 */
	@Test
	public void test6x6()
	{
		
		final double[] d6_6 = new double[] { -3, -1, 3, 0, -1, -6, 3, 0, -5, 0, 0, -1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0,
				2, 0, 1, 0, 0, 0, -1, -2, -3, 0, 0, -5, 0 };
		final double[] s6_6 = new double[] { -1.25, -0.75, 0, -4.375, -0.5, 0.25, 0, 0, 0, 0.5, 1, 0, -0.75, -0.65, 0,
				-2.725, -0.3, 0.15, 0, 0, 0.3333333333333333333333333333, 0, 0, 0, 0.5, 0.3, 0, 1.45, -0.4, -0.3, 0, 0, 0,
				0.5, 0, 0 };
		final Matrix A = new Matrix(d6_6, 6, 6, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(s6_6, 6, 6, true);
		
		final Matrix X = A.inverse();
		// System.out.println(Sol);
		// System.out.println(X);
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, B));
		
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test_wrongDet()
	{
		final double[] d4_4 = new double[] { 0, 0, 0, 0, 1, -3, -1, 4, -1, 0, 2, 4, 2, -2, -2, -4 };
		final Matrix A = new Matrix(d4_4, 4, 4, true);
		A.inverse();
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test_wrongArray()
	{
		final double[] d4_4 = new double[] { 4, 1, 2, 1, -3, -1, 4, -1, 0, 2, 4, 2, -2, -2, -4 };
		final Matrix A = new Matrix(d4_4, 4, 4, true);
		A.inverse();
	}
	
	
	/**
	 */
	@Test
	public void testChangeCurr2x2()
	{
		final double[] d2_2 = new double[] { -10, -4, -2, 0 };
		
		final Matrix A = new Matrix(d2_2, 2, 2, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { 0.0, -0.5, -0.25, 1.25 }, 2, 2, true);
		
		final Matrix X = A.inverse(true);
		
		assertTrue(equals(X, Sol));
		assertFalse(equals(A, B));
		assertTrue(equals(X, A));
	}
	
	
	/**
	 */
	@Test
	public void testChangeCurr3x3()
	{
		final double[] d3_3 = new double[] { 0.0, 0.0, 10.0, 10.0, -1.0, 0.0, -2.0, 1.0, 3.0 };
		final Matrix A = new Matrix(d3_3, 3, 3, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { -0.0375, 0.125, 0.125, -0.375, 0.25, 1.25, 0.1, 0.0, 0.0 }, 3, 3,
				true);
		
		final Matrix X = A.inverse(true);
		
		assertTrue(equals(X, Sol));
		assertFalse(equals(A, B));
		assertTrue(equals(X, A));
	}
	
	
	/**
	 */
	@Test
	public void testChangeCurr4x4()
	{
		final double[] d4_4 = new double[] { -2, 1, 3, 2, 1, -3, -1, 4, -1, 0, 2, 4, 2, -2, -2, -4 };
		final Matrix A = new Matrix(d4_4, 4, 4, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(new double[] { -1.5, -1.0, 2.5, 0.75, -0.75, -0.5, 0.75, -0.125, -0.25, -0.5, 1.25,
				0.625, -0.25, 0.0, 0.25, -0.125 }, 4, 4, true);
		
		
		final Matrix X = A.inverse(true);
		
		assertTrue(equals(X, Sol));
		assertFalse(equals(A, B));
		assertTrue(equals(X, A));
	}
	
	
	/**
	 */
	@Test
	public void testChangeCurr6x6()
	{
		final double[] d6_6 = new double[] { -3, -1, 3, 0, -1, -6, 3, 0, -5, 0, 0, -1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0,
				2, 0, 1, 0, 0, 0, -1, -2, -3, 0, 0, -5, 0 };
		final double[] s6_6 = new double[] { -1.25, -0.75, 0, -4.375, -0.5, 0.25, 0, 0, 0, 0.5, 1, 0, -0.75, -0.65, 0,
				-2.725, -0.3, 0.15, 0, 0, 0.3333333333333333333333333333, 0, 0, 0, 0.5, 0.3, 0, 1.45, -0.4, -0.3, 0, 0, 0,
				0.5, 0, 0 };
		final Matrix A = new Matrix(d6_6, 6, 6, true);
		final Matrix B = A.copy();
		final Matrix Sol = new Matrix(s6_6, 6, 6, true);
		
		final Matrix X = A.inverse(true);
		
		assertTrue(equals(X, Sol));
		assertFalse(equals(A, B));
		assertTrue(equals(X, A));
	}
	
	
	/**
	 */
	@Test
	public void testInverseCholesky()
	{
		final double[][] d4_4 = new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } };
		final double[][] sol = new double[][] { { 2, -3, 2 }, { -3, 9, -5 }, { 2, -5, 3 } };
		final Matrix A = new Matrix(d4_4);
		final Matrix Copy = A.copy();
		final Matrix Sol = new Matrix(sol);
		
		final Matrix X = A.inverseByCholesky();
		assertTrue(equals(A, Copy));
		assertTrue(equals(X, Sol));
	}
}
