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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;


/**
 * Class to find out, which is the fastest way:
 * A *X = B or A = B * X^-1
 * and control, that jama and matrix bring the same results
 * @author Birgit
 * 
 */
public class Call_InvertOrSolve extends APerformanceTest
{
	/**
	 */
	@Test
	public void test()
	{
		final Matrix M_A = new Matrix(new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } });
		final Matrix M_Copy = M_A.copy();
		final Matrix M_X = new Matrix(new double[][] { { 8, 9, 0 }, { -3, 5, 0 }, { 2, 1, 1 } });
		final Matrix M_B = new Matrix(new double[][] { { 13, 10, -3 }, { -6, 5, 4 }, { -18, 2, 9 } });
		
		// calculate solution-vector
		final Matrix M_X_Calc = M_A.solveCholesky(M_B);
		final Matrix M_A_Calc = M_B.times(M_X_Calc.inverse());
		
		
		assertTrue(equals(M_X_Calc, M_X));
		assertTrue(equals(M_A_Calc, M_A));
		assertTrue(equals(M_A, M_Copy));
		
		
		final Jama.Matrix J_A = new Jama.Matrix(new double[][] { { 2, -1, -3 }, { -1, 2, 4 }, { -3, 4, 9 } });
		final Jama.Matrix J_Copy = J_A.copy();
		final Jama.Matrix J_X = new Jama.Matrix(new double[][] { { 8, 9, 0 }, { -3, 5, 0 }, { 2, 1, 1 } });
		final Jama.Matrix J_B = new Jama.Matrix(new double[][] { { 13, 10, -3 }, { -6, 5, 4 }, { -18, 2, 9 } });
		
		// calculate solution-vector
		final Jama.Matrix J_X_Calc = J_A.chol().solve(J_B);
		final Jama.Matrix J_A_Calc = J_B.times(J_X_Calc.inverse());
		
		
		assertTrue(equals(J_X_Calc, J_X));
		assertTrue(equals(J_A_Calc, J_A));
		assertTrue(equals(J_A, J_Copy));
		
		
		assertTrue(equals(M_A_Calc, J_A_Calc));
		assertTrue(equals(M_X_Calc, J_X_Calc));
	}
	
	
}
