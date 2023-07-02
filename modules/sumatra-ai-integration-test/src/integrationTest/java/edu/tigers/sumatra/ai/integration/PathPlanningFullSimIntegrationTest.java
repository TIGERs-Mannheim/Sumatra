/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.integration.blocker.WpSimTimeBlocker;
import edu.tigers.sumatra.ai.integration.stopcondition.BotsReachedDestStopCondition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PathPlanningFullSimIntegrationTest extends AFullSimIntegrationTest
{
	@Before
	public void prepare()
	{
		// turn of AIs
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAIControlState.OFF);
	}


	@Test
	public void complicatedPathPlanning()
	{
		// turn of autoRef
		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.OFF);

		double x = Geometry.getFieldLength() / 2 - 300;
		double y = Geometry.getPenaltyAreaWidth() / 2 + 400;
		var snapshot = readSnapshot("snapshots/pathplanningSituationPenArea.snap")
				.toBuilder()
				.command(SslGcRefereeMessage.Referee.Command.FORCE_START)
				.bot(BotID.createBotId(0, ETeamColor.YELLOW), new SnapObject(
						Vector3.fromXYZ(x, -y, 0),
						Vector3.fromXYZ(0, 0, 0)
				))
				.build();
		initSimulation(snapshot);

		// initialize the `lastWorldFrameWrapper` field
		defaultSimTimeBlocker(0.1).await();

		BotID botId = BotID.createBotId(0, ETeamColor.YELLOW);
		IVector2 initPos = lastWorldFrameWrapper.getWorldFrame(EAiTeam.YELLOW).getBot(botId).getPos();
		IVector2 destination = Vector2.fromXY(-x, -y);
		MoveToSkill skill = MoveToSkill.createMoveToSkill();
		skill.updateDestination(destination);
		SumatraModel.getInstance().getModule(ASkillSystem.class).execute(botId, skill);

		defaultSimTimeBlocker(20)
				.addStopCondition(w -> w.getWorldFrame(EAiTeam.YELLOW).getBot(botId).getPos().distanceTo(destination) < 0.1)
				.await();

		ITrackedBot tBot = lastWorldFrameWrapper.getWorldFrame(EAiTeam.YELLOW).getBot(botId);
		assertThat(tBot.getPos().distanceTo(destination))
				.withFailMessage("The robot did not move from %s to %s in time. Last here: %s", initPos, destination,
						tBot.getPos())
				.isLessThan(1);

		assertNoWarningsOrErrors();
		success();
	}


	@Test
	public void fromSideToCenter()
	{
		Snapshot snapshot = readSnapshot("snapshots/pathplanning/fromSideToCenter.json");
		runPathPlanningWithoutAi(snapshot, 7);
	}


	@Test
	public void fromSideToCenterCrossed()
	{
		Snapshot snapshot = readSnapshot("snapshots/pathplanning/fromSideToCenterCrossed.json");
		runPathPlanningWithoutAi(snapshot, 8);
	}


	@Test
	public void fromSideToOtherSideCrossed()
	{
		Snapshot snapshot = readSnapshot("snapshots/pathplanning/fromSideToOtherSideCrossed.json");
		runPathPlanningWithoutAi(snapshot, 15);
	}


	@Test
	public void fromFrontToBackCrossed()
	{
		Snapshot snapshot = readSnapshot("snapshots/pathplanning/fromFrontToBackCrossed.json");
		runPathPlanningWithoutAi(snapshot, 15);
	}


	@Test
	public void throughOpponents()
	{
		Snapshot snapshot = readSnapshot("snapshots/pathplanning/throughOpponents.json");
		runPathPlanningWithoutAi(snapshot, 2);
	}


	private void runPathPlanningWithoutAi(Snapshot snapshot, double maxDuration)
	{
		initSimulation(snapshot);
		defaultSimTimeBlocker(maxDuration)
				.addStopCondition(new BotsReachedDestStopCondition(snapshot))
				.await();

		assertNoWarningsOrErrors();
		assertNoAvoidableViolations();
		assertBotsHaveMoved();
		success();
	}


	private WpSimTimeBlocker defaultSimTimeBlocker(double maxDuration)
	{
		return new WpSimTimeBlocker(maxDuration).addStopCondition(w -> stuck);
	}
}
