/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 10, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.functionality;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.types.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.matrix.performance.APerformanceTest;


/**
 * test: (I-K*H)*P
 * @author Birgit
 * 
 */
public class Call_IminusKtimesH_timesP extends APerformanceTest
{
	/**
	 */
	@Test
	public void call_Matrix()
	{
		final Matrix IInit = Matrix.identity(2, 2);
		final Matrix HInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		final Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, -4 } });
		final Matrix KInit = new Matrix(new double[][] { { 0, 5 }, { 3, -5 } });
		final Matrix XInit = new Matrix(new double[][] { { -20, 66 }, { 15, -48 } });
		// init
		final Matrix I = IInit.copy();
		final Matrix H = HInit.copy();
		final Matrix P = PInit.copy();
		final Matrix K = KInit.copy();
		
		// call with new instances
		final Matrix X = (I.minus(K.times(H))).times(P);
		
		// test consistency
		assertTrue(equals(I, IInit));
		assertTrue(equals(H, HInit));
		assertTrue(equals(P, PInit));
		assertTrue(equals(K, KInit));
		assertTrue(equals(X, XInit));
	}
	
	
	/**
	 */
	@Test
	public void call_Jama()
	{
		final Jama.Matrix IInit = Jama.Matrix.identity(2, 2);
		final Jama.Matrix HInit = new Jama.Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		final Jama.Matrix PInit = new Jama.Matrix(new double[][] { { 0, 1 }, { 1, -4 } });
		final Jama.Matrix KInit = new Jama.Matrix(new double[][] { { 0, 5 }, { 3, -5 } });
		final Jama.Matrix XInit = new Jama.Matrix(new double[][] { { -20, 66 }, { 15, -48 } });
		// init
		final Jama.Matrix I = IInit.copy();
		final Jama.Matrix H = HInit.copy();
		final Jama.Matrix P = PInit.copy();
		final Jama.Matrix K = KInit.copy();
		
		// call with new instances
		final Jama.Matrix X = (I.minus(K.times(H))).times(P);
		
		// test consistency
		assertTrue(equals(I, IInit));
		assertTrue(equals(H, HInit));
		assertTrue(equals(P, PInit));
		assertTrue(equals(K, KInit));
		assertTrue(equals(X, XInit));
		
	}
}
