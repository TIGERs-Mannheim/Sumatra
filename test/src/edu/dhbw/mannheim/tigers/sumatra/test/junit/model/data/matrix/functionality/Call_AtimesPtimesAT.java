/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2010
 * Author(s): Birgit
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.functionality;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.matrix.performance.APerformanceTest;


/**
 * Class, to test some functionality for the Kalman-filter
 * @author Birgit
 * 
 */
public class Call_AtimesPtimesAT extends APerformanceTest
{
	// "_" before and behind a parameter means, that this must not changed!!!
	// A*_P_*AT
	
	@Test
	public void call_1()
	{
		Matrix AInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, 0 } });
		Matrix SInit = new Matrix(new double[][] { { 4, 10 }, { 10, 24 } });
		
		// init
		Matrix A = AInit.copy();
		Matrix P = PInit.copy();
		
		// call with new instances
		Matrix S = A.times(P).times(A.transpose());
		
		// test consistency
		assertTrue(equals(A,AInit));
		assertTrue(equals(P,PInit));
		assertTrue(equals(S,SInit));
	}
	

	@Test
	public void testJama()
	{
		
		Matrix AInit = new Matrix(new double[][] { { 1, 2 }, { 3, 4 } });
		Matrix PInit = new Matrix(new double[][] { { 0, 1 }, { 1, 0 } });
		Matrix SInit = new Matrix(new double[][] { { 4, 10 }, { 10, 24 } });
		
		// init
		Matrix A = AInit.copy();
		Matrix P = PInit.copy();
		
		Matrix S = (A.times(P)).times(A.transpose());
		
		// test consistency
		assertTrue(equals(A, AInit));
		assertTrue(equals(S, SInit));
		assertTrue(equals(P, PInit));
	}
	
}
