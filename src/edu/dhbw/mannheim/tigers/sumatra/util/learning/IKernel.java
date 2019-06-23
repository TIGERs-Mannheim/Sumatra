/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.01.2015
 * Author(s): Jannik Abbenseth <jannik.abbenseth@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.PolicyParameters.DimensionParams;


/**
 * @author Jannik Abbenseth <jannik.abbenseth@gmail.com>
 */
public interface IKernel
{
	/**
	 * @param dp Hyperparameters for policy
	 * @param a state a
	 * @param b state b
	 * @return
	 */
	public double kernelFn(final DimensionParams dp, final Matrix a, final Matrix b);
	
	
	/**
	 * @param dp
	 * @param policy
	 */
	public void computeRegularizedGramMatrix(DimensionParams dp, SinglePolicy policy);
	
}
