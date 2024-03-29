/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.jmh;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.WpSimTimeBlocker;
import edu.tigers.sumatra.ai.integration.stopcondition.BotsReachedDestStopCondition;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.snapshot.Snapshot;
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
 * Let robots move from initial maintenance position to their STOP position. Wait until no robot is moving anymore.
 */
@State(Scope.Benchmark)
public class PathPlanningPerfTest extends AFullSimPerfTest
{
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void run()
	{
		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.OFF);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.OFF);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.OFF);
		Snapshot snapshot = initSimulation("snapshots/pathplanning/fromSideToCenterCrossed.json");
		sendRefereeCommand(SslGcRefereeMessage.Referee.Command.HALT);
		new WpSimTimeBlocker(30)
				.addStopCondition(new BotsReachedDestStopCondition(snapshot))
				.await();
	}


	public static void main(String[] args) throws RunnerException
	{
		if (args.length > 0)
		{
			PathPlanningPerfTest test = new PathPlanningPerfTest();
			for (int i = 0; i < Integer.parseInt(args[0]); i++)
			{
				test.before();
				test.run();
				test.after();
			}
		} else
		{
			Options opt = new OptionsBuilder()
					.include(PathPlanningPerfTest.class.getSimpleName())
					.forks(5)
					.warmupIterations(5)
					.measurementIterations(5)
					.build();

			new Runner(opt).run();
		}
	}
}
