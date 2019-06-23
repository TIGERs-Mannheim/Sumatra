/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2010
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
 * This class tests the operations, which are used in matrix-class
 * @author Birgit
 * 
 */
public class Matrix_Operations extends APerformanceTest
{
	double[][]	r2_2_a	= new double[][] { { -10, -4 }, { -2, 0 } };
	// 0 -0,5
	// -0,25 1,25
	
	
	double[][]	r3_3_a	= new double[][] { { 0.0, 0.0, 10.0 }, { 10.0, -1.0, 0.0 }, { -2.0, 1.0, 3.0 } };
	
	// -0,0375 0,125 0,125
	// -0,375 0,25 1,25
	// 0,1 0 0
	
	
	double[][]	r4_4_a	= new double[][] { { -2, 1, 3, 2 }, { 1, -3, -1, 4 }, { -1, 0, 2, 4 }, { 2, -2, -2, -4 } };
	
	
	// -1,5 -1.0 2,5 0,75
	// -0,75 -0,5 0,75 -0,125
	// -0,25 -0,5 1,25 0,625
	// -0,25 0 0,25 -0,125
	
	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix#plus(edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix)}
	 * .
	 */
	@Test
	public void testPlus()
	{
		// create one matrix
		Matrix A = new Matrix(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } });
		final Matrix T = A.copy();
		
		// create second matrix
		final Matrix B = new Matrix(new double[][] { { 2.0, 4.0 }, { 6.0, 8.0 }, { 10.0, -20.0 } });
		
		// create third correct matrices
		final Matrix D = new Matrix(new double[][] { { 3.0, 6.0 }, { 9.0, 12.0 }, { 15.0, -14.0 } });
		
		
		// new Matrix:---------------------
		Matrix C = A.plus(B);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.plus(B, false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.plus(B, true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
		
		
	}
	
	
	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix#minus(edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix)}
	 * .
	 */
	@Test
	public void testMinus()
	{
		// create one matrix
		Matrix A = new Matrix(new double[][] { { 20.0, 21.0 }, { 22.0, -4.0 }, { -3.0, -2.0 } });
		final Matrix T = A.copy();
		
		// create second matrix
		final Matrix B = new Matrix(new double[][] { { 6.0, 5.0 }, { 4.0, 3.0 }, { 2.0, 1.0 } });
		
		// create third correct matrices
		final Matrix D = new Matrix(new double[][] { { 14.0, 16.0 }, { 18.0, -7.0 }, { -5.0, -3.0 } });
		
		
		// new Matrix:---------------------
		Matrix C = A.minus(B);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.minus(B, false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.minus(B, true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
	}
	
	
	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix#transpose()}.
	 */
	@Test
	public void testTranspose()
	{
		// create origin matrix
		Matrix A = new Matrix(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } });
		final Matrix T = A.copy();
		
		// create correct matrix
		final Matrix D = new Matrix(new double[][] { { 1.0, 3.0, 5.0 }, { 2.0, 4.0, 6.0 } });
		
		// new Matrix:---------------------
		Matrix C = A.transpose();
		
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.transpose();
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
	}
	
	
	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix#times(edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix)}
	 * .
	 */
	@Test
	public void testTimes()
	{
		// create one matrix
		Matrix A = new Matrix(new double[][] { { 7.0, 6.0 }, { 5.0, 4.0 }, { 3.0, 2.0 } });
		final Matrix T = A.copy();
		// create second matrix
		final Matrix B = new Matrix(new double[][] { { 7.0, 6.0, 5.0, 4.0 }, { 3.0, 2.0, 1.0, 0.0 } });
		
		// create third correct matrices
		final Matrix D = new Matrix(new double[][] { { 67.0, 54.0, 41.0, 28.0 }, { 47.0, 38.0, 29.0, 20.0 },
				{ 27.0, 22.0, 17.0, 12.0 } });
		
		// new Matrix:---------------------
		Matrix C = A.times(B);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.times(B);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
	}
	
	
	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix#inverse()}.
	 */
	@Test
	public void testInverse()
	{
		// correct
		// wrong 2
	}
	
	
	/**
	 */
	@Test
	public void testInverse_correct()
	{
		
		// test 2x2
		Matrix A = new Matrix(r2_2_a);
		Matrix T = A.copy();
		Matrix D = new Matrix(new double[][] { { 0.0, -0.5 }, { -0.25, 1.25 } });
		
		// new Matrix:---------------------
		Matrix C = A.inverse();
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.inverse(false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.inverse(true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
		
		// test 3x3
		A = new Matrix(r3_3_a);
		T = A.copy();
		D = new Matrix(new double[][] { { -0.0375, 0.125, 0.125 }, { -0.375, 0.25, 1.25 }, { 0.1, 0.0, 0.0 } });
		
		// new Matrix:---------------------
		C = A.inverse();
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.inverse(false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.inverse(true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
		
		
		// test 4x4
		A = new Matrix(r4_4_a);
		T = A.copy();
		D = new Matrix(new double[][] { { -1.5, -1.0, 2.5, 0.75 }, { -0.75, -0.5, 0.75, -0.125 },
				{ -0.25, -0.5, 1.25, 0.625 }, { -0.25, 0.0, 0.25, -0.125 } });
		
		// new Matrix:---------------------
		C = A.inverse();
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.inverse(false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.inverse(true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
		
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInverse_wrong2()
	{
		// test 2x2
		final double[][] r = new double[][] { { 1.0, 1.0 }, { 1.0, 1.0 } };
		final Matrix A = new Matrix(r);
		A.inverse();
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInverse_wrong3()
	{
		// test
		final double[][] r = new double[][] { { 1.0, 1.0, 1.0 }, { 1.0, 1.0, 1.0 }, { 1.0, 1.0, 1.0 } };
		final Matrix A = new Matrix(r);
		A.inverse();
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInverse_wrong4()
	{
		// test
		final double[][] r = new double[][] { { 1.0, 1.0, 1.0, 1.0 }, { 1.0, 1.0, 1.0, 1.0 }, { 1.0, 1.0, 1.0, 1.0 },
				{ 1.0, 1.0, 1.0, 1.0 } };
		final Matrix A = new Matrix(r);
		A.inverse();
	}
	
	
	/**
	 */
	@Test
	public void testIdentity()
	{
		Matrix A = new Matrix(r4_4_a);
		final Matrix T = A.copy();
		final Matrix D = new Matrix(new double[][] { { 1.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0 },
				{ 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0 } });
		
		// new Matrix:---------------------
		Matrix C = A.identity();
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		// new Matrix:---------------------
		C = A.identity(false);
		// test correctness
		assertTrue(equals(C, D));
		assertTrue(equals(A, T));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertTrue(equals(A, T));
		// reset a
		A = T.copy();
		
		
		// same matrix:------------------
		C = A.identity(true);
		// test correctness
		assertTrue(equals(C, D));
		assertFalse(equals(A, T));
		assertTrue(equals(A, D));
		// change result
		C.set(0, 0, -123);
		// test influential on start
		assertFalse(equals(A, T));
		assertFalse(equals(A, D));
		// reset a
		A = T.copy();
	}
	
}
