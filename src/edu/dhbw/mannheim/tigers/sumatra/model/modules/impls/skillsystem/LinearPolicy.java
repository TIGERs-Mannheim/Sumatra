/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 10, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.IPolicyController;


/**
 * Test policy to test state composition
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LinearPolicy implements IPolicyController
{
	private int					stateDimension	= 6;
	
	@Configurable(spezis = { "GRSIM", "SUMATRA", "" }, defValue = "3.5;3.5;6")
	private static Float[]	pid_p				= { 3.5f, 3.5f, 6f };
	@Configurable(spezis = { "GRSIM", "SUMATRA", "" }, defValue = "0.4;0.4;1")
	private static Float[]	pid_d				= { 0.4f, 0.4f, 1f };
	
	
	/**
	 * 
	 */
	public LinearPolicy()
	{
	}
	
	
	@Override
	public Matrix getControl(final Matrix state)
	{
		// assert state.getColumnDimension() == stateDimension;
		assert state.getRowDimension() == 1;
		
		
		Matrix u = new Matrix(1, state.getColumnDimension() / 2);
		for (int a = 0; a < (state.getColumnDimension() / 2); a++)
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
	public float getDt()
	{
		return 0.1f;
	}
	
}
