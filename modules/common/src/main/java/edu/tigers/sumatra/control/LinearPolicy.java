/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 10, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.control;

import java.util.Arrays;

import Jama.Matrix;


/**
 * Test policy to test state composition
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LinearPolicy implements IPolicyController
{
	
	private final int	stateDimension	= 6;
												
	private Double[]	pid_p				= { 3.5, 3.5, 6.0 };
	private Double[]	pid_d				= { 0.4, 0.4, 1.0 };
												
												
	/**
	 * @param pid_p
	 * @param pid_d
	 */
	public LinearPolicy(final Double[] pid_p, final Double[] pid_d)
	{
		this.pid_p = Arrays.copyOf(pid_p, 3);
		this.pid_d = Arrays.copyOf(pid_d, 3);
	}
	
	
	@Override
	public Matrix getControl(final Matrix state)
	{
		assert state.getRowDimension() == 1;
		
		
		Matrix u = new Matrix(1, state.getColumnDimension() / 2);
		for (int a = 0; a < (state.getColumnDimension() / 2.0); a++)
		{
			double action = (pid_p[a] * state.get(0, a)) - (pid_d[a] * state.get(0, (state.getColumnDimension() / 2) + a));
			u.set(0, a, action);
		}
		
		return u;
	}
	
	
	@Override
	public int getStateDimension()
	{
		return stateDimension;
	}
	
	
	@Override
	public double getDt()
	{
		return 0.1;
	}
	
}
