/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance;


import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This class provides abstract functions, to test to performance of the matrix-class
 * @author Birgit
 * 
 */
public abstract class APerformanceTest
{
	private final Matrix[]			memory			= new Matrix[4];
	private final Jama.Matrix[]	JamaMemory		= new Jama.Matrix[4];
	
	protected long						startMillis;
	protected long						endMillis;
	
	protected long						breakStartMillis;
	protected long						breakEndMillis;
	
	protected boolean					pause				= false;
	
	protected boolean					output			= false;
	
	protected int						timesInit		= 500000000;
	protected int						timesEasyOps	= 100000000;
	
	
	// protected int timesInit = 10;
	// protected int timesEasyOps = 10;
	
	/**
	 *
	 */
	public void startTimer()
	{
		startMillis = SumatraClock.currentTimeMillis();
	}
	
	
	/**
	 *
	 */
	public void endTimer()
	{
		endMillis = SumatraClock.currentTimeMillis();
		final long diff = endMillis - startMillis;
		if (output)
		{
			System.out.println("Time: " + diff);
		}
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	public Jama.Matrix createRandomMatrixJaMa(int size)
	{
		final long fctStart = SumatraClock.currentTimeMillis();
		
		final Jama.Matrix A = new Jama.Matrix(createRandomArray(size, false));
		
		final long fctStop = SumatraClock.currentTimeMillis();
		final long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	public Matrix createRandomCheckMatrix(int size)
	{
		final long fctStart = SumatraClock.currentTimeMillis();
		
		final Matrix A = new Matrix(createRandomArray(size, false));
		
		final long fctStop = SumatraClock.currentTimeMillis();
		final long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	
	
	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 * @param size
	 * @return
	 */
	public Matrix createRandomMatrix(int size)
	{
		final long fctStart = SumatraClock.currentTimeMillis();
		
		final Matrix A = new Matrix(createRandomArray(size, false));
		
		final long fctStop = SumatraClock.currentTimeMillis();
		final long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	
	
	/**
	 * 
	 * @param size
	 * @param stopTime
	 * @return
	 */
	public double[][] createRandomArray(int size, boolean stopTime)
	{
		long fctStart = 0;
		if (stopTime)
		{
			fctStart = SumatraClock.currentTimeMillis();
		}
		
		final double[][] array = new double[size][size];
		
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				array[i][j] = Math.random();
			}
		}
		
		if (stopTime)
		{
			final long fctStop = SumatraClock.currentTimeMillis();
			final long diff = fctStop - fctStart;
			
			startMillis += diff;
		}
		return array;
	}
	
	
	/**
	 * 
	 * @param M
	 * @return
	 */
	public String toString(Jama.Matrix M)
	{
		String str = "";
		
		// for all rows
		for (int i = 0; i < M.getRowDimension(); i++)
		{
			for (int j = 0; j < M.getColumnDimension(); j++)
			{
				// str += "[";
				// str += m_data[i][j];
				// str += String.format("%010.2f", m_data[i][j]);
				// str += "]\t";
				// System.out.print(" ");
				// System.out.print(String.format("%6.2f", c) + "\t")<<<e
				str += "[";
				str += String.format("% 10.4e ", M.get(i, j));
				str += "]";
			}
			str += "\n";
		}
		
		return str;
	}
	
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	public String toString(double[] a)
	{
		String str = "";
		for (final double element : a)
		{
			str += "[" + element + "]";
		}
		return str;
	}
	
	
	/**
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	public boolean equals(Matrix A, Matrix B)
	{
		if (A.getRowDimension() != B.getRowDimension())
		{
			return false;
		}
		
		if (A.getColumnDimension() != B.getColumnDimension())
		{
			return false;
		}
		
		for (int i = 0; i < A.getRowDimension(); i++)
		{
			for (int j = 0; j < A.getColumnDimension(); j++)
			{
				if (A.get(i, j) != B.get(i, j))
				{
					// pruefe auf 15 stellen genau
					final double e = 10E-12;
					final double a = A.get(i, j);
					final double b = B.get(i, j);
					if (((a + e) > b) && ((a - e) < b))
					{
						// System.out.println("Nur auf 12 Stellen genau");
					} else
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param A
	 * @param JB
	 * @return
	 */
	public boolean equals(Matrix A, Jama.Matrix JB)
	{
		final Matrix B = new Matrix(JB.getArray());
		return equals(A, B);
	}
	
	
	/**
	 * 
	 * @param JA
	 * @param B
	 * @return
	 */
	public boolean equals(Jama.Matrix JA, Matrix B)
	{
		final Matrix A = new Matrix(JA.getArray());
		return equals(A, B);
	}
	
	
	/**
	 * 
	 * @param JA
	 * @param JB
	 * @return
	 */
	public boolean equals(Jama.Matrix JA, Jama.Matrix JB)
	{
		final Matrix A = new Matrix(JA.getArray());
		final Matrix B = new Matrix(JB.getArray());
		return equals(A, B);
	}
	
	
	/**
	 *
	 */
	public void breakTimer()
	{
		if (pause)
		{
			try
			{
				throw new Exception("We have already break");
			} catch (final Exception err)
			{
				err.printStackTrace();
			}
		}
		pause = true;
		breakStartMillis = SumatraClock.currentTimeMillis();
	}
	
	
	/**
	 *
	 */
	public void restartTimer()
	{
		if (!pause)
		{
			try
			{
				throw new Exception("We don't have break");
			} catch (final Exception err)
			{
				err.printStackTrace();
			}
		}
		pause = false;
		breakEndMillis = SumatraClock.currentTimeMillis();
		
		final long diff = breakEndMillis - breakStartMillis;
		startMillis += diff;
	}
	
	
	/**
	 * 
	 * stop automatically the time
	 * @param A
	 */
	public void saveMatrix(Matrix A)
	{
		breakTimer();
		memory[(int) (startMillis % 4)] = A;
		memory[(int) (SumatraClock.currentTimeMillis() % 4)] = memory[0];
		restartTimer();
	}
	
	
	/**
	 * 
	 * stop automatically the time
	 * @param A
	 */
	public void saveJamaMatrix(Jama.Matrix A)
	{
		breakTimer();
		JamaMemory[(int) (startMillis % 4)] = A;
		JamaMemory[(int) (SumatraClock.currentTimeMillis() % 4)] = JamaMemory[0];
		restartTimer();
	}
}
