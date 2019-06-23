/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 5, 2010
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
 * Test the constructor-calls in 0D
 * @author Birgit
 * 
 */
public class Matrix_Constructors0D extends APerformanceTest
{
	/******************************************************************************
	 * testCheckMatrixIntIntDouble
	 ******************************************************************************/
	@Test
	public void testCheckMatrixIntIntDouble()
	{
		// correct
		// wrong row
		// wrong col
		// wrong value
	}
	

	@Test
	public void testCheckMatrixIntIntDouble_correct()
	{
		// create a 2x4 matrix and control the content
		Matrix m = new Matrix(2, 4, 3.4567);
		
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(3.4567, m.get(i, j), 0.000001);
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixIntIntDouble_wrongRow()
	{
		// create a wrong 0x4 matrix and control the content
		new Matrix(0, 4, 3.4567);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixIntIntDouble_wrongCol()
	{
		// create a wrong 0x4 matrix and control the content
		new Matrix(3, -30, 3.4567);
	}
	

	/******************************************************************************
	 * testCheckMatrixInt
	 ******************************************************************************/
	@Test
	public void testCheckMatrixInt()
	{
		// correct
		// wrong row/col
	}
	

	@Test
	public void testCheckMatrixInt_correct()
	{
		// create a 3x3 matrix and control the content
		Matrix m = new Matrix(3, 3);
		
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				assertEquals(0.0, m.get(i, j), 0.000001);
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixInt_wrongRowCol()
	{
		new Matrix(-20);
	}
	

	/******************************************************************************
	 * testCheckMatrixIntInt
	 ******************************************************************************/
	@Test
	public void testCheckMatrixIntInt()
	{
		// correct
		// wrong row
		// wrong col
	}
	

	@Test
	public void testCheckMatrixIntInt_correct()
	{
		// create a 1x4 matrix and control the content
		Matrix M = new Matrix(1, 4);
		
		for (int i = 0; i < 1; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(0.0, M.get(i, j), 0.000001);
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixIntInt_wrongRow()
	{
		new Matrix(-5, 4);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixIntInt_wrongCol()
	{
		new Matrix(2, 0);
	}
	

	/************************************************************************
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#copy()}.
	 ************************************************************************/
	@Test
	public void testCopy()
	{
		Matrix A = new Matrix(new double[][] { { 1, 2 }, { 3, -4 } });
		Matrix B = A.copy();
		
		// test
		assertTrue(equals(A,B));
	}
	

	/************************************************************************
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#Identity(int, int)}.
	 ************************************************************************/
	@Test
	public void testIdentity()
	{
		// let create identity-matrix
		Matrix A = Matrix.Identity(3, 4);
		
		// create identitiy-matrix
		Matrix B = new Matrix(new double[][] { { 1.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0 },
				{ 0.0, 0.0, 1.0, 0.0 } });

		// test
		assertTrue(equals(A,B));
	}
}
