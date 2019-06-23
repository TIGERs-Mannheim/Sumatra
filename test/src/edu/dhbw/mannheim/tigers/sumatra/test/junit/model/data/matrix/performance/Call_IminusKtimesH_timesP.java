/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 10, 2010
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;


/**
 * TODO Birgit, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author Birgit
 * 
 */
public class Call_IminusKtimesH_timesP extends APerformanceTest
{
	@Test
	public void test()
	{
		System.out.println("##########################################################");
		System.out.println("IminusKtimesH_timesP");
		System.out.println("##########################################################");
		
		output = false;
		int times = 10;
		Jama(times);
		Matrix(times);
		

		output = true;
		times = timesEasyOps;
		System.out.println("Number of runs: " + times);
		Jama(times);
		Matrix(times);
	}
	

	public void Matrix(int times)
	{
		if (output)
			System.out.println("MatrixNew");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			Matrix I = Matrix.Identity(2, 2);
			Matrix H = new Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			Matrix P = new Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			Matrix K = new Matrix(new double[][] { { 0, 5 }, { 3, Math.random() } });
			
			Matrix X =  (I.minus(K.times(H))).times(P);
			saveMatrix(X);
		}
		endTimer();
	}
	

	public void Jama(int times)
	{
		if (output)
			System.out.println("JaMa");
		startTimer();
		for (int i = 0; i < times; i++)
		{
			// set a random value
			Jama.Matrix I = Jama.Matrix.identity(2, 2);
			Jama.Matrix H = new Jama.Matrix(new double[][] { { Math.random(), 2 }, { 3, 4 } });
			Jama.Matrix P = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, Math.random() } });
			Jama.Matrix K = new Jama.Matrix(new double[][] { { 0, 5 }, { 3, Math.random() } });
			
			Jama.Matrix X = (I.minus(K.times(H))).times(P);
			saveJamaMatrix(X);
		}
		endTimer();
	}
	
}
