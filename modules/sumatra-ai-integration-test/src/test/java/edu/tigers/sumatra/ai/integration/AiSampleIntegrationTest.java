/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 */
public class AiSampleIntegrationTest extends AAiIntegrationTest
{
	private static final Logger log = Logger.getLogger(AiSampleIntegrationTest.class.getName());
	
	
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
		assertThat(getMetisAiFrame().getGamestate().getState()).isEqualTo(EGameState.RUNNING);
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
		log.error("Fail!");
		assertThat(logEventWatcher.getEvents(Level.ERROR)).hasSize(1);
	}
}
