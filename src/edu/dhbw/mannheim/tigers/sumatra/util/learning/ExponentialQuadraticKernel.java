/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2015
 * Author(s): Jannik Abbenseth <jannik.abbenseth@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.learning;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.PolicyParameters.DimensionParams;


/**
 * @author Jannik Abbenseth <jannik.abbenseth@gmail.com>
 */
public class ExponentialQuadraticKernel implements IKernel
{
	private static final Logger	log	= Logger.getLogger(GaussianPolicy.class.getName());
	
	
	@Override
	public double kernelFn(final DimensionParams dp, final Matrix a, final Matrix b)
	{
		double[][] q = new double[dp.getBandwidth().size()][dp.getBandwidth().size()];
		for (int i = 0; i < dp.getBandwidth().size(); i++)
		{
			q[i][i] = 1 / (dp.getBandwidth().get(i) * dp.getBandwidth().get(i));
		}
		Matrix q_mat = new Matrix(q);
		Matrix c = a.minus(b);
		Matrix pow = (c.times(q_mat)).times(c.transpose()).times(-0.5);
		assert (pow.getRowDimension() == 1) && (pow.getColumnDimension() == 1);
		
		return dp.getScale() * Math.exp(pow.get(0, 0));
	}
	
	
	/**
	 * @param dp Hyperparameters for policy
	 * @param a states a
	 * @param b states b
	 * @return gramMatrix
	 */
	public Matrix getGramMatrix(final DimensionParams dp, final Matrix a, final Matrix b)
	{
		double[][] g = new double[a.getRowDimension()][a.getRowDimension()];
		for (int i = 0; i < a.getRowDimension(); i++)
		{
			for (int j = 0; j < a.getRowDimension(); j++)
			{
				
				g[i][j] = kernelFn(dp, a.getMatrix(i, i, 0, a.getColumnDimension()),
						b.getMatrix(j, j, 0, b.getColumnDimension()));
				
			}
		}
		return new Matrix(g);
	}
	
	
	/**
	 * @param dp Hyperparameters for policy
	 * @param policy
	 */
	@Override
	public void computeRegularizedGramMatrix(final DimensionParams dp, final SinglePolicy policy)
	{
		int rows = dp.getStates().length;
		int cols = dp.getStates()[0].length;
		
		double[][] g = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
		{
			for (int j = i; j < rows; j++)
			{
				
				g[i][j] = kernelFn(dp, policy.stateMat.getMatrix(i, i, 0, cols - 1),
						policy.stateMat.getMatrix(j, j, 0, cols - 1));
				
				g[j][i] = g[i][j];
			}
		}
		
		double[][] invWeights = new double[dp.getWeights().length][dp.getWeights()[0].length];
		for (int i = 0; i < dp.getWeights().length; i++)
		{
			for (int j = 0; j < dp.getWeights()[0].length; j++)
			{
				invWeights[i][j] = 1 / dp.getWeights()[i][j];
				if (!Double.isFinite(invWeights[i][j]))
				{
					log.warn("non finite invWeight.");
					invWeights[i][j] = 0;
				}
			}
		}
		Matrix reg = Matrix.identity(rows, rows);
		for (int r = 0; r < rows; r++)
		{
			reg.set(r, r, invWeights[r][0]);
		}
		reg.timesEquals(dp.getLambda());
		Matrix g_mat = new Matrix(g);
		g_mat.plusEquals(reg);
		
		policy.regularizedGramMatrix = g_mat;
	}
	
}
