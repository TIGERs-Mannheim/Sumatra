/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.trajectory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Test Bang-Bang Trajectory 2D Look-Up Table
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BangBangTrajectory2dDLUTPerfTest
{
	private static final double maxLUTPos = 15.0;
	private static final double maxLUTVel = 5.0;
	private static final double maxTrajVel = 2.0;
	private static final double maxTrajAcc = 3.0;
	
	private static final BangBangTrajectory2DLUT lut = new BangBangTrajectory2DLUT(maxLUTPos, maxLUTVel, maxTrajVel,
			maxTrajAcc);
	
	
	@Test
	public void testGenerationTime()
	{
		int numSamples = 10;
		
		List<Double> samples = new LinkedList<>();
		
		System.out.println("### LUT Generation Benchmark ###");
		
		for (int i = 0; i < numSamples; i++)
		{
			long tStart = System.nanoTime();
			new BangBangTrajectory2DLUT(maxLUTPos, maxLUTVel, maxTrajVel, maxTrajAcc);
			long tEnd = System.nanoTime();
			
			double generationTime = (tEnd - tStart) * 1e-9;
			
			samples.add(generationTime);
			
			assertThat(generationTime).isLessThan(60.0);
		}
		
		DoubleSummaryStatistics stats = samples.subList(numSamples / 2, numSamples)
				.stream().collect(Collectors.summarizingDouble(Double::doubleValue));
		
		System.out.println("Generation average: " + stats.getAverage() + "s");
	}
	
	
	@Test
	public void testQueryPerformance()
	{
		System.out.println("### LUT Query Benchmark ###");
		
		double calcWarmup = doPerformanceRunCalc();
		System.out.println("Calc Warmup: " + (calcWarmup * 1e6) + "us");
		
		double calcBenchmark = doPerformanceRunCalc();
		System.out.println("Calc Benchmark: " + (calcBenchmark * 1e6) + "us");
		
		double lutWarmup = doPerformanceRunLUT();
		System.out.println("LUT Warmup: " + (lutWarmup * 1e6) + "us");
		
		double lutBenchmark = doPerformanceRunLUT();
		System.out.println("LUT Benchmark: " + (lutBenchmark * 1e6) + "us");
	}
	
	
	private double doPerformanceRunCalc()
	{
		final int STEPS = 100;
		final int NUM_SAMPLES = STEPS * STEPS * STEPS * 8;
		
		long tStart = System.nanoTime();
		
		for (double finalX = -maxLUTPos + 1e-3; finalX < maxLUTPos; finalX += maxLUTPos / STEPS)
		{
			for (double finalY = -maxLUTPos + 1e-3; finalY < maxLUTPos; finalY += maxLUTPos / STEPS)
			{
				IVector2 finalPos = Vector2.fromXY(finalX, finalY);
				
				for (double initialVelAbs = -maxLUTVel + 1e-3; initialVelAbs < maxLUTVel; initialVelAbs += maxLUTVel
						/ STEPS)
				{
					IVector2 initialVel = Vector2.fromX(initialVelAbs);
					
					new BangBangTrajectory2D(Vector2f.ZERO_VECTOR, finalPos, initialVel,
							maxTrajVel, maxTrajAcc);
				}
			}
		}
		
		long tEnd = System.nanoTime();
		
		return ((tEnd - tStart) * 1e-9) / NUM_SAMPLES;
	}
	
	
	private double doPerformanceRunLUT()
	{
		final int STEPS = 100;
		final int NUM_SAMPLES = STEPS * STEPS * STEPS * 8;
		
		long tStart = System.nanoTime();
		
		for (double finalX = -maxLUTPos + 1e-3; finalX < maxLUTPos; finalX += maxLUTPos / STEPS)
		{
			for (double finalY = -maxLUTPos + 1e-3; finalY < maxLUTPos; finalY += maxLUTPos / STEPS)
			{
				IVector2 finalPos = Vector2.fromXY(finalX, finalY);
				
				for (double initialVelAbs = -maxLUTVel + 1e-3; initialVelAbs < maxLUTVel; initialVelAbs += maxLUTVel
						/ STEPS)
				{
					IVector2 initialVel = Vector2.fromX(initialVelAbs);
					
					lut.getTrajectory(Vector2f.ZERO_VECTOR, finalPos, initialVel);
				}
			}
		}
		
		long tEnd = System.nanoTime();
		
		return ((tEnd - tStart) * 1e-9) / NUM_SAMPLES;
	}
	
}
