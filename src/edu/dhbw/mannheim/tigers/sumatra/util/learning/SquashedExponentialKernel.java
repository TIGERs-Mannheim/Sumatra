/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2015
 * Author(s): Jannik Abbenseth <jannik.abbenseth@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import java.util.List;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.PolicyParameters.DimensionParams;


/**
 * TODO Jannik Abbenseth <jannik.abbenseth@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Jannik Abbenseth <jannik.abbenseth@gmail.com>
 */
public class SquashedExponentialKernel extends ExponentialQuadraticKernel
{
	private List<Float>	squashingParams;
	
	
	/**
	 * @param list
	 */
	public SquashedExponentialKernel(final List<Float> list)
	{
		super();
		squashingParams = list;
	}
	
	
	private Matrix squash(final Matrix a)
	{
		Matrix a1 = a.copy();
		for (int i = 0; i < a1.getRowDimension(); i++)
		{
			for (int j = 0; j < (a1.getColumnDimension() / 2); j++)
			{
				a1.set(i, j, squashingParams.get(0) * Math.tanh(squashingParams.get(1) * a1.get(i, j)));
			}
		}
		return a1;
	}
	
	
	@Override
	public double kernelFn(final DimensionParams dp, final Matrix a, final Matrix b)
	{
		Matrix a1 = squash(a);
		Matrix b1 = squash(b);
		
		return super.kernelFn(dp, a1, b1);
	}
}
