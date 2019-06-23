/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 17, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.functionality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;


/**
 * Class to test LR: A*X = B
 * @author Birgit
 * 
 */
public class Matrix_SolveLR extends APerformanceTest
{
	/**
	 */
	@Test
	public void solve_LR_success_Jama_OneLine()
	{
		final Jama.Matrix A = new Jama.Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 },
				{ 1, 2, 3, 4 } });
		final Jama.Matrix B = new Jama.Matrix(new double[] { 3, 6, 4, 7 }, 4);
		final Jama.Matrix Sol = new Jama.Matrix(new double[] { 0, 1, -1, 2 }, 4);
		
		// get some value
		final double bev = A.get(2, 2);
		
		// calculate solution-vector
		final Jama.Matrix X = A.chol().solve(B);
		
		// get some other value
		final double aft = A.get(2, 2);
		
		// compare
		assertTrue(equals(Sol, X));
		assertEquals(aft, bev, 1E-15);
	}
	
	
	/**
	 */
	@Test
	public void solve_LR_success_Jama()
	{
		final Jama.Matrix A = new Jama.Matrix(new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } });
		final Jama.Matrix Sol = new Jama.Matrix(new double[][] { { 8, 9 }, { -3, 5 }, { 2, 1 } });
		final Jama.Matrix B = new Jama.Matrix(new double[][] { { 13, 10 }, { -6, 5 }, { -18, 2 } });
		// get some value
		final double bev = A.get(2, 2);
		
		// calculate solution-vector
		final Jama.Matrix X = A.chol().solve(B);
		// get some other value
		final double aft = A.get(2, 2);
		
		// compare
		assertTrue(equals(Sol, X));
		assertEquals(aft, bev, 1E-15);
	}
	
	
	/**
	 */
	@Test
	public void solve_LR_success_OneLine()
	{
		final Matrix A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		final Matrix Copy = A.copy();
		final Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		final Matrix Sol = new Matrix(new double[] { 0, 1, -1, 2 }, 4, 1, true);
		
		// calculate solution-vector
		final Matrix X = A.solveLR(B);
		
		// compare
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, Copy));
	}
	
	
	/**
	 */
	@Test
	public void solve_LR_zeroInside()
	{
		final Matrix A = new Matrix(new double[][] { { 0, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		final Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		
		// calculate solution-vector
		A.solveLR(B);
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void solve_LR_wrongMatrix()
	{
		// create matrix
		final Matrix A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, });
		// create vector
		final Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		
		// calculate solution-vector
		A.solveLR(B);
	}
	
	
	/**
	 */
	@Test
	public void solve_LR_Matrix()
	{
		final Matrix A = new Matrix(new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } });
		final Matrix Copy = A.copy();
		final Matrix Sol = new Matrix(new double[][] { { 8, 9 }, { -3, 5 }, { 2, 1 } });
		final Matrix B = new Matrix(new double[][] { { 13, 10 }, { -6, 5 }, { -18, 2 } });
		
		// calculate solution-vector
		final Matrix X = A.solveLR(B);
		
		assertTrue(equals(X, Sol));
		assertTrue(equals(A, Copy));
	}
	
	
	/**
	 */
	@Test
	public void compare_Jama_Matrix()
	{
		final Matrix M_A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		final Matrix M_B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		final Matrix M_Sol = new Matrix(new double[] { 0, 1, -1, 2 }, 4, 1, true);
		
		final Jama.Matrix J_A = new Jama.Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 },
				{ 1, 2, 3, 4 } });
		final Jama.Matrix J_B = new Jama.Matrix(new double[] { 3, 6, 4, 7 }, 4);
		final Jama.Matrix J_Sol = new Jama.Matrix(new double[] { 0, 1, -1, 2 }, 4);
		
		
		// calculate solution-vector
		final Jama.Matrix J_X = J_A.chol().solve(J_B);
		final Matrix M_X = M_A.solveLR(M_B);
		
		assertTrue(equals(M_X, M_Sol));
		assertTrue(equals(J_X, J_Sol));
		assertTrue(equals(J_X, M_X));
	}
	
	
	/**
	 */
	@Test(expected = IllegalArgumentException.class)
	public void solve_LR_Matrix_wrongInput()
	{
		// create one matrix
		final Matrix A = new Matrix(new double[][] { { 7.0, 6.0 }, { 5.0, 4.0 }, { 3.0, 2.0 } });
		
		// create third correct matrices
		final Matrix B = new Matrix(new double[][] { { 67.0, 54.0, 41.0, 28.0 }, { 47.0, 38.0, 29.0, 20.0 } });
		
		// calculate solution-vector
		A.solveLR(B);
		
	}
	
	
	/**
	 */
	@Test
	public void solve_LR_success()
	{
		// create matrix
		final Matrix A = new Matrix(
				new double[][] { { 2, -1, -3, 3 }, { 4, 0, -3, 1 }, { 6, 1, -1, 6 }, { -2, -5, 4, 1 } });
		final Matrix Copy = A.copy();
		// create vector
		final Matrix B = new Matrix(new double[] { 1, -8, -16, -12 }, 4, 1, true);
		// set solution-vector
		final Matrix Sol = new Matrix(new double[] { -4.5, 2, -3, 1 }, 4, 1, true);
		
		// calculate solution-vector
		final Matrix X = A.solveLR(B);
		
		// compare
		assertTrue(equals(A, Copy));
		assertTrue(equals(X, Sol));
	}
}
