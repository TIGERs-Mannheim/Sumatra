/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;


/**
 * @author BirgitD
 * 
 *         This class provides elementary matrix-functions with double as type
 *         and check-options
 * 
 */
public class Matrix
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param m_data Store the data
	 */
	private double[]	m_data;
	
	/**
	 * @param m_coles Width of CheckMatrix
	 * @param m_rows Height of CheckMatrix
	 * @param m_tmp
	 */
	private int			m_coles;
	
	private int			m_rows;
	
	private int			m_tmp;
	
	
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
	public Matrix(int rows, int coles, double s)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		
		m_rows = rows;
		m_coles = coles;
		m_data = new double[m_rows * m_coles];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < m_rows; i++)
		{
			m_tmp = m_coles * i;
			for (j = 0; j < m_coles; j++)
			{
				m_data[m_tmp + j] = s;
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
		
		m_rows = size;
		m_coles = size;
		m_data = new double[m_rows * m_coles];
	}
	

	/**
	 * Construct Matrix and fill with zeros
	 * 
	 * @param rows Number of rows.
	 * @param coles Number of columns.
	 */
	public Matrix(int rows, int coles)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		
		m_rows = rows;
		m_coles = coles;
		m_data = new double[m_rows * m_coles];
	}
	

	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param data Two-dimensional array of doubles.
	 * @param alloc new space for data or use the given space
	 * @exception IllegalArgumentException All rows must have the same length
	 * @see #constructWithCopy
	 */
	public Matrix(final double[][] data)
	{
		m_rows = data.length;
		m_coles = data[0].length;
		
		checkArrayLength(data, m_rows, m_coles);
		
		// copy content
		m_data = new double[m_rows * m_coles];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < m_rows; i++)
		{
			m_tmp = m_coles * i;
			for (j = 0; j < m_coles; j++)
			{
				m_data[m_tmp + j] = data[i][j];
			}
		}
	}
	

	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param data Two-dimensional array of doubles.
	 * @param alloc new space for data or use the given space
	 * @exception IllegalArgumentException All rows must have the same length
	 * @see #constructWithCopy
	 */
	public Matrix(final double[][] data, int rows, int coles)
	{
		m_rows = rows;
		m_coles = coles;
		
		checkRowAndColumnCreateDimension(m_rows, m_coles);
		checkArrayLength(data, m_rows, m_coles);
		
		// copy content
		m_data = new double[m_rows * m_coles];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < m_rows; i++)
		{
			m_tmp = m_coles * i;
			for (j = 0; j < m_coles; j++)
			{
				m_data[m_tmp + j] = data[i][j];
			}
		}
	}
	

	/**
	 * 
	 * make a deep copy of an one-dimensional matrix
	 * @param data
	 * @return
	 */
	private double[] copyDoubleArray(double[] data)
	{
		double[] copy = new double[data.length];
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
		Matrix mat = new Matrix(m_data, m_rows, m_coles, false);
		return mat;
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
	public static Matrix Identity(final int rows, final int coles)
	{
		if (rows < 0 || coles < 0)
		{
			throw new IllegalArgumentException("You cant acces Row/column: " + rows + "/" + coles);
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
		Matrix A = new Matrix(array, rows, coles, true);
		return A;
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
	public Matrix(double[] data, int rows, int coles, boolean changeThisMemory)
	{
		checkRowAndColumnCreateDimension(rows, coles);
		checkArrayLength(data, rows, coles);
		
		m_rows = rows;
		m_coles = coles;
		
		// copy content
		if (!changeThisMemory)
		{
			m_data = copyDoubleArray(data);
		} else
		{
			m_data = data;
		}
	}
	

	/**
	 * create an identity matrix
	 * 
	 * @param changeThisMemory decide, weather new space has to be allocated or take the current one
	 * @return
	 */
	public Matrix identity()
	{
		return Matrix.Identity(m_rows, m_coles);
	}
	

	/**
	 * createn a identity matrix
	 * 
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return
	 */
	public Matrix identity(boolean changeThisMemory)
	{
		if (changeThisMemory)
		{
			int i = -1;
			int j = -1;
			for (i = 0; i < m_rows; i++)
			{
				m_tmp = m_coles * i;
				for (j = 0; j < m_coles; j++)
				{
					m_data[m_tmp + j] = (i == j ? 1.0 : 0.0);
				}
			}
			return this;
		} else
		{
			return Matrix.Identity(m_rows, m_coles);
		}
	}
	

	/**
	 * Access the internal array.
	 * 
	 * @return Pointer to the one-dimensional array of matrix elements.
	 */
	private double[] getArray()
	{
		return m_data;
	}
	

	/**
	 * 
	 * C = A+B
	 * 
	 * @param B
	 * @return C
	 */
	public Matrix plus(Matrix B)
	{
		Matrix A = this.copy();
		return A.plus(B, true);
	}
	

	/**
	 * 
	 * C = A+B
	 * 
	 * @param B
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * 
	 * @return C
	 */
	public Matrix plus(Matrix B, boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			Matrix C = this.copy();
			return C.plus(B, true);
		} else
		{
			double[] B_data = B.getArray();
			
			if ((m_rows != B.getRowDimension()) || (m_coles != B.getColumnDimension()))
			{
				throw new IllegalArgumentException("All matrices should have the same size.");
			}
			
			int i = -1;
			int j = -1;
			for (i = 0; i < m_rows; i++)
			{
				m_tmp = m_coles * i;
				for (j = 0; j < m_coles; j++)
				{
					m_data[m_tmp + j] = m_data[m_tmp + j] + B_data[m_tmp + j];
				}
			}
			return this;
		}
	}
	

	/**
	 * 
	 * C = A-B
	 * 
	 * @param B
	 * @return C
	 */
	public Matrix minus(Matrix B)
	{
		Matrix C = this.copy();
		return C.minus(B, true);
	}
	

	/**
	 * 
	 * C = A-B
	 * 
	 * @param B
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * 
	 * @return
	 */
	public Matrix minus(Matrix B, boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			Matrix C = this.copy();
			return C.minus(B, true);
		} else
		{
			double[] B_data = B.getArray();
			
			if ((m_rows != B.getRowDimension()) || (m_coles != B.getColumnDimension()))
			{
				throw new IllegalArgumentException("All matrices should have the same size.");
			}
			
			int i = -1;
			int j = -1;
			for (i = 0; i < m_rows; i++)
			{
				m_tmp = m_coles * i;
				for (j = 0; j < m_coles; j++)
				{
					m_data[m_tmp + j] = m_data[m_tmp + j] - B_data[m_tmp + j];
				}
			}
			return this;
		}
	}
	

	/**
	 * C = At
	 * @return CheckMatrix with the Result
	 */
	public Matrix transpose()
	{
		
		double[] array = new double[m_coles * m_rows];
		
		int i = -1;
		int j = -1;
		for (i = 0; i < m_rows; i++)
		{
			m_tmp = m_coles * i;
			for (j = 0; j < m_coles; j++)
			{
				array[j * m_rows + i] = m_data[m_tmp + j];
			}
		}
		return new Matrix(array, m_coles, m_rows, true);
	}
	

	/**
	 * 
	 * C = A*B
	 * 
	 * @param B
	 * @return C
	 */
	public Matrix times(Matrix B)
	{
		
		double[] B_data = B.getArray();
		
		int B_rows = B.getRowDimension();
		int B_coles = B.getColumnDimension();
		
		if (m_coles != B_rows)
		{
			throw new IllegalArgumentException("The first matrix columns must be the same as the second matrix rows");
		}
		
		double[] array = new double[m_rows * B_coles];
		double val = 0;
		
		int i = -1;
		int j = -1;
		int k = -1;
		// go through all elements in the new matrix by line
		for (i = 0; i < m_rows; i++)
		{
			// and column
			for (j = 0; j < B_coles; j++)
			{
				// and go through the current line/column
				for (k = 0; k < m_coles; k++)
				{
					val += m_data[i * m_coles + k] * B_data[k * B_coles + j];
				}
				array[i * B_coles + j] = val;
				val = 0.0;
			}
		}
		return new Matrix(array, m_rows, B_coles, true);
	}
	

	/**
	 * C = A^-1
	 * 
	 * only for psd matrices
	 * 
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return C
	 * @exception IllegalArgumentException Only a squared matrix can be inverted
	 */
	public Matrix inverseByCholesky()
	{
		// if matrix is small
		if (m_rows < 5)
		{
			// take hardcoded inverse
			return inverse();
		} else
		{
			// solve with cholesky
			return this.solve_Cholesky(this.identity(false));
		}
	}
	

	/**
	 * C = A^-1
	 * 
	 * @return C
	 */
	public Matrix inverse()
	{
		Matrix C = this.copy();
		return C.inverse(true);
	}
	

	/**
	 * C = A^-1
	 * 
	 * This function is only implemented for 2x2, 3x3 and 4x4!!!
	 * 
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return C
	 * @exception IllegalArgumentException Only a squared matrix can be inverted
	 */
	public Matrix inverse(boolean changeThisMemory)
	{
		if (!changeThisMemory)
		{
			Matrix C = this.copy();
			return C.inverse(true);
		} else
		{
			if (m_coles != m_rows)
			{
				throw new IllegalArgumentException("Only a squared matrix can be inverted");
			}
			
			if (m_coles == 4)
			{
				return invert4x4();
			} else if (m_coles == 3)
			{
				return invert3x3();
			} else if (m_coles == 2)
			{
				return invert2x2();
			} else if (m_coles > 4)
			{
				Matrix X = this.solve_LR(this.identity());
				// Matrix X = this.solve_Cholesky(this.identity(false));
				m_data = X.getArray();
				// System.out.println(X);
				/*
				 * double det = det(true);
				 * double faktor = 1.0 / det;
				 * 
				 * double[] array = new double[m_rows * m_coles];
				 * 
				 * for (int i = 0; i < m_rows; i++)
				 * {
				 * for (int j = 0; j < m_coles; j++)
				 * {
				 * array[i * m_coles + j] = faktor * adjunkte(j, i);
				 * }
				 * }
				 * m_data = array;
				 */
			}
			return this;
		}
	}
	

	private double adjunkte(int line, int column)
	{
		double faktor = 1;
		if ((line + column) % 2 != 0)
		{
			faktor = -1;
		}
		
		double adj;
		
		Matrix unterDet = remove(line, column);
		
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
	 * Calculates the determinante of a matrix
	 * 
	 * For big matrices, it is slow
	 * 
	 * @return double with result
	 * @exception IllegalArgumentException Only a squared matrix has a determinant
	 */
	public double det(boolean checkDetisZero)
	{
		
		if (m_coles != m_rows)
		{
			throw new IllegalArgumentException("Only a squared matrix has a determinant");
		}
		
		double det = 0;
		if (m_rows == 2)
		{
			det = (m_data[0] * m_data[m_coles + 1] - m_data[1] * m_data[m_coles]);
		} else if (m_rows == 3)
		{
			det = m_data[0] * m_data[m_coles + 1] * m_data[2 * m_coles + 2] + m_data[1] * m_data[m_coles + 2]
					* m_data[2 * m_coles] + m_data[2] * m_data[m_coles] * m_data[2 * m_coles + 1] - m_data[1]
					* m_data[m_coles] * m_data[2 * m_coles + 2] - m_data[0] * m_data[m_coles + 2] * m_data[2 * m_coles + 1]
					- m_data[2] * m_data[m_coles + 1] * m_data[2 * m_coles];
		} else
		{
			for (int i = 0; i < m_rows; i++)
			{
				det += m_data[i] * adjunkte(0, i);
			}
		}
		
		if (checkDetisZero && det == 0)
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		return det;
	}
	

	/**
	 * This function removes one line and one column from a matrix and creates a new one
	 */
	
	private Matrix remove(int delLine, int delColumn)
	{
		double[] newArray = new double[(m_rows - 1) * (m_coles - 1)];
		
		int offsetLine = 0;
		int offsetColumn = 0;
		
		for (int i = 0; i < m_rows - 1; i++)
		{
			if (i == delLine)
				offsetLine = 1;
			for (int j = 0; j < m_coles - 1; j++)
			{
				if (j == delColumn)
					offsetColumn = 1;
				newArray[i * (m_coles - 1) + j] = m_data[(i + offsetLine) * m_coles + j + offsetColumn];
			}
			offsetColumn = 0;
		}
		Matrix A = new Matrix(newArray, m_rows - 1, m_coles - 1, true);
		return A;
	}
	

	/**
	 * Invert this 2x2 matrix.
	 */
	private Matrix invert2x2()
	{
		double[] array = new double[4];
		
		double det = this.det(true);
		
		double faktor = 1.0 / det;
		array[0] = faktor * m_data[m_coles + 1];
		array[1] = faktor * -m_data[1];
		array[m_coles] = faktor * -m_data[m_coles];
		array[m_coles + 1] = faktor * m_data[0];
		
		m_data = array;
		return this;
	}
	

	/**
	 * Invert this 3x3 matrix.
	 */
	private Matrix invert3x3()
	{
		double[] array = new double[9];
		
		double det = this.det(true);
		
		if (det == 0)
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		
		double faktor = 1.0 / det;
		
		array[0] = faktor
				* (m_data[1 * m_coles + 1] * m_data[2 * m_coles + 2] - m_data[1 * m_coles + 2] * m_data[2 * m_coles + 1]);
		array[1] = faktor * (m_data[2] * m_data[2 * m_coles + 1] - m_data[1] * m_data[2 * m_coles + 2]);
		array[2] = faktor * (m_data[1] * m_data[1 * m_coles + 2] - m_data[2] * m_data[1 * m_coles + 1]);
		
		array[m_coles] = faktor
				* (m_data[1 * m_coles + 2] * m_data[2 * m_coles] - m_data[m_coles] * m_data[2 * m_coles + 2]);
		array[1 * m_coles + 1] = faktor * (m_data[0] * m_data[2 * m_coles + 2] - m_data[2] * m_data[2 * m_coles]);
		array[1 * m_coles + 2] = faktor * (m_data[2] * m_data[m_coles] - m_data[0] * m_data[1 * m_coles + 2]);
		
		array[2 * m_coles] = faktor
				* (m_data[m_coles] * m_data[2 * m_coles + 1] - m_data[1 * m_coles + 1] * m_data[2 * m_coles]);
		array[2 * m_coles + 1] = faktor * (m_data[1] * m_data[2 * m_coles] - m_data[0] * m_data[2 * m_coles + 1]);
		array[2 * m_coles + 2] = faktor * (m_data[0] * m_data[1 * m_coles + 1] - m_data[1] * m_data[m_coles]);
		
		m_data = array;
		return this;
	}
	

	/**
	 * Invert this 4x4 matrix.
	 */
	private Matrix invert4x4()
	{
		double[] tmp = new double[12];
		double[] src = new double[16];
		double[] dst = new double[16];
		
		// Transpose matrix
		int i = -1;
		for (i = 0; i < 4; i++)
		{
			src[i + 0] = m_data[i * m_coles + 0];
			src[i + 4] = m_data[i * m_coles + 1];
			src[i + 8] = m_data[i * m_coles + 2];
			src[i + 12] = m_data[i * m_coles + 3];
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
		dst[0] = tmp[0] * src[5] + tmp[3] * src[6] + tmp[4] * src[7];
		dst[0] -= tmp[1] * src[5] + tmp[2] * src[6] + tmp[5] * src[7];
		dst[1] = tmp[1] * src[4] + tmp[6] * src[6] + tmp[9] * src[7];
		dst[1] -= tmp[0] * src[4] + tmp[7] * src[6] + tmp[8] * src[7];
		dst[2] = tmp[2] * src[4] + tmp[7] * src[5] + tmp[10] * src[7];
		dst[2] -= tmp[3] * src[4] + tmp[6] * src[5] + tmp[11] * src[7];
		dst[3] = tmp[5] * src[4] + tmp[8] * src[5] + tmp[11] * src[6];
		dst[3] -= tmp[4] * src[4] + tmp[9] * src[5] + tmp[10] * src[6];
		dst[4] = tmp[1] * src[1] + tmp[2] * src[2] + tmp[5] * src[3];
		dst[4] -= tmp[0] * src[1] + tmp[3] * src[2] + tmp[4] * src[3];
		dst[5] = tmp[0] * src[0] + tmp[7] * src[2] + tmp[8] * src[3];
		dst[5] -= tmp[1] * src[0] + tmp[6] * src[2] + tmp[9] * src[3];
		dst[6] = tmp[3] * src[0] + tmp[6] * src[1] + tmp[11] * src[3];
		dst[6] -= tmp[2] * src[0] + tmp[7] * src[1] + tmp[10] * src[3];
		dst[7] = tmp[4] * src[0] + tmp[9] * src[1] + tmp[10] * src[2];
		dst[7] -= tmp[5] * src[0] + tmp[8] * src[1] + tmp[11] * src[2];
		
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
		dst[8] = tmp[0] * src[13] + tmp[3] * src[14] + tmp[4] * src[15];
		dst[8] -= tmp[1] * src[13] + tmp[2] * src[14] + tmp[5] * src[15];
		dst[9] = tmp[1] * src[12] + tmp[6] * src[14] + tmp[9] * src[15];
		dst[9] -= tmp[0] * src[12] + tmp[7] * src[14] + tmp[8] * src[15];
		dst[10] = tmp[2] * src[12] + tmp[7] * src[13] + tmp[10] * src[15];
		dst[10] -= tmp[3] * src[12] + tmp[6] * src[13] + tmp[11] * src[15];
		dst[11] = tmp[5] * src[12] + tmp[8] * src[13] + tmp[11] * src[14];
		dst[11] -= tmp[4] * src[12] + tmp[9] * src[13] + tmp[10] * src[14];
		dst[12] = tmp[2] * src[10] + tmp[5] * src[11] + tmp[1] * src[9];
		dst[12] -= tmp[4] * src[11] + tmp[0] * src[9] + tmp[3] * src[10];
		dst[13] = tmp[8] * src[11] + tmp[0] * src[8] + tmp[7] * src[10];
		dst[13] -= tmp[6] * src[10] + tmp[9] * src[11] + tmp[1] * src[8];
		dst[14] = tmp[6] * src[9] + tmp[11] * src[11] + tmp[3] * src[8];
		dst[14] -= tmp[10] * src[11] + tmp[2] * src[8] + tmp[7] * src[9];
		dst[15] = tmp[10] * src[10] + tmp[4] * src[8] + tmp[9] * src[9];
		dst[15] -= tmp[8] * src[9] + tmp[11] * src[10] + tmp[5] * src[8];
		
		// Calculate determinant
		double det = src[0] * dst[0] + src[1] * dst[1] + src[2] * dst[2] + src[3] * dst[3];
		
		if (det == 0)
		{
			throw new IllegalArgumentException("The determinante is singular!");
		}
		
		// new instance:
		double[] array = new double[16];
		
		// Calculate matrix inverse
		det = 1.0 / det;
		
		int j = -1;
		for (i = 0; i < 4; i++)
		{
			for (j = 0; j < 4; j++)
			{
				array[i * m_coles + j] = dst[i * 4 + j] * det;
			}
		}
		m_data = array;
		return this;
	}
	

	/**
	 * A*X = B
	 * 
	 * @param B
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return
	 */
	public Matrix solve_LR(Matrix B)
	{
		if (this.getRowDimension() != B.getRowDimension())
		{
			throw new IllegalArgumentException("Only matrices with same row-number are possible as input");
		}
		
		if (m_rows != m_coles)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		// allocate space for the result X
		Matrix A = new Matrix(m_rows);
		Matrix X = new Matrix(A.getColumnDimension(), B.getColumnDimension());
		int cols = X.getColumnDimension();
		
		// set the column vector b
		double[] b = new double[m_rows];
		double[] x = new double[m_rows];
		
		int i = -1;
		
		for (int j_result = 0; j_result < cols; j_result++)
		{
			// fill the column vector from the current column
			for (i = 0; i < m_rows; i++)
			{
				b[i] = B.get(i, j_result);
				
			}
			
			this.copyThisArrayDataTo(A);
			A.solve_LROneVector(b, x);
			
			// fill the column vector from the current column
			for (i = 0; i < m_rows; i++)
			{
				X.set(i, j_result, x[i]);
			}
		}
		return X;
	}
	

	/**
	 * 
	 * A*x = b
	 * 
	 * @param b
	 * @param changeThisMemory boolean, to decide weather the current matrix will
	 *           be changed or a new matrix will be filled with the result
	 *           true: A' = A.op(B) or false: C = A.op(B)
	 * @return x
	 */
	private double[] solve_LROneVector(double[] b, double[] x)
	{
		this.partition_LR(b);
		
		int i = -1;
		int j = -1;
		
		for (j = m_rows - 1; j >= 0; j--)
		{
			double sum = b[j];
			for (i = j + 1; i <= m_rows - 1; i++)
			{
				sum = sum - m_data[j * m_rows + i] * x[i];
			}
			x[j] = sum / m_data[j * m_rows + j];
		}
		return x;
	}
	

	/**
	 * A=L*R; b=y
	 * 
	 * @param b
	 * @return
	 */
	private Matrix partition_LR(double[] b)
	{
		int i = -1;
		int j = -1;
		int k = -1;
		// calculate normalized main-diagonal
		double[] diagonal = new double[m_rows];
		
		for (j = 0; j < m_rows - 1; j++)
		{
			changeCurrRowWithBestNumericFit(b, j, diagonal);
			if (m_data[j * m_coles + j] == 0.0)
			{
				throw new IllegalArgumentException("Only Matrices with main-diagonal without zeros can be calculated");
			}
			for (i = j + 1; i <= m_rows - 1; i++)
			{
				m_tmp = i * m_coles;
				m_data[m_tmp + j] = m_data[m_tmp + j] / m_data[j * m_coles + j];// entspricht L[i][j]
				
				b[i] = b[i] - b[j] * m_data[m_tmp + j];
				for (k = j + 1; k <= m_rows - 1; k++)
				{
					m_data[m_tmp + k] = m_data[m_tmp + k] - m_data[j * m_coles + k] * m_data[m_tmp + j];
				}
			}
		}
		return this;
	}
	

	/**
	 * 
	 * change the rows by the current best numeric fit
	 * 'zeilenvertauschung mit spaltenpivotisierung'
	 * 
	 * @param b
	 * @param curDepth
	 */
	private void changeCurrRowWithBestNumericFit(double[] b, int curDepth, double[] diagonal)
	{
		int i = -1;
		int k = -1;
		for (i = curDepth; i < m_rows; i++)
		{
			m_tmp = i * m_coles;
			// calculate "Zeilennorm der iten Zeile"
			double sum = 0;
			for (k = curDepth; k < m_coles; k++)
			{
				sum += Math.pow(m_data[m_tmp + k], 2);
			}
			// fill diagonal.vector
			diagonal[i] = 1.0 / Math.sqrt(sum);
		}
		

		// calculate highest element-index
		double maxVal = -1;
		int id = -1;
		// go through column
		for (k = curDepth; k < m_rows; k++)
		{
			double val = diagonal[k] * Math.abs(m_data[k * m_coles + curDepth]);
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
		double[] tmp_arr = new double[m_rows];
		double tmp_val = -1;
		
		// tmp = a
		m_tmp = curDepth * m_coles;
		for (int l = 0; l < m_rows; l++)
		{
			tmp_arr[l] = m_data[m_tmp + l];
			
		}
		tmp_val = b[curDepth];
		
		// a = b
		for (int l = 0; l < m_rows; l++)
		{
			m_data[m_tmp + l] = m_data[id * m_coles + l];
		}
		b[curDepth] = b[id];
		
		// b = tmp
		for (int l = 0; l < m_rows; l++)
		{
			m_data[id * m_coles + l] = tmp_arr[l];
		}
		b[id] = tmp_val;
	}
	

	/**
	 * A*X = B
	 * 
	 * @param B
	 * @return
	 */
	public Matrix solve_Cholesky(Matrix B)
	{
		if (this.getRowDimension() != B.getRowDimension())
		{
			throw new IllegalArgumentException("Only matrices with same row-number are possible as input");
		}
		
		if (m_rows != m_coles)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		// generate C*Ct
		Matrix C_Ct = this.copy();
		C_Ct.partition_Cholesky();
		
		// allocate space for the result X
		Matrix X = new Matrix(C_Ct.getColumnDimension(), B.getColumnDimension());
		int x_coles = X.getColumnDimension();
		
		// set the column vector b
		double[] b = new double[m_rows];
		// allocate some space for a temporary vector
		double[] y = new double[m_rows];
		// allocate space for new matrix
		double[] x = new double[m_rows];
		
		for (int j_result = 0; j_result < x_coles; j_result++)
		{
			// fill the column vector from the current column
			for (int i = 0; i < m_rows; i++)
			{
				b[i] = B.get(i, j_result);
			}
			C_Ct.solveCholOneVector(b, x, y);
			// fill the column vector from the current column
			for (int i = 0; i < m_rows; i++)
			{
				X.set(i, j_result, x[i]);
			}
		}
		return X;
	}
	

	private void solveCholOneVector(double[] b, double[] x, double[] y)
	{
		
		// Ct*y=b
		for (int i = 0; i <= m_rows - 1; i++)
		{
			double sum = b[i];
			for (int j = 0; j < i; j++)
			{
				sum -= y[j] * m_data[j * m_rows + i];
			}
			y[i] = sum / m_data[i * m_rows + i];
		}
		
		// C*x=y
		for (int i = m_rows - 1; i >= 0; i--)
		{
			double sum = y[i];
			for (int j = m_rows - 1; j > i; j--)
			{
				sum -= x[j] * m_data[i * m_coles + j];
			}
			x[i] = sum / m_data[i * m_coles + i];
		}
	}
	

	/**
	 * 
	 * A*x = b
	 * 
	 * @param b
	 * @return x
	 */
	public double[] solve_Cholesky(double[] b)
	{
		if (m_rows != m_coles)
		{
			throw new IllegalArgumentException("Only sqaured matrices can be solved");
		}
		
		Matrix C = this.copy();
		C.partition_Cholesky();
		double[] data = C.getArray();
		
		// allocate some space for the result-vector
		double[] x = new double[m_rows];
		
		// allocate some space for a temporary vector
		double[] y = new double[m_rows];
		
		// CT*y=b
		for (int j = 0; j <= m_rows - 1; j++)
		{
			double sum = b[j];
			for (int k = 0; k <= j; k++)
			{
				sum -= y[k] * data[k * m_rows + j];
			}
			y[j] = sum / data[j * m_rows + j];
		}
		
		// C*x=y
		for (int j = m_rows - 1; j >= 0; j--)
		{
			double sum = y[j];
			for (int k = m_rows - 1; k >= j; k--)
			{
				sum -= x[k] * data[j * m_coles + k];
			}
			x[j] = sum / data[j * m_coles + j];
		}
		return x;
	}
	

	/**
	 * A = C*Ct
	 * 
	 * @param b
	 * @return C*Ct
	 */
	private Matrix partition_Cholesky()
	{
		
		// fill the lines with values
		for (int j = 0; j < m_rows; j++)
		{
			for (int k = 0; k < m_rows; k++)
			{
				if (j > k)
				{
					m_data[j * m_coles + k] = 0.0;
				} else if (j == k)
				{
					double sum = m_data[j * m_coles + j];
					for (int i = 0; i < j; i++)
					{
						sum -= Math.pow(m_data[i * m_coles + j], 2);
					}
					m_data[j * m_coles + j] = Math.sqrt(sum);
				} else
				{
					double sum = m_data[j * m_coles + k];
					for (int i = 0; i < j; i++)
					{
						sum -= m_data[i * m_coles + j] * m_data[i * m_coles + k];
					}
					m_data[j * m_rows + k] = sum / m_data[j * m_rows + j];
				}
			}
		}
		return this;
	}
	

	// --------------------------------------------------------------------------
	// --- getters and setters --------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int getRowDimension()
	{
		return m_rows;
	}
	

	public int getColumnDimension()
	{
		return m_coles;
	}
	

	public double get(int row, int col)
	{
		checkRowAndColumnAccess(row, col);
		return m_data[row * m_coles + col];
	}
	
	//without check for access in correct area
	public double at(int row, int col)
	{
		return m_data[row * m_coles + col];
	}
	

	public void set(int row, int col, double value)
	{
		checkRowAndColumnAccess(row, col);
		m_data[row * m_coles + col] = value;
	}
	

	// --------------------------------------------------------------------------
	// --- help-functions -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private final void checkRowAndColumnCreateDimension(int row, int col)
	{
		if (row < 1 || col < 1)
		{
			throw new IllegalArgumentException("The Row and Column has to be at least 1");
		}
	}
	

	private final void checkRowAndColumnAccess(int row, int col)
	{
		
		if (row > m_rows || col > m_coles || row < 0 || col < 0)
		{
			throw new IllegalArgumentException("You cant acces Row/column: " + row + "/" + col);
		}
	}
	

	private final void checkArrayLength(double[][] data, int rows, int coles)
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
	

	private final void checkArrayLength(double[] data, int rows, int coles)
	{
		// check whole size
		if (rows * coles != data.length)
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
	public String toString()
	{
		String str = "";
		
		int i = -1;
		int j = -1;
		// for all rows
		for (i = 0; i < m_rows; i++)
		{
			for (j = 0; j < m_coles; j++)
			{
				str += "[";
				str += String.format("% 10.4e ", m_data[i * m_coles + j]);
				str += "]";
			}
			str += "\n";
		}
		return str;
	}
	

	/**
	 * Function, to copy the content
	 * 
	 * @return
	 */
	private void copyThisArrayDataTo(Matrix A)
	{
		double[] data = A.getArray();
		for (int i = 0; i < m_rows; i++)
		{
			m_tmp = i * m_coles;
			for (int j = 0; j < m_coles; j++)
			{
				data[m_tmp + j] = m_data[m_tmp + j];
			}
		}
	}
	

}
