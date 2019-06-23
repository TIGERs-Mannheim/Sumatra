/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.05.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import java.util.function.Function;

import Jama.Matrix;


/**
 * Supplies discrete kernels and methods for application to (two dimensional) arrays
 * 
 * @author JulianT
 */
public class KernelMath
{
	
	/**
	 * Enum to select kernel type
	 * 
	 * @author JulianT
	 */
	public enum EKernelFunction
	{
		/**  */
		GAUSS(gauss);
		
		private Function<Float, Float>	function;
		
		
		private EKernelFunction(final Function<Float, Float> function)
		{
			this.function = function;
		}
		
		
		/**
		 * @return The selected function
		 */
		public Function<Float, Float> getFunction()
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
			final float dx, final float dy)
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
				float d = (float) Math.sqrt(Math.pow(dx * (center - i), 2) + Math.pow(dy * (center - j), 2));
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
	public static float[] filter(final Matrix kernel, final long[] mat, final int numX, final int numY)
	{
		float result[] = new float[numX * numY];
		
		for (int x = 0; x < numX; x++)
		{
			for (int y = 0; y < numY; y++)
			{
				result[x + (y * numX)] = 0;
				for (int i = 0; i < kernel.getRowDimension(); i++)
				{
					if ((((x + i) - (kernel.getRowDimension() / 2)) >= 0)
							&& (((x + i) - (kernel.getRowDimension() / 2)) < numX))
					{
						for (int j = 0; j < kernel.getColumnDimension(); j++)
						{
							if ((((y + j) - (kernel.getColumnDimension() / 2)) >= 0)
									&& (((y + j) - (kernel.getColumnDimension() / 2)) < numY))
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
	
	protected static Function<Float, Float>	gauss	= x -> new Float(Math.exp(-Math.pow(x, 2) / 2)
																			/ Math.sqrt(2 * Math.PI));
}
