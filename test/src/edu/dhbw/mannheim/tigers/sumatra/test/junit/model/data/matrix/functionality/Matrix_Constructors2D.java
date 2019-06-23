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
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * Test the constructor calls in 2D
 * @author Birgit
 * 
 */
public class Matrix_Constructors2D
{
	Matrix	m			= null;
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
	
	double[][]	r2_4_a	= new double[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 } };
	
	
	/******************************************************************************
	 * testCheckMatrixDoubleArrayArrayIntIntBoolean
	 ******************************************************************************/
	
	@Test
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean()
	{
		// correct
		// wrong row
		// wrong col
		// wrong array
		// checkContent
	}
	

	@Test
	public void testCheckMatrixDoubleArrayArrayIntInt_correct()
	{
		// create a 2x4 matrix and control the content
		Matrix m = new Matrix(r2_4_a, 2, 4);
		
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(r2_4_a[i][j], m.get(i, j), 0.000001);
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArrayIntInt_wrongRow()
	{
		m = new Matrix(r2_4_a, -5, 4);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArrayIntInt_wrongCol()
	{
		m = new Matrix(r2_4_a, 4, -5);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArrayIntInt_wrongArray()
	{
		double[][] wrong = new double[][] { { 1.0 }, { 2.0, 3.0 } };
		m = new Matrix(wrong, 4, 5);
	}
	

	@Test
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean_checkContent()
	{
		double[][] arr = new double[][] { { 1.0, 3.0 }, { 2.0, 3.0 } };
		
		// array with new space
		m = new Matrix(arr, 2, 2);
		
		// make some changes and look for them
		m.set(0, 0, 333.0);
		
		boolean test = true;
		
		double e = 10E-15;
		double a = m.get(0, 0);// 333.0
		double b = arr[0][0]; // 1.0
		
		if (a + e > b && a - e < b)
		{
			test = true;
		} else
		{
			test = false;
		}
		
		assertFalse(test);
		

		// array with this space
		m = new Matrix(arr, 2, 2);
		// make some changes and look for them
		m.set(0, 0, 333.0);
		assertEquals(arr[0][0], m.get(0, 0), 10E20);
		
	}
	

	/******************************************************************************
	 * DoubleArrayArray
	 ******************************************************************************/
	
	@Test
	public void testCheckMatrixDoubleArrayArrayBoolean()
	{
		// correct
		// wrong array
		// checkContent
	}
	

	@Test
	public void testCheckMatrixDoubleArrayArray_correct()
	{
		// create a 2x4 matrix and control the content
		Matrix m = new Matrix(r2_4_a);
		
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(r2_4_a[i][j], m.get(i, j), 0.000001);
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArray_wrongArray()
	{
		double[][] wrong = new double[][] { { 1.0 }, { 2.0, 3.0 } };
		m = new Matrix(wrong);
	}
	

	@Test
	public void testCheckMatrixDoubleArrayArrayBoolean_checkContent()
	{
		double[][] arr = new double[][] { { 1.0, 3.0 }, { 2.0, 3.0 } };
		
		// array with new space
		m = new Matrix(arr);
		
		// make some changes and look for them
		m.set(0, 0, 333.0);
		
		boolean test = true;
		
		double e = 10E-15;
		double a = m.get(0, 0);// 333.0
		double b = arr[0][0]; // 1.0
		
		if (a + e > b && a - e < b)
		{
			test = true;
		} else
		{
			test = false;
		}
		
		assertFalse(test);
		
		// array with this space
		m = new Matrix(arr);
		// make some changes and look for them
		m.set(0, 0, 333.0);
		assertEquals(arr[0][0], m.get(0, 0), 10E20);
	}
}
