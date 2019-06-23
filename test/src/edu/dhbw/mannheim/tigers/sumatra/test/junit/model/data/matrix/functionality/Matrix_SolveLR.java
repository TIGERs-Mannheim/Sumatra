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

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;


/**
 * Class to test LR: A*X = B
 * @author Birgit
 * 
 */
public class Matrix_SolveLR extends APerformanceTest
{

	@Test
	public void solve_LR_success_Jama_OneLine()
	{
		Jama.Matrix A = new Jama.Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		Jama.Matrix B = new Jama.Matrix(new double[] { 3, 6, 4, 7 }, 4);
		Jama.Matrix Sol = new Jama.Matrix(new double[] { 0, 1, -1, 2 }, 4);
		
		// get some value
		double bev = A.get(2, 2);
		
		// calculate solution-vector
		Jama.Matrix X = A.chol().solve(B);
		
		// get some other value
		double aft = A.get(2, 2);
		
		// compare
		assertTrue(equals(Sol, X));
		assertEquals(aft, bev, 1E-15);
	}
	
	@Test
	public void solve_LR_success_Jama()
	{
		Jama.Matrix A = new Jama.Matrix(new double[][] { {2,-1,-3},{-1,2,4},{-3,4,9}});
		Jama.Matrix Sol = new Jama.Matrix(new double[][] {{8,9},{-3,5},{2,1} });
		Jama.Matrix B = new Jama.Matrix(new double[][] { {13,10},{-6,5},{-18,2} });
		// get some value
		double bev = A.get(2, 2);
		
		// calculate solution-vector
		Jama.Matrix X = A.chol().solve(B);
		// get some other value
		double aft = A.get(2, 2);
		
		// compare
		assertTrue(equals(Sol, X));
		assertEquals(aft, bev, 1E-15);
	}
	

	@Test
	public void solve_LR_success_OneLine()
	{
		Matrix A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		Matrix Copy = A.copy();
		Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		Matrix Sol = new Matrix(new double[] { 0, 1, -1, 2 }, 4, 1, true);

		// calculate solution-vector
		Matrix X = A.solve_LR(B);

		// compare
		assertTrue(equals(X,Sol));
		assertTrue(equals(A,Copy));
	}
	

	@Test
	public void solve_LR_zeroInside()
	{
		Matrix A = new Matrix(new double[][] { { 0, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		
		// calculate solution-vector
		A.solve_LR(B);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void solve_LR_wrongMatrix()
	{
		// create matrix
		Matrix A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, });
		// create vector
		Matrix B = new Matrix(new double[] { 3, 6, 4, 7 }, 4, 1, true);
		
		// calculate solution-vector
		A.solve_LR(B);
	}
	

	@Test
	public void solve_LR_Matrix()
	{
		Matrix A = new Matrix(new double[][] { {2,-1,-3},{-1,2,4},{-3,4,9}});
		Matrix Copy = A.copy();
		Matrix Sol = new Matrix(new double[][] {{8,9},{-3,5},{2,1} });
		Matrix B = new Matrix(new double[][] { {13,10},{-6,5},{-18,2} });
		
		// calculate solution-vector
		Matrix X = A.solve_LR(B);
	
		assertTrue(equals(X,Sol));
		assertTrue(equals(A,Copy));
	}
	

	@Test
	public void compare_Jama_Matrix()
	{
		Matrix M_A = new Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		Matrix M_B = new Matrix(new double[] { 3, 6, 4, 7 },4,1,true);
		Matrix M_Sol = new Matrix(new double[] { 0, 1, -1, 2 }, 4,1,true);
		
		Jama.Matrix J_A = new Jama.Matrix(new double[][] { { 4, 3, 2, 1 }, { 3, 6, 4, 2 }, { 2, 4, 6, 3 }, { 1, 2, 3, 4 } });
		Jama.Matrix  J_B = new Jama.Matrix(new double[] { 3, 6, 4, 7 }, 4);
		Jama.Matrix  J_Sol = new Jama.Matrix(new double[] { 0, 1, -1, 2 }, 4);
		

		// calculate solution-vector
		Jama.Matrix J_X = J_A.chol().solve(J_B);
		Matrix M_X = M_A.solve_LR(M_B);
		
		assertTrue(equals(M_X, M_Sol));
		assertTrue(equals(J_X, J_Sol));
		assertTrue(equals(J_X, M_X));
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void solve_LR_Matrix_wrongInput()
	{
		// create one matrix
		Matrix A = new Matrix(new double[][] { { 7.0, 6.0 }, { 5.0, 4.0 }, { 3.0, 2.0 } });
		
		// create third correct matrices
		Matrix B = new Matrix(new double[][] { { 67.0, 54.0, 41.0, 28.0 }, { 47.0, 38.0, 29.0, 20.0 } });
		
		// calculate solution-vector
		A.solve_LR(B);
		
	}	

	@Test
	public void solve_LR_success()
	{
		// create matrix
		Matrix A = new Matrix(new double[][] { { 2, -1, -3, 3 }, { 4, 0, -3, 1 }, { 6, 1, -1, 6 },
				{ -2, -5, 4, 1 } });
		Matrix Copy = A.copy();
		// create vector
		Matrix B = new Matrix(new double[] { 1, -8, -16, -12 },4,1,true);
				// set solution-vector
		Matrix Sol = new Matrix(new double[] { -4.5, 2, -3, 1 },4,1,true);
		
		// calculate solution-vector
		Matrix X = A.solve_LR(B);
		
		// compare
		assertTrue(equals(A,Copy));
		assertTrue(equals(X,Sol));
	}
}
