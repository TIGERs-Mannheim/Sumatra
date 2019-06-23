/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math;

import java.util.function.Function;

import Jama.Matrix;


/**
 * Supplies discrete kernels and methods for application to (two dimensional) arrays
 * 
 * @author JulianT
 */
public class KernelMath
{
	
	protected static Function<Double, Double> gauss = x -> Math.exp(-Math.pow(x, 2) / 2.0)
			/ SumatraMath.sqrt(2 * Math.PI);
	
	/**
	 * Enum to select kernel type
	 * 
	 * @author JulianT
	 */
	public enum EKernelFunction
	{
		/**  */
		GAUSS(gauss);
		
		private final Function<Double, Double>	function;
		
		
		EKernelFunction(final Function<Double, Double> function)
		{
			this.function = function;
		}
		
		
		/**
		 * @return The selected function
		 */
		public Function<Double, Double> getFunction()
		{
			return function;
		}
	}
	
	
	/**
	 * Create a new square kernel
	 * 
	 * @param kernelFunction Kernel function
	 * @param size Number of elements per row/column
	 * @param dx Distance between two elements in a row
	 * @param dy Distance between two elements in a column
	 * @return Matrix representing the kernel
	 */
	public static Matrix createSquareKernel(final EKernelFunction kernelFunction, final int size,
			final double dx, final double dy)
	{
		if ((size % 2) != 1)
		{
			return null;
		}
		
		Matrix kernel = new Matrix(size, size);
		int center = size / 2;
		
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				double d = SumatraMath.sqrt(Math.pow(dx * (center - i), 2) + Math.pow(dy * (center - j), 2));
				kernel.set(i, j, kernelFunction.getFunction().apply(d));
			}
		}
		
		return kernel;
	}
	
	
	/**
	 * Applies a kernel to a Matrix
	 * 
	 * @param kernel
	 * @param mat
	 * @param numX
	 * @param numY
	 * @return
	 */
	public static double[] filter(final Matrix kernel, final long[] mat, final int numX, final int numY)
	{
		double[] result = new double[numX * numY];
		
		for (int x = 0; x < numX; x++)
		{
			for (int y = 0; y < numY; y++)
			{
				result[x + (y * numX)] = 0;
				for (int i = 0; i < kernel.getRowDimension(); i++)
				{
					if ((((x + i) - (kernel.getRowDimension() / 2.0)) >= 0)
							&& (((x + i) - (kernel.getRowDimension() / 2.0)) < numX))
					{
						for (int j = 0; j < kernel.getColumnDimension(); j++)
						{
							if ((((y + j) - (kernel.getColumnDimension() / 2.0)) >= 0)
									&& (((y + j) - (kernel.getColumnDimension() / 2.0)) < numY))
							{
								result[x + (y * numX)] += mat[x + (i - (kernel.getRowDimension() / 2))
										+ (((y + j) - (kernel.getColumnDimension() / 2)) * numX)] + kernel.get(i, j);
							}
						}
					}
				}
				
				result[x + (y * numX)] /= (kernel.getColumnDimension() * kernel.getRowDimension());
			}
		}
		
		return result;
	}
}
