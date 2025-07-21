/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 */
@Log4j2
public class AiSampleIntegrationTest extends AAiIntegrationTest
{
	@Test
	public void testThreeIterations() throws Exception
	{
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		nextFrame();
		nextFrame();
		nextFrame();
	}


	@Test
	public void testMetisOnly() throws Exception
	{
		setGameState(GameState.RUNNING);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		processMetis();
		assertThat(getMetisAiFrame().getGameState().getState()).isEqualTo(EGameState.RUNNING);
	}


	@Test
	public void testAthenaOnly() throws Exception
	{
		setGameState(GameState.RUNNING);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		processAthena();
		assertThat(getAthenaAiFrame().getPlayStrategy().getActiveRoles()).hasSize(6);
	}


	@Test
	public void testFinish() throws Exception
	{
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		finishFrame();
	}


	@Test
	public void testHalt() throws Exception
	{
		setGameState(GameState.HALT);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		finishFrame();
		assertThat(getAthenaAiFrame().getPlayStrategy().getActiveRoles()).isEmpty();
	}


	@Test
	public void testLogger()
	{
		log.error("Fake fail");
		assertThat(logEventWatcher.getEvents(Level.ERROR)).hasSize(1);
	}
}
