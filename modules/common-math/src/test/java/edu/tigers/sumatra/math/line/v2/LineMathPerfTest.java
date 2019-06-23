/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author Lukas Magel
 */
public class LineMathPerfTest extends AbstractBenchmark
{
	
	private static edu.tigers.sumatra.math.line.ILine	lineV1A;
	private static edu.tigers.sumatra.math.line.ILine	lineV1B;
	
	private static ILine											lineV2A;
	private static ILine											lineV2B;
	
	
	@BeforeClass
	public static void setup()
	{
		IVector2 sVA = Vector2.ZERO_VECTOR;
		IVector2 sVB = Vector2.fromXY(0, 1);
		
		IVector2 dVA = Vector2.X_AXIS;
		IVector2 dVB = Vector2.Y_AXIS;
		
		lineV1A = edu.tigers.sumatra.math.line.Line.fromDirection(sVA, dVA);
		lineV1B = edu.tigers.sumatra.math.line.Line.fromDirection(sVB, dVB);
		
		lineV2A = Line.fromDirection(sVA, dVA);
		lineV2B = Line.fromDirection(sVB, dVB);
	}
	
	
	@BenchmarkOptions(benchmarkRounds = 10_000_000, warmupRounds = 50_000_000)
	@Test
	public void testV2()
	{
		lineV2A.intersectLine(lineV2B);
	}
	
	
	@BenchmarkOptions(benchmarkRounds = 10_000_000, warmupRounds = 50_000_000)
	@Test
	public void testV1()
	{
		lineV1A.intersectionWith(lineV1B);
	}
	
	
}
