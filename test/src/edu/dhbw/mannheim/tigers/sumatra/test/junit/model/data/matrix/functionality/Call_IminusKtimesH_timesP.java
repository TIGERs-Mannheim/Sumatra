/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 10, 2010
 * Author(s): Birgit
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.functionality;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;


/**
 * test: (I-K*H)*P
 * @author Birgit
 * 
 */
public class Call_IminusKtimesH_timesP extends APerformanceTest
{
	@Test
	public void call_Matrix()
	{
		Matrix IInit = Matrix.Identity(2, 2);
		Matrix HInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, -4 } });
		Matrix KInit = new Matrix(new double[][] { { 0, 5 }, { 3, -5 } });
		Matrix XInit = new Matrix(new double[][] { { -20, 66 }, { 15, -48 } });
		// init
		Matrix I = IInit.copy();
		Matrix H = HInit.copy();
		Matrix P = PInit.copy();
		Matrix K = KInit.copy();
	
		// call with new instances
		Matrix X = (I.minus(K.times(H))).times(P); 
		
		// test consistency
		assertTrue(equals(I,IInit));
		assertTrue(equals(H,HInit));
		assertTrue(equals(P,PInit));
		assertTrue(equals(K,KInit));
		assertTrue(equals(X,XInit));
	}
	
	@Test
	public void call_Jama()
	{
		Jama.Matrix IInit =Jama. Matrix.identity(2, 2);
		Jama.Matrix HInit = new Jama.Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		Jama.Matrix PInit = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, -4 } });
		Jama.Matrix KInit = new Jama.Matrix(new double[][] { { 0, 5 }, { 3, -5 } });
		Jama.Matrix XInit = new Jama.Matrix(new double[][] { { -20, 66 }, { 15, -48 } });
		// init
		Jama.Matrix I = IInit.copy();
		Jama.Matrix H = HInit.copy();
		Jama.Matrix P = PInit.copy();
		Jama.Matrix K = KInit.copy();

		// call with new instances
		Jama.Matrix X = (I.minus(K.times(H))).times(P); 
		
		// test consistency
		assertTrue(equals(I,IInit));
		assertTrue(equals(H,HInit));
		assertTrue(equals(P,PInit));
		assertTrue(equals(K,KInit));
		assertTrue(equals(X,XInit));
		
	}
}
	