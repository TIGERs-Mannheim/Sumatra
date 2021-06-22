/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 2)
@Threads(4)
public class BangBangTrajectoryBenchmark
{
	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();

	private final Random rnd = new Random(42);

	private static final double MAX_POS = 6;
	private static final double MAX_VEL = 4;
	private static final double MAX_ACC = 3;

	private static final int NUM_RND_DATA = 10_000;

	private final List<IVector2> ps = new ArrayList<>();
	private final List<IVector2> vs = new ArrayList<>();


	public static void main(String[] args) throws Exception
	{
		org.openjdk.jmh.Main.main(args);
	}


	public BangBangTrajectoryBenchmark()
	{
		for (int i = 0; i < NUM_RND_DATA; i++)
		{
			ps.add(random(MAX_POS));
			vs.add(random(MAX_VEL));
		}
	}


	@Benchmark
	public double full()
	{
		double sum = 0;
		for (int i = 0; i < ps.size(); i++)
		{
			IVector2 p1 = ps.get(i % ps.size());
			IVector2 p2 = ps.get((i + 1) % ps.size());
			IVector2 v0 = vs.get(i % vs.size());
			BangBangTrajectory2D t = trajectoryFactory.sync(p1, p2, v0, MAX_VEL, MAX_ACC);
			sum += t.getTotalTime();
			sum += t.getPosition(t.getTotalTime()).x();
			sum += t.getVelocity(t.getTotalTime()).x();
			sum += t.getAcceleration(t.getTotalTime()).x();
			sum += t.mirrored().getTotalTime();
		}
		return sum;
	}


	@Benchmark
	public double generate()
	{
		double sum = 0;
		for (int i = 0; i < ps.size(); i++)
		{
			IVector2 p1 = ps.get(i % ps.size());
			IVector2 p2 = ps.get((i + 1) % ps.size());
			IVector2 v0 = vs.get(i % vs.size());
			BangBangTrajectory2D t = trajectoryFactory.sync(p1, p2, v0, MAX_VEL, MAX_ACC);
			sum += t.getTotalTime();
		}
		return sum;
	}


	@Benchmark
	public double getState()
	{
		double sum = 0;
		for (int i = 0; i < ps.size(); i++)
		{
			IVector2 p1 = ps.get(i % ps.size());
			IVector2 p2 = ps.get((i + 1) % ps.size());
			IVector2 v0 = vs.get(i % vs.size());
			BangBangTrajectory2D t = trajectoryFactory.sync(p1, p2, v0, MAX_VEL, MAX_ACC);
			sum += t.getPosition(t.getTotalTime()).x();
			sum += t.getVelocity(t.getTotalTime()).x();
			sum += t.getAcceleration(t.getTotalTime()).x();
		}
		return sum;
	}


	@Benchmark
	public double mirror()
	{
		double sum = 0;
		for (int i = 0; i < ps.size(); i++)
		{
			IVector2 p1 = ps.get(i % ps.size());
			IVector2 p2 = ps.get((i + 1) % ps.size());
			IVector2 v0 = vs.get(i % vs.size());
			BangBangTrajectory2D t = trajectoryFactory.sync(p1, p2, v0, MAX_VEL, MAX_ACC);
			sum += t.mirrored().getTotalTime();
		}
		return sum;
	}


	private IVector2 random(double range)
	{
		return Vector2.fromXY(rnd.nextDouble() * range - range / 2, rnd.nextDouble() * range - range / 2);
	}
}
