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
class AiSampleIntegrationTest extends AAiIntegrationTest
{
	@Test
	void testThreeIterations() throws Exception
	{
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		nextFrame();
		nextFrame();
		nextFrame();
	}


	@Test
	void testMetisOnly() throws Exception
	{
		setGameState(GameState.RUNNING);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		processMetis();
		assertThat(getMetisAiFrame().getGameState().getState()).isEqualTo(EGameState.RUNNING);
	}


	@Test
	void testAthenaOnly() throws Exception
	{
		setGameState(GameState.RUNNING);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		processAthena();
		assertThat(getAthenaAiFrame().getPlayStrategy().getActiveRoles()).hasSize(6);
	}


	@Test
	void testFinish() throws Exception
	{
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		finishFrame();
	}


	@Test
	void testHalt() throws Exception
	{
		setGameState(GameState.HALT);
		loadSnapshot("snapshots/aiSampleIntegration.snap");
		finishFrame();
		assertThat(getAthenaAiFrame().getPlayStrategy().getActiveRoles()).isEmpty();
	}


	@Test
	void testLogger()
	{
		log.error("Fake fail");
		assertThat(LOG_EVENT_WATCHER.getEvents(Level.ERROR)).hasSize(1);
	}
}
