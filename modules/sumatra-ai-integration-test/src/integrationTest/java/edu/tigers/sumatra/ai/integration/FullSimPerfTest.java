/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Run tests in a full simulation environment
 */
@Ignore // uncomment if you want to run it
public class FullSimPerfTest extends AFullSimIntegrationTest
{
	private MoveToSkill skill;
	private double maxMaxExecutionTime;
	private List<Double> averageExecutionTimes;


	@Before
	@Override
	public void before()
	{
		super.before();
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);
	}


	@SneakyThrows
	@Test
	public void runPenArea()
	{
		IVector2 destination = Vector2.fromXY(-5900, -1400);
		int n = 10;
		for (int i = 0; i < n; i++)
		{
			maxMaxExecutionTime = 0;
			averageExecutionTimes = new ArrayList<>();
			runSim(Snapshot.loadFromResources("snapshots/pathplanningSituationPenArea.snap"), Collections
					.singletonMap(BotID.createBotId(0, ETeamColor.YELLOW), destination));
		}
	}


	@SneakyThrows
	@Test
	public void runWall()
	{
		IVector2 destination = Vector2.fromXY(2600, 100);
		int n = 20;
		for (int i = 0; i < n; i++)
		{
			maxMaxExecutionTime = 0;
			averageExecutionTimes = new ArrayList<>();
			runSim(Snapshot.loadFromResources("snapshots/pathplanningSituationWall.snap"), Collections
					.singletonMap(BotID.createBotId(0, ETeamColor.YELLOW), destination));
		}
	}


	@Test
	public void runRandomCrowded()
	{
		int n = 1000;
		int m = 1;
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
			{
				maxMaxExecutionTime = 0;
				averageExecutionTimes = new ArrayList<>();

				System.out.printf("%d %d ", i, j);
				runSim(new SnapshotGenerator().randomNear32Bots(i).buildSnapshot(), Collections
						.singletonMap(BotID.createBotId(0, ETeamColor.YELLOW), Vector2f.fromY(Geometry.getFieldWidth() / 2)));
			}
		}
	}


	@Test
	public void runRandomVersus()
	{
		int n = 10;
		int m = 1;
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
			{
				maxMaxExecutionTime = 0;
				averageExecutionTimes = new ArrayList<>();


				Map<BotID, IVector2> destinations = new HashMap<>();
				for (BotID botID : BotID.getAll())
				{
					double x = Geometry.getFieldLength() / 2 - Geometry.getPenaltyAreaDepth() - 1000;
					double y = Math.ceil(botID.getNumber() / 2.0) * 200 * (botID.getNumber() % 2 == 0 ? 1 : -1);
					IVector2 p = Vector2.fromXY(-x, y);
					destinations.put(botID, p);
				}

				runSim(new SnapshotGenerator().random16vs16Bots(i).buildSnapshot(), destinations);
			}
		}
	}


	private void runSim(Snapshot snapshot, Map<BotID, IVector2> destinations)
	{
		var params = snapshot.toBuilder()
				.command(Command.FORCE_START)
				.build();
		SimulationHelper.loadSimulation(params);

		WpSimTimeBlocker simTimeBlocker = new WpSimTimeBlocker(0.1);
		simTimeBlocker.await();
		simTimeBlocker = new WpSimTimeBlocker(20);

		for (Map.Entry<BotID, IVector2> entry : destinations.entrySet())
		{
			BotID botId = entry.getKey();
			IVector2 destination = entry.getValue();
			skill = MoveToSkill.createMoveToSkill();
			MovementCon moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaOurObstacle(true);
			moveCon.setPenaltyAreaTheirObstacle(true);
			moveCon.setGameStateObstacle(false);
			skill.updateDestination(destination);
			SumatraModel.getInstance().getModule(ASkillSystem.class).execute(botId, skill);

			simTimeBlocker.addStopCondition(
					wfw -> wfw.getWorldFrame(EAiTeam.primary(botId.getTeamColor())).getBot(botId).getPos()
							.distanceTo(destination) < 1e-3);
		}

		long tStart = System.nanoTime();
		simTimeBlocker.await();
		long tStop = System.nanoTime();

		// stop measurement
		skill = null;

		double execDuration = (tStop - tStart) / 1e9;
		double simDuration = simTimeBlocker.getDuration();
		double distToDest = destinations.entrySet().stream()
				.mapToDouble(e -> lastWorldFrameWrapper.getWorldFrame(EAiTeam.primary(e.getKey().getTeamColor()))
						.getBot(e.getKey()).getPos()
						.distanceTo(e.getValue()))
				.sum();
		double maxAverageExecutionTime = averageExecutionTimes.stream().mapToDouble(a -> a).average().orElse(0.0);

		System.out.printf("%10.1f %10.2f %10.2f %10.6f %10.6f\n", distToDest, execDuration,
				simDuration,
				maxMaxExecutionTime, maxAverageExecutionTime);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		super.onNewWorldFrame(wFrameWrapper);

		if (skill != null)
		{
			maxMaxExecutionTime = Math.max(maxMaxExecutionTime, skill.getAverageTimeMeasure().getMaxTime());
			averageExecutionTimes.add(skill.getAverageTimeMeasure().getLatestMeasureTime());
		}
	}
}
