/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.jmh;

import edu.tigers.sumatra.ai.integration.AiSimTimeBlocker;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


/**
 * Run simulation with force_start for a fixed amount of time.
 */
@State(Scope.Benchmark)
public class ForceStartFixDurationPerfTest extends AFullSimPerfTest
{
	@Benchmark
	@BenchmarkMode({ Mode.AverageTime, Mode.SingleShotTime })
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void run()
	{
		loadSimulation("snapshots/stoppedGame11vs11.json");
		sendRefereeCommand(SslGcRefereeMessage.Referee.Command.FORCE_START);
		new AiSimTimeBlocker(1).await();
	}


	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder()
				.include(ForceStartFixDurationPerfTest.class.getSimpleName())
				.forks(2)
				.warmupIterations(1)
				.measurementIterations(4)
				.build();

		new Runner(opt).run();
	}
}
