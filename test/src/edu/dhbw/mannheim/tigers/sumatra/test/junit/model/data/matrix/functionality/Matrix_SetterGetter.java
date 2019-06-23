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
 * This class tests the setter and getter o the matrix-class
 * @author Birgit
 * 
 */
public class Matrix_SetterGetter
{
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#getRowDimension()}.
	 */
	@Test
	public void testGetRowDimension()
	{
		int row = 2;
		int col = 3;
		Matrix A = new Matrix(row, col);
		assertEquals(A.getRowDimension(), row);
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#getColumnDimension()}.
	 */
	@Test
	public void testGetColumnDimension()
	{
		int row = 2;
		int col = 3;
		Matrix A = new Matrix(row, col);
		assertEquals(A.getColumnDimension(), col);
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#get(int, int)}.
	 */
	@Test
	public void testGet()
	{
		// correct
		// wrongIndexRow
		// wrongIndexColumn
	}
	

	@Test
	public void testGet_correct()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				assertEquals(array[i][j],Correct.get(i, j),1E-15 );
			}
		}
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testGet_wrongIndexRow()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		Correct.get(-25, 1);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testGet_wrongIndexCol()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		Correct.get(1, 10);
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix#set(int, int, double)}.
	 */
	@Test
	public void testSet()
	{
		// correct
		// wrongIndexRow
		// wrongIndexColumn
		// wrongValue
	}
	

	@Test
	public void testSet_correct()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		double val = 3.0;
		int row = 0;
		int col = 1;
		Correct.set(row, col, val);
		assertEquals(val,Correct.get(row, col),1E-15 );
		
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testSet_wrongIndexRow()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		double val = 3.0;
		int row = 100;
		int col = 1;
		Correct.set(row, col, val);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testSet_wrongIndexCol()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		double val = 3.0;
		int row = 1;
		int col = -25;
		Correct.set(row, col, val);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testSet_wrongValue()
	{
		double[][] array = new double[][] { { 1.0,  2.0,  3.0 }, { 4.0,  5.0,  -6.0 } };
		Matrix Correct = new Matrix(array);
		
		double val = 3.0E+60;
		int row = 1;
		int col = 100;
		Correct.set(row, col, (double) val);
	}
}
