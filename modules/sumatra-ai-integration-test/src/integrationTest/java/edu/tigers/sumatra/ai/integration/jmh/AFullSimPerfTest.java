/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration.jmh;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.snapshot.Snapshot;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import java.io.IOException;


/**
 * Base class for benchmarks with simulation.
 */
public abstract class AFullSimPerfTest
{
	private static final String MODULI_CONFIG = "integration_test.xml";


	@SneakyThrows
	private static void beforeClass()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfig(MODULI_CONFIG);
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);
	}


	@SneakyThrows
	@Setup(Level.Trial)
	public void before()
	{
		beforeClass();
		SumatraModel.getInstance().startModules();
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SimulationHelper.setHandleBotCount(false);

		SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(EAutoRefMode.ACTIVE);

		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
	}


	@SneakyThrows
	@TearDown(Level.Trial)
	public void after()
	{
		SumatraModel.getInstance().stopModules();
	}


	private Snapshot loadSimParamsFromSnapshot(final String snapshotFile)
	{
		try
		{
			return Snapshot.loadFromResources(snapshotFile);
		} catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}


	protected void loadSimulation(String snapshotFile)
	{
		loadSimulation(loadSimParamsFromSnapshot(snapshotFile));
	}


	private void loadSimulation(Snapshot snapshot)
	{
		SimulationHelper.loadSimulation(snapshot);
	}


	protected void sendRefereeCommand(SslGcRefereeMessage.Referee.Command command)
	{
		SumatraModel.getInstance().getModule(AReferee.class).sendGameControllerEvent(GcEventFactory.command(command));
	}
}
