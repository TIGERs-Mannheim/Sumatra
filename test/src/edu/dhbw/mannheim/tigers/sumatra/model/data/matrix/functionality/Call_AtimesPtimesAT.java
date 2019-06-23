/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2010
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
 * Class, to test some functionality for the Kalman-filter
 * @author Birgit
 * 
 */
public class Call_AtimesPtimesAT extends APerformanceTest
{
	// "_" before and behind a parameter means, that this must not changed!!!
	// A*_P_*AT
	/**
	 */
	@Test
	public void call_1()
	{
		final Matrix AInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		final Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, 0 } });
		final Matrix SInit = new Matrix(new double[][] { { 4, 10 }, { 10, 24 } });
		
		// init
		final Matrix A = AInit.copy();
		final Matrix P = PInit.copy();
		
		// call with new instances
		final Matrix S = A.times(P).times(A.transpose());
		
		// test consistency
		assertTrue(equals(A, AInit));
		assertTrue(equals(P, PInit));
		assertTrue(equals(S, SInit));
	}
	
	
	/**
	 */
	@Test
	public void testJama()
	{
		
		final Matrix AInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		final Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, 0 } });
		final Matrix SInit = new Matrix(new double[][] { { 4, 10 }, { 10, 24 } });
		
		// init
		final Matrix A = AInit.copy();
		final Matrix P = PInit.copy();
		
		final Matrix S = (A.times(P)).times(A.transpose());
		
		// test consistency
		assertTrue(equals(A, AInit));
		assertTrue(equals(S, SInit));
		assertTrue(equals(P, PInit));
	}
	
}
