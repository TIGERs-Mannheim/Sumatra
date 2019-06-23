/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2010
 * Author(s): Birgit
 * *********************************************************
 */
package edu.tigers.sumatra.wp.flyingBalls;


/**
 * @author BirgitD
 *         This class provides elementary matrix-functions with double as type
 *         and check-options
 */
public class Matrix
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** Store the data */
	private double[]	mData;
	
	/** Width of CheckMatrix */
	private final int	mColes;
	/** Height of CheckMatrix */
	private final int	mRows;
	/** */
	private int			mTmp;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Construct a matrix filled with s
	 * 
	 * @param rows Number of rows.
	 * @param coles Number of columns.
	 * @param s Fill the matrix with this scalar value.
	 */
	public Matrix(final int rows, final int coles, final double s)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		
		mRows = rows;
		mColes = coles;
		mData = new double[mRows * mColes];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				mData[mTmp + j] = s;
			}
		}
	}
	
	
	/**
	 * Construct an matrix, filled with zero
	 * 
	 * @param size Number of rows and columns.
	 */
	public Matrix(final int size)
	{
		checkRowAndColumnCreateDimension(size, size);
		
		mRows = size;
		mColes = size;
		mData = new double[mRows * mColes];
	}
	
	
	/**
	 * Construct Matrix and fill with zeros
	 * 
	 * @param rows Number of rows.
	 * @param coles Number of columns.
	 */
	public Matrix(final int rows, final int coles)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		
		mRows = rows;
		mColes = coles;
		mData = new double[mRows * mColes];
	}
	
	
	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param data Two-dimensional array of doubles.
	 * @exception IllegalArgumentException All rows must have the same length
	 */
	public Matrix(final double[][] data)
	{
		mRows = data.length;
		mColes = data[0].length;
		
		checkArrayLength(data, mRows, mColes);
		
		// copy content
		mData = new double[mRows * mColes];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				mData[mTmp + j] = data[i][j];
			}
		}
	}
	
	
	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param data Two-dimensional array of doubles.
	 * @param rows
	 * @param coles
	 * @exception IllegalArgumentException All rows must have the same length
	 * @see #copy()
	 */
	public Matrix(final double[][] data, final int rows, final int coles)
	{
		mRows = rows;
		mColes = coles;
		
		checkRowAndColumnCreateDimension(mRows, mColes);
		checkArrayLength(data, mRows, mColes);
		
		// copy content
		mData = new double[mRows * mColes];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				mData[mTmp + j] = data[i][j];
			}
		}
	}
	
	
	/**
	 * make a deep copy of an one-dimensional matrix
	 * 
	 * @param data
	 * @return
	 */
	private double[] copyDoubleArray(final double[] data)
	{
		final double[] copy = new double[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return copy;
	}
	
	
	// --------------------------------------------------------------
	// --- copy-constructor(s) --------------------------------------
	// --------------------------------------------------------------
	
	/**
	 * Perform a deep copy of the matrix.
	 * 
	 * @return deep copy of the matrix
	 */
	public Matrix copy()
	{
		// create a deep copy of the matrix
		return new Matrix(mData, mRows, mColes, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Generate identity matrix
	 * 
	 * @param rows Number of rows.
	 * @param coles Number of columns.
	 * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
	 */
	public static Matrix identity(final int rows, final int coles)
	{
		if ((rows < 0) || (coles < 0))
		{
			throw new IllegalArgumentException("You can't access Row/column: " + rows + "/" + coles);
		}
		
		// allocate new space
		// fill with data
		final double[] array = new double[rows * coles];
		int tmp = 0;
		for (int i = 0; i < rows; i++)
		{
			tmp = coles * i;
			for (int j = 0; j < coles; j++)
			{
				array[tmp + j] = (i == j ? 1.0 : 0.0);
			}
		}
		return new Matrix(array, rows, coles, true);
	}
	
	
	/**
	 * Create matrix out of 1d-array
	 * 
	 * @param data one dimensional data-array
	 * @param rows number of rows
	 * @param coles number of columns
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result;
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 */
	public Matrix(final double[] data, final int rows, final int coles, final boolean changeThisMemory)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		checkArrayLength(data, rows, coles);
		
		mRows = rows;
		mColes = coles;
		
		// copy content
		if (!changeThisMemory)
		{
			mData = copyDoubleArray(data);
		} else
		{
			mData = data;
		}
	}
	
	
	/**
	 * create an identity matrix
	 * 
	 * @return
	 */
	public Matrix identity()
	{
		return Matrix.identity(mRows, mColes);
	}
	
	
	/**
	 * create a identity matrix
	 * 
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return
	 */
	public Matrix identity(final boolean changeThisMemory)
	{
		if (changeThisMemory)
		{
			int i = -1;
			int j = -1;
			for (i = 0; i < mRows; i++)
			{
				mTmp = mColes * i;
				for (j = 0; j < mColes; j++)
				{
					mData[mTmp + j] = (i == j ? 1.0 : 0.0);
				}
			}
			return this;
		}
		return Matrix.identity(mRows, mColes);
	}
	
	
	/**
	 * Access the internal array.
	 * 
	 * @return Pointer to the one-dimensional array of matrix elements.
	 */
	private double[] getArray()
	{
		return mData;
	}
	
	
	/**
	 * C = A+B
	 * 
	 * @param b
	 * @return C
	 */
	public Matrix plus(final Matrix b)
	{
		final Matrix a = copy();
		return a.plus(b, true);
	}
	
	
	/**
	 * C = A+B
	 * 
	 * @param b
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return C
	 */
	public Matrix plus(final Matrix b, final boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			final Matrix c = copy();
			return c.plus(b, true);
		}
		final double[] bData = b.getArray();
		
		if ((mRows != b.getRowDimension()) || (mColes != b.getColumnDimension()))
		{
			throw new IllegalArgumentException("All matrices should have the same size.");
		}
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				mData[mTmp + j] = mData[mTmp + j] + bData[mTmp + j];
			}
		}
		return this;
	}
	
	
	/**
	 * C = A-B
	 * 
	 * @param b
	 * @return C
	 */
	public Matrix minus(final Matrix b)
	{
		final Matrix c = copy();
		return c.minus(b, true);
	}
	
	
	/**
	 * C = A-B
	 * 
	 * @param b
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return
	 */
	public Matrix minus(final Matrix b, final boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			final Matrix c = copy();
			return c.minus(b, true);
		}
		final double[] bData = b.getArray();
		
		if ((mRows != b.getRowDimension()) || (mColes != b.getColumnDimension()))
		{
			throw new IllegalArgumentException("All matrices should have the same size.");
		}
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				mData[mTmp + j] = mData[mTmp + j] - bData[mTmp + j];
			}
		}
		return this;
	}
	
	
	/**
	 * C = At
	 * 
	 * @return CheckMatrix with the Result
	 */
	public Matrix transpose()
	{
		
		final double[] array = new double[mColes * mRows];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < mRows; i++)
		{
			mTmp = mColes * i;
			for (j = 0; j < mColes; j++)
			{
				array[(j * mRows) + i] = mData[mTmp + j];
			}
		}
		return new Matrix(array, mColes, mRows, true);
	}
	
	
	/**
	 * C = A*B
	 * 
	 * @param b
	 * @return C
	 */
	public Matrix times(final Matrix b)
	{
		
		final double[] bData = b.getArray();
		
		final int bRows = b.getRowDimension();
		final int bColes = b.getColumnDimension();
		
		if (mColes != bRows)
		{
			throw new IllegalArgumentException("The first matrix columns must be the same as the second matrix rows");
		}
		
		final double[] array = new double[mRows * bColes];
		double val = 0;
		
		int i = -1;
		int j = -1;
		int k = -1;
		// go through all elements in the new matrix by line
		for (i = 0; i < mRows; i++)
		{
			// and column
			for (j = 0; j < bColes; j++)
			{
				// and go through the current line/column
				for (k = 0; k < mColes; k++)
				{
					val += mData[(i * mColes) + k] * bData[(k * bColes) + j];
				}
				array[(i * bColes) + j] = val;
				val = 0.0;
			}
		}
		return new Matrix(array, mRows, bColes, true);
	}
	
	
	/**
	 * C = A^-1
	 * only for psd matrices
	 * 
	 * @return C
	 * @exception IllegalArgumentException Only a squared matrix can be inverted
	 */
	public Matrix inverseByCholesky()
	{
		// if matrix is small
		if (mRows < 5)
		{
			// take hardcoded inverse
			return inverse();
		}
		// solve with cholesky
		return this.solveCholesky(this.identity(false));
	}
	
	
	/**
	 * C = A^-1
	 * 
	 * @return C
	 */
	public Matrix inverse()
	{
		final Matrix c = copy();
		return c.inverse(true);
	}
	
	
	/**
	 * C = A^-1
	 * This function is only implemented for 2x2, 3x3 and 4x4!!!
	 * 
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return C
	 * @exception IllegalArgumentException Only a squared matrix can be inverted
	 */
	public Matrix inverse(final boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			final Matrix c = copy();
			return c.inverse(true);
		}
		if (mColes != mRows)
		{
			throw new IllegalArgumentException("Only a squared matrix can be inverted");
		}
		
		if (mColes == 4)
		{
			return invert4x4();
		} else if (mColes == 3)
		{
			return invert3x3();
		} else if (mColes == 2)
		{
			return invert2x2();
		} else if (mColes > 4)
		{
			final Matrix x = solveLR(this.identity());
			mData = x.getArray();
		}
		return this;
	}
	
	
	private double adjunkte(final int line, final int column)
	{
		double faktor = 1;
		if (((line + column) % 2) != 0)
		{
			faktor = -1;
		}
		
		double adj;
		
		final Matrix unterDet = remove(line, column);
		
		if (unterDet.getRowDimension() == 2)
		{
			if (line == column)
			{
				if (line == 0)
				{
					adj = faktor * unterDet.get(1, 1);
				} else
				{
					adj = faktor * unterDet.get(0, 0);
				}
			} else
			{
				adj = faktor * unterDet.get(column, line);
			}
		} else
		{
			adj = (faktor * unterDet.det(false));
		}
		return adj;
	}
	
	
	/**
	 * Matrix
	 * Calculates the determinante of a matrix
	 * For big matrices, it is slow
	 * 
	 * @param checkDetisZero
	 * @return double with result
	 * @exception IllegalArgumentException Only a squared matrix has a determinant
	 */
	public double det(final boolean checkDetisZero)
	{
		
		if (mColes != mRows)
		{
			throw new IllegalArgumentException("Only a squared matrix has a determinant");
		}
		
		double det = 0;
		if (mRows == 2)
		{
			det = ((mData[0] * mData[mColes + 1]) - (mData[1] * mData[mColes]));
		} else if (mRows == 3)
		{
			det = ((mData[0] * mData[mColes + 1] * mData[(2 * mColes) + 2])
					+ (mData[1] * mData[mColes + 2] * mData[2 * mColes]) + (mData[2] * mData[mColes] * mData[(2 * mColes) + 1]))
					- (mData[1] * mData[mColes] * mData[(2 * mColes) + 2])
					- (mData[0] * mData[mColes + 2] * mData[(2 * mColes) + 1])
					- (mData[2] * mData[mColes + 1] * mData[2 * mColes]);
		} else
		{
			for (int i = 0; i < mRows; i++)
			{
				det += mData[i] * adjunkte(0, i);
			}
		}
		
		if (checkDetisZero && (det == 0))
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		return det;
	}
	
	
	/**
	 * This function removes one line and one column from a matrix and creates a new one
	 */
	
	private Matrix remove(final int delLine, final int delColumn)
	{
		final double[] newArray = new double[(mRows - 1) * (mColes - 1)];
		
		int offsetLine = 0;
		int offsetColumn = 0;
		
		for (int i = 0; i < (mRows - 1); i++)
		{
			if (i == delLine)
			{
				offsetLine = 1;
			}
			for (int j = 0; j < (mColes - 1); j++)
			{
				if (j == delColumn)
				{
					offsetColumn = 1;
				}
				newArray[(i * (mColes - 1)) + j] = mData[((i + offsetLine) * mColes) + j + offsetColumn];
			}
			offsetColumn = 0;
		}
		return new Matrix(newArray, mRows - 1, mColes - 1, true);
	}
	
	
	/**
	 * Invert this 2x2 matrix.
	 */
	private Matrix invert2x2()
	{
		final double[] array = new double[4];
		
		final double det = det(true);
		
		final double faktor = 1.0 / det;
		array[0] = faktor * mData[mColes + 1];
		array[1] = faktor * -mData[1];
		array[mColes] = faktor * -mData[mColes];
		array[mColes + 1] = faktor * mData[0];
		
		mData = array;
		return this;
	}
	
	
	/**
	 * Invert this 3x3 matrix.
	 */
	private Matrix invert3x3()
	{
		final double[] array = new double[9];
		
		final double det = det(true);
		
		if (det == 0)
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		
		final double faktor = 1.0 / det;
		
		array[0] = faktor
				* ((mData[(1 * mColes) + 1] * mData[(2 * mColes) + 2]) - (mData[(1 * mColes) + 2] * mData[(2 * mColes) + 1]));
		array[1] = faktor * ((mData[2] * mData[(2 * mColes) + 1]) - (mData[1] * mData[(2 * mColes) + 2]));
		array[2] = faktor * ((mData[1] * mData[(1 * mColes) + 2]) - (mData[2] * mData[(1 * mColes) + 1]));
		
		array[mColes] = faktor
				* ((mData[(1 * mColes) + 2] * mData[2 * mColes]) - (mData[mColes] * mData[(2 * mColes) + 2]));
		array[(1 * mColes) + 1] = faktor * ((mData[0] * mData[(2 * mColes) + 2]) - (mData[2] * mData[2 * mColes]));
		array[(1 * mColes) + 2] = faktor * ((mData[2] * mData[mColes]) - (mData[0] * mData[(1 * mColes) + 2]));
		
		array[2 * mColes] = faktor
				* ((mData[mColes] * mData[(2 * mColes) + 1]) - (mData[(1 * mColes) + 1] * mData[2 * mColes]));
		array[(2 * mColes) + 1] = faktor * ((mData[1] * mData[2 * mColes]) - (mData[0] * mData[(2 * mColes) + 1]));
		array[(2 * mColes) + 2] = faktor * ((mData[0] * mData[(1 * mColes) + 1]) - (mData[1] * mData[mColes]));
		
		mData = array;
		return this;
	}
	
	
	/**
	 * Invert this 4x4 matrix.
	 */
	private Matrix invert4x4()
	{
		final double[] tmp = new double[12];
		final double[] src = new double[16];
		final double[] dst = new double[16];
		
		// Transpose matrix
		int i = -1;
		for (i = 0; i < 4; i++)
		{
			src[i + 0] = mData[(i * mColes) + 0];
			src[i + 4] = mData[(i * mColes) + 1];
			src[i + 8] = mData[(i * mColes) + 2];
			src[i + 12] = mData[(i * mColes) + 3];
		}
		
		// Calculate pairs for first 8 elements (cofactors)
		tmp[0] = src[10] * src[15];
		tmp[1] = src[11] * src[14];
		tmp[2] = src[9] * src[15];
		tmp[3] = src[11] * src[13];
		tmp[4] = src[9] * src[14];
		tmp[5] = src[10] * src[13];
		tmp[6] = src[8] * src[15];
		tmp[7] = src[11] * src[12];
		tmp[8] = src[8] * src[14];
		tmp[9] = src[10] * src[12];
		tmp[10] = src[8] * src[13];
		tmp[11] = src[9] * src[12];
		
		// Calculate first 8 elements (cofactors)
		dst[0] = (tmp[0] * src[5]) + (tmp[3] * src[6]) + (tmp[4] * src[7]);
		dst[0] -= (tmp[1] * src[5]) + (tmp[2] * src[6]) + (tmp[5] * src[7]);
		dst[1] = (tmp[1] * src[4]) + (tmp[6] * src[6]) + (tmp[9] * src[7]);
		dst[1] -= (tmp[0] * src[4]) + (tmp[7] * src[6]) + (tmp[8] * src[7]);
		dst[2] = (tmp[2] * src[4]) + (tmp[7] * src[5]) + (tmp[10] * src[7]);
		dst[2] -= (tmp[3] * src[4]) + (tmp[6] * src[5]) + (tmp[11] * src[7]);
		dst[3] = (tmp[5] * src[4]) + (tmp[8] * src[5]) + (tmp[11] * src[6]);
		dst[3] -= (tmp[4] * src[4]) + (tmp[9] * src[5]) + (tmp[10] * src[6]);
		dst[4] = (tmp[1] * src[1]) + (tmp[2] * src[2]) + (tmp[5] * src[3]);
		dst[4] -= (tmp[0] * src[1]) + (tmp[3] * src[2]) + (tmp[4] * src[3]);
		dst[5] = (tmp[0] * src[0]) + (tmp[7] * src[2]) + (tmp[8] * src[3]);
		dst[5] -= (tmp[1] * src[0]) + (tmp[6] * src[2]) + (tmp[9] * src[3]);
		dst[6] = (tmp[3] * src[0]) + (tmp[6] * src[1]) + (tmp[11] * src[3]);
		dst[6] -= (tmp[2] * src[0]) + (tmp[7] * src[1]) + (tmp[10] * src[3]);
		dst[7] = (tmp[4] * src[0]) + (tmp[9] * src[1]) + (tmp[10] * src[2]);
		dst[7] -= (tmp[5] * src[0]) + (tmp[8] * src[1]) + (tmp[11] * src[2]);
		
		// Calculate pairs for second 8 elements (cofactors)
		tmp[0] = src[2] * src[7];
		tmp[1] = src[3] * src[6];
		tmp[2] = src[1] * src[7];
		tmp[3] = src[3] * src[5];
		tmp[4] = src[1] * src[6];
		tmp[5] = src[2] * src[5];
		tmp[6] = src[0] * src[7];
		tmp[7] = src[3] * src[4];
		tmp[8] = src[0] * src[6];
		tmp[9] = src[2] * src[4];
		tmp[10] = src[0] * src[5];
		tmp[11] = src[1] * src[4];
		
		// Calculate second 8 elements (cofactors)
		dst[8] = (tmp[0] * src[13]) + (tmp[3] * src[14]) + (tmp[4] * src[15]);
		dst[8] -= (tmp[1] * src[13]) + (tmp[2] * src[14]) + (tmp[5] * src[15]);
		dst[9] = (tmp[1] * src[12]) + (tmp[6] * src[14]) + (tmp[9] * src[15]);
		dst[9] -= (tmp[0] * src[12]) + (tmp[7] * src[14]) + (tmp[8] * src[15]);
		dst[10] = (tmp[2] * src[12]) + (tmp[7] * src[13]) + (tmp[10] * src[15]);
		dst[10] -= (tmp[3] * src[12]) + (tmp[6] * src[13]) + (tmp[11] * src[15]);
		dst[11] = (tmp[5] * src[12]) + (tmp[8] * src[13]) + (tmp[11] * src[14]);
		dst[11] -= (tmp[4] * src[12]) + (tmp[9] * src[13]) + (tmp[10] * src[14]);
		dst[12] = (tmp[2] * src[10]) + (tmp[5] * src[11]) + (tmp[1] * src[9]);
		dst[12] -= (tmp[4] * src[11]) + (tmp[0] * src[9]) + (tmp[3] * src[10]);
		dst[13] = (tmp[8] * src[11]) + (tmp[0] * src[8]) + (tmp[7] * src[10]);
		dst[13] -= (tmp[6] * src[10]) + (tmp[9] * src[11]) + (tmp[1] * src[8]);
		dst[14] = (tmp[6] * src[9]) + (tmp[11] * src[11]) + (tmp[3] * src[8]);
		dst[14] -= (tmp[10] * src[11]) + (tmp[2] * src[8]) + (tmp[7] * src[9]);
		dst[15] = (tmp[10] * src[10]) + (tmp[4] * src[8]) + (tmp[9] * src[9]);
		dst[15] -= (tmp[8] * src[9]) + (tmp[11] * src[10]) + (tmp[5] * src[8]);
		
		// Calculate determinant
		double det = (src[0] * dst[0]) + (src[1] * dst[1]) + (src[2] * dst[2]) + (src[3] * dst[3]);
		
		if (det == 0)
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		
		// new instance:
		final double[] array = new double[16];
		
		// Calculate matrix inverse
		det = 1.0 / det;
		
		int j = -1;
		for (i = 0; i < 4; i++)
		{
			for (j = 0; j < 4; j++)
			{
				array[(i * mColes) + j] = dst[(i * 4) + j] * det;
			}
		}
		mData = array;
		return this;
	}
	
	
	/**
	 * A*X = B
	 * 
	 * @param b
	 * @return
	 */
	public Matrix solveLR(final Matrix b)
	{
		if (getRowDimension() != b.getRowDimension())
		{
			throw new IllegalArgumentException("Only matrices with same row-number are possible as input");
		}
		
		if (mRows != mColes)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		// allocate space for the result X
		final Matrix a = new Matrix(mRows);
		final Matrix x = new Matrix(a.getColumnDimension(), b.getColumnDimension());
		final int cols = x.getColumnDimension();
		
		// set the column vector b
		final double[] colB = new double[mRows];
		final double[] colX = new double[mRows];
		
		int i = -1;
		
		for (int jResult = 0; jResult < cols; jResult++)
		{
			// fill the column vector from the current column
			for (i = 0; i < mRows; i++)
			{
				colB[i] = b.get(i, jResult);
				
			}
			
			copyThisArrayDataTo(a);
			a.solveLROneVector(colB, colX);
			
			// fill the column vector from the current column
			for (i = 0; i < mRows; i++)
			{
				x.set(i, jResult, colX[i]);
			}
		}
		return x;
	}
	
	
	/**
	 * A*x = b
	 * 
	 * @param b
	 * @param x
	 * @return x
	 */
	private double[] solveLROneVector(final double[] b, final double[] x)
	{
		partitionLR(b);
		
		int i = -1;
		int j = -1;
		
		for (j = mRows - 1; j >= 0; j--)
		{
			double sum = b[j];
			for (i = j + 1; i <= (mRows - 1); i++)
			{
				sum = sum - (mData[(j * mRows) + i] * x[i]);
			}
			x[j] = sum / mData[(j * mRows) + j];
		}
		return x;
	}
	
	
	/**
	 * A=L*R; b=y
	 * 
	 * @param b
	 * @return
	 */
	private Matrix partitionLR(final double[] b)
	{
		int i = -1;
		int j = -1;
		int k = -1;
		// calculate normalized main-diagonal
		final double[] diagonal = new double[mRows];
		
		for (j = 0; j < (mRows - 1); j++)
		{
			changeCurrRowWithBestNumericFit(b, j, diagonal);
			if (mData[(j * mColes) + j] == 0.0)
			{
				throw new IllegalArgumentException("Only Matrices with main-diagonal without zeros can be calculated");
			}
			for (i = j + 1; i <= (mRows - 1); i++)
			{
				mTmp = i * mColes;
				// entspricht L[i][j]
				mData[mTmp + j] = mData[mTmp + j] / mData[(j * mColes) + j];
				
				b[i] = b[i] - (b[j] * mData[mTmp + j]);
				for (k = j + 1; k <= (mRows - 1); k++)
				{
					mData[mTmp + k] = mData[mTmp + k] - (mData[(j * mColes) + k] * mData[mTmp + j]);
				}
			}
		}
		return this;
	}
	
	
	/**
	 * change the rows by the current best numeric fit
	 * 'zeilenvertauschung mit spaltenpivotisierung'
	 * 
	 * @param b
	 * @param curDepth
	 */
	private void changeCurrRowWithBestNumericFit(final double[] b, final int curDepth, final double[] diagonal)
	{
		int i = -1;
		int k = -1;
		for (i = curDepth; i < mRows; i++)
		{
			mTmp = i * mColes;
			// calculate "Zeilennorm der iten Zeile"
			double sum = 0;
			for (k = curDepth; k < mColes; k++)
			{
				sum += Math.pow(mData[mTmp + k], 2);
			}
			// fill diagonal.vector
			diagonal[i] = 1.0 / Math.sqrt(sum);
		}
		
		
		// calculate highest element-index
		double maxVal = -1;
		int id = -1;
		// go through column
		for (k = curDepth; k < mRows; k++)
		{
			final double val = diagonal[k] * Math.abs(mData[(k * mColes) + curDepth]);
			if (val > maxVal)
			{
				maxVal = val;
				id = k;
			}
		}
		
		// need we a row-change?
		if (id == curDepth)
		{
			return;
		}
		// switch lines
		final double[] tmpArr = new double[mRows];
		
		// tmp = a
		mTmp = curDepth * mColes;
		for (int l = 0; l < mRows; l++)
		{
			tmpArr[l] = mData[mTmp + l];
			
		}
		double tmpVal = b[curDepth];
		
		// a = b
		for (int l = 0; l < mRows; l++)
		{
			mData[mTmp + l] = mData[(id * mColes) + l];
		}
		b[curDepth] = b[id];
		
		// b = tmp
		for (int l = 0; l < mRows; l++)
		{
			mData[(id * mColes) + l] = tmpArr[l];
		}
		b[id] = tmpVal;
	}
	
	
	/**
	 * A*X = B
	 * 
	 * @param b
	 * @return
	 */
	public Matrix solveCholesky(final Matrix b)
	{
		if (getRowDimension() != b.getRowDimension())
		{
			throw new IllegalArgumentException("Only matrices with same row-number are possible as input");
		}
		
		if (mRows != mColes)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		// generate C*Ct
		final Matrix cCt = copy();
		cCt.partitionCholesky();
		
		// allocate space for the result X
		final Matrix x = new Matrix(cCt.getColumnDimension(), b.getColumnDimension());
		final int xColes = x.getColumnDimension();
		
		// set the column vector b
		final double[] colB = new double[mRows];
		// allocate some space for a temporary vector
		final double[] colY = new double[mRows];
		// allocate space for new matrix
		final double[] colX = new double[mRows];
		
		for (int jResult = 0; jResult < xColes; jResult++)
		{
			// fill the column vector from the current column
			for (int i = 0; i < mRows; i++)
			{
				colB[i] = b.get(i, jResult);
			}
			cCt.solveCholOneVector(colB, colX, colY);
			// fill the column vector from the current column
			for (int i = 0; i < mRows; i++)
			{
				x.set(i, jResult, colX[i]);
			}
		}
		return x;
	}
	
	
	private void solveCholOneVector(final double[] b, final double[] x, final double[] y)
	{
		
		// Ct*y=b
		for (int i = 0; i <= (mRows - 1); i++)
		{
			double sum = b[i];
			for (int j = 0; j < i; j++)
			{
				sum -= y[j] * mData[(j * mRows) + i];
			}
			y[i] = sum / mData[(i * mRows) + i];
		}
		
		// C*x=y
		for (int i = mRows - 1; i >= 0; i--)
		{
			double sum = y[i];
			for (int j = mRows - 1; j > i; j--)
			{
				sum -= x[j] * mData[(i * mColes) + j];
			}
			x[i] = sum / mData[(i * mColes) + i];
		}
	}
	
	
	/**
	 * A*x = b
	 * 
	 * @param b
	 * @return x
	 */
	public double[] solveCholesky(final double[] b)
	{
		if (mRows != mColes)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		final Matrix c = copy();
		c.partitionCholesky();
		final double[] data = c.getArray();
		
		// allocate some space for the result-vector
		final double[] x = new double[mRows];
		
		// allocate some space for a temporary vector
		final double[] y = new double[mRows];
		
		// CT*y=b
		for (int j = 0; j <= (mRows - 1); j++)
		{
			double sum = b[j];
			for (int k = 0; k <= j; k++)
			{
				sum -= y[k] * data[(k * mRows) + j];
			}
			y[j] = sum / data[(j * mRows) + j];
		}
		
		// C*x=y
		for (int j = mRows - 1; j >= 0; j--)
		{
			double sum = y[j];
			for (int k = mRows - 1; k >= j; k--)
			{
				sum -= x[k] * data[(j * mColes) + k];
			}
			x[j] = sum / data[(j * mColes) + j];
		}
		return x;
	}
	
	
	/**
	 * A = C*Ct
	 */
	private Matrix partitionCholesky()
	{
		
		// fill the lines with values
		for (int j = 0; j < mRows; j++)
		{
			for (int k = 0; k < mRows; k++)
			{
				if (j > k)
				{
					mData[(j * mColes) + k] = 0.0;
				} else if (j == k)
				{
					double sum = mData[(j * mColes) + j];
					for (int i = 0; i < j; i++)
					{
						sum -= Math.pow(mData[(i * mColes) + j], 2);
					}
					mData[(j * mColes) + j] = Math.sqrt(sum);
				} else
				{
					double sum = mData[(j * mColes) + k];
					for (int i = 0; i < j; i++)
					{
						sum -= mData[(i * mColes) + j] * mData[(i * mColes) + k];
					}
					mData[(j * mRows) + k] = sum / mData[(j * mRows) + j];
				}
			}
		}
		return this;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getters and setters --------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public int getRowDimension()
	{
		return mRows;
	}
	
	
	/**
	 * @return
	 */
	public int getColumnDimension()
	{
		return mColes;
	}
	
	
	/**
	 * @param row
	 * @param col
	 * @return
	 */
	public double get(final int row, final int col)
	{
		checkRowAndColumnAccess(row, col);
		return mData[(row * mColes) + col];
	}
	
	
	/**
	 * without check for access in correct area
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public double at(final int row, final int col)
	{
		return mData[(row * mColes) + col];
	}
	
	
	/**
	 * @param row
	 * @param col
	 * @param value
	 */
	public void set(final int row, final int col, final double value)
	{
		checkRowAndColumnAccess(row, col);
		mData[(row * mColes) + col] = value;
	}
	
	
	// --------------------------------------------------------------------------
	// --- help-functions -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void checkRowAndColumnCreateDimension(final int row, final int col)
	{
		if ((row < 1) || (col < 1))
		{
			throw new IllegalArgumentException("The Row and Column has to be at least 1");
		}
	}
	
	
	private void checkRowAndColumnAccess(final int row, final int col)
	{
		
		if ((row > mRows) || (col > mColes) || (row < 0) || (col < 0))
		{
			throw new IllegalArgumentException("You cant acces Row/column: " + row + "/" + col);
		}
	}
	
	
	private void checkArrayLength(final double[][] data, final int rows, final int coles)
	{
		// check the data
		int i = -1;
		for (i = 0; i < rows; i++)
		{
			if (data[i].length != coles)
			{
				throw new IllegalArgumentException("All rows must have the same length.");
			}
		}
	}
	
	
	private void checkArrayLength(final double[] data, final int rows, final int coles)
	{
		// check whole size
		if ((rows * coles) != data.length)
		{
			throw new IllegalArgumentException("There are not enough or to many elements");
		}
	}
	
	
	/*
	 * private String toString(double[] a)
	 * {
	 * String str = "";
	 * int i = -1;
	 * for (i = 0; i < a.length; i++)
	 * {
	 * str += "[" + a[i] + "]";
	 * }
	 * return str;
	 * }
	 */
	
	
	/**
	 * Converts the CheckMatrix to an string, to print in a nice form
	 */
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		int i = -1;
		int j = -1;
		// for all rows
		for (i = 0; i < mRows; i++)
		{
			for (j = 0; j < mColes; j++)
			{
				str.append('[');
				String formattedData = String.format("% 10.4e ", mData[(i * mColes) + j]);
				str.append(formattedData);
				str.append(']');
			}
			str.append('\n');
		}
		return str.toString();
	}
	
	
	/**
	 * Function, to copy the content
	 * 
	 * @param a
	 */
	private void copyThisArrayDataTo(final Matrix a)
	{
		final double[] data = a.getArray();
		for (int i = 0; i < mRows; i++)
		{
			mTmp = i * mColes;
			for (int j = 0; j < mColes; j++)
			{
				data[mTmp + j] = mData[mTmp + j];
			}
		}
	}
}
