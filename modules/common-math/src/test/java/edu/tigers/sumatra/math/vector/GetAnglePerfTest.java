/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.util.FastMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;


@BenchmarkMethodChart(filePrefix = "benchmark-getAngle")
@BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 10, callgc = true)
public class GetAnglePerfTest extends AbstractBenchmark
{
	
	private static final int RUNS = 5_000_000;
	private static double result = 0;
	
	private static List<Double> data;
	
	
	@BeforeClass
	public static void setup()
	{
		DoubleStream rndStream = new Random(0).doubles();
		data = new ArrayList<>();
		rndStream.limit(RUNS * 2).forEach(d -> data.add(d));
	}
	
	
	@AfterClass
	public static void after()
	{
		System.out.println(result);
	}
	
	
	@Test
	public void testMath()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += Math.atan2(data.get(i), data.get(i + 1));
	}
	
	
	@Test
	public void testFastMath()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += FastMath.atan2(data.get(i), data.get(i + 1));
	}
	
	
	@Test
	public void testStrictMath()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += StrictMath.atan2(data.get(i), data.get(i + 1));
	}
	
	
	@Test
	public void testJafamaFastMath()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += net.jafama.FastMath.atan2(data.get(i), data.get(i + 1));
	}
	
	
	@Test
	public void testJafamaStrictFastMath()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += net.jafama.StrictFastMath.atan2(data.get(i), data.get(i + 1));
	}
	
	
	@Test
	public void testAcosSqrt()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += acosSqrt(data.get(i), data.get(i + 1));
	}
	
	
	double acosSqrt(double x, double y)
	{
		double result = net.jafama.FastMath.acos(x / net.jafama.FastMath.sqrt((x * x) + (y * y)));
		result = AngleMath.normalizeAngle(result);
		if (y < 0)
		{
			result = -result;
		}
		return result;
	}
	
	
	@Test
	public void testLut()
	{
		for (int i = 0; i < RUNS; i += 2)
			result += SumatraMath.atan2(data.get(i), data.get(i + 1));
	}
}
