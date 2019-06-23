/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 18, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;


import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * This class provides abstract functions, to test to performance of the matrix-class
 * @author Birgit
 * 
 */
public class APerformanceTest
{
	private Matrix[]			memory			= new Matrix[4];
	private Jama.Matrix[]	JamaMemory		= new Jama.Matrix[4];
	
	protected long				startMillis;
	protected long				endMillis;
	
	protected long				breakStartMillis;
	protected long				breakEndMillis;
	
	protected boolean			pause				= false;
	
	protected boolean			output			= false;
	
	protected int timesInit = 500000000;
	protected int timesEasyOps = 100000000;
	
	//protected int				timesInit		= 10;
	//protected int				timesEasyOps	= 10;
	
	
	public void startTimer()
	{
		startMillis = System.currentTimeMillis();
	}
	

	public void endTimer()
	{
		endMillis = System.currentTimeMillis();
		long diff = endMillis - startMillis;
		if (output)
		{
			System.out.println("Time: " + diff);
		}
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	
	public Jama.Matrix createRandomMatrixJaMa(int size)
	{
		long fctStart = System.currentTimeMillis();
		
		Jama.Matrix A = new Jama.Matrix(createRandomArray(size, false));
		
		long fctStop = System.currentTimeMillis();
		long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	public Matrix createRandomCheckMatrix(int size)
	{
		long fctStart = System.currentTimeMillis();
		
		Matrix A = new Matrix(createRandomArray(size, false));
		
		long fctStop = System.currentTimeMillis();
		long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	

	/**
	 * create a squared matrix with randomNumbers
	 * while this function is running, the time is waiting
	 */
	public Matrix createRandomMatrix(int size)
	{
		long fctStart = System.currentTimeMillis();
		
		Matrix A = new Matrix(createRandomArray(size, false));
		
		long fctStop = System.currentTimeMillis();
		long diff = fctStop - fctStart;
		
		startMillis += diff;
		return A;
	}
	

	public double[][] createRandomArray(int size, boolean stopTime)
	{
		long fctStart = 0;
		if (stopTime)
		{
			fctStart = System.currentTimeMillis();
		}
		
		double[][] array = new double[size][size];
		
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				array[i][j] = Math.random();
			}
		}
		
		if (stopTime)
		{
			long fctStop = System.currentTimeMillis();
			long diff = fctStop - fctStart;
			
			startMillis += diff;
		}
		return array;
	}
	

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
	

	public String toString(double[] a)
	{
		String str = "";
		for (int i = 0; i < a.length; i++)
		{
			str += "[" + a[i] + "]";
		}
		return str;
	}
	

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
					double e = 10E-12;
					double a = A.get(i, j);
					double b = B.get(i, j);
					if (a + e > b && a - e < b)
					{
						//System.out.println("Nur auf 12 Stellen genau");
					}
					else
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	

	public boolean equals( Matrix A,Jama.Matrix JB)
	{
		Matrix B = new Matrix(JB.getArray());
		return equals(A,B);
	}
	

	public boolean equals(Jama.Matrix JA, Matrix B)
	{
		Matrix A = new Matrix(JA.getArray());
		return equals(A,B);
	}
	

	public boolean equals(Jama.Matrix JA, Jama.Matrix JB)
	{
		Matrix A = new Matrix(JA.getArray());
		Matrix B = new Matrix(JB.getArray());
		return equals(A,B);
	}
	

	public void breakTimer()
	{
		if (pause)
		{
			try
			{
				throw new Exception("We have already break");
			} catch (Exception err)
			{
				err.printStackTrace();
			}
		}
		pause = true;
		breakStartMillis = System.currentTimeMillis();
	}
	

	public void restartTimer()
	{
		if (!pause)
		{
			try
			{
				throw new Exception("We don't have break");
			} catch (Exception err)
			{
				err.printStackTrace();
			}
		}
		pause = false;
		breakEndMillis = System.currentTimeMillis();
		
		long diff = breakEndMillis - breakStartMillis;
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
		memory[(int) startMillis % 4] = A;
		memory[(int) System.currentTimeMillis() % 4] = memory[0];
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
		JamaMemory[(int) startMillis % 4] = A;
		JamaMemory[(int) System.currentTimeMillis() % 4] = JamaMemory[0];
		restartTimer();
	}
}
