/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class BangBangTrajectoryPerfTest
{
	
	private final Random rnd = new Random(42);
	
	double maxVel = 4;
	double maxAcc = 3;
	
	
	@Test
	public void test()
	{
		int numRndData = 100000;
		int numWarmup = 10000000;
		int numRun = 1000000;
		
		List<IVector2> ps = new ArrayList<>();
		List<IVector2> vs = new ArrayList<>();
		
		for (int i = 0; i < numRndData; i++)
		{
			ps.add(random(6));
			vs.add(random(4));
		}
		
		long t0 = System.nanoTime();
		double sumWarmup = run(numWarmup, ps, vs);
		long t1 = System.nanoTime();
		double sumWarmupRef = run(numRun, ps, vs);
		long t2 = System.nanoTime();
		double sumRun = run(numRun, ps, vs);
		long t3 = System.nanoTime();
		
		System.out.printf("Mean total time (s):\n%10.2f\n%10.2f\n%10.2f\n",
				(sumWarmup / numWarmup),
				(sumWarmupRef / numRun),
				(sumRun / numRun));
		
		double tWarmup = (t1 - t0) / 1e6;
		double tWarmupRef = (t2 - t1) / 1e6;
		double tRun = (t3 - t2) / 1e6;
		System.out.printf("Times (ms):\n%10.2f\n%10.2f\n%10.2f\n", tWarmup, tWarmupRef, tRun);
		System.out.println();
	}
	
	
	private double run(final int numRuns, final List<IVector2> ps, final List<IVector2> vs)
	{
		double sum = 0;
		for (int i = 0; i < numRuns; i++)
		{
			IVector2 p1 = ps.get(i % ps.size());
			IVector2 p2 = ps.get((i + 1) % ps.size());
			IVector2 v1 = vs.get(i % vs.size());
			sum += new BangBangTrajectory2D(p1, p2, v1, maxVel, maxAcc).getTotalTime();
		}
		return sum;
	}
	
	
	private IVector2 random(double range)
	{
		return Vector2.fromXY(rnd.nextDouble() * range - range / 2, rnd.nextDouble() * range - range / 2);
	}
}
