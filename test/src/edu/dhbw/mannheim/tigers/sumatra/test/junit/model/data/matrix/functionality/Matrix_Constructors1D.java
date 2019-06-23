/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.functionality;

import static org.junit.Assert.*;


import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * This class tests the constructors o matrix-class
 * @author Birgit
 * 
 */
public class Matrix_Constructors1D
{
	double[]	r4_4_a	= new double[] { -2, 1, 3, 2, 1, -3, -1, 4, -1, 0, 2, 4, 2, -2, -2, -4 };
	
	double[]	r2_4_a	= new double[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	
	
	/******************************************************************************
	 * testCheckMatrixIntIntDouble
	 ******************************************************************************/
	@Test
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean()
	{
		// correct
		// wrong row
		// wrong col
		// wrong value
	}
	

	@Test
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean_correct()
	{
		// create a 2x4 matrix and control the content
		Matrix M = new Matrix(r2_4_a, 2,4, true);
		
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(r2_4_a[i*4+j], M.get(i, j),1E-15);
			}
		}
		
		// create a 2x4 matrix and control the content
		M = new Matrix(r2_4_a, 2,4, false);
		
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				assertEquals(r2_4_a[i*4+j], M.get(i, j), 0.000001);
			}
		}
		
		//change the matrix and control
		M.set(0,0,123);
		assertEquals(M.get(0, 0), 123, 1E-15);
		assertFalse(M.get(0,0) ==  r2_4_a[0]);
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean_wrongRow()
	{
		new Matrix(r2_4_a, 1,4, true);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCheckMatrixDoubleArrayArrayIntIntBoolean_wrongCol()
	{
		new Matrix(r2_4_a, 2,6, true);
	}

}
