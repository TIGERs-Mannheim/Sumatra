/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@RunWith(Parameterized.class)
public class RoleNumberIntegrationTest extends AAiIntegrationTest
{

	private String snapFile;


	public RoleNumberIntegrationTest(final String snapFile)
	{
		this.snapFile = snapFile;
	}


	@Parameterized.Parameters
	public static Collection<Object[]> parameters()
	{
		Collection<Object[]> params = new ArrayList<>();
		params.add(new Object[] { "snapshots/roleNumber1.json" });
		params.add(new Object[] { "snapshots/roleNumber2.json" });
		params.add(new Object[] { "snapshots/roleNumber3.json" });
		params.add(new Object[] { "snapshots/roleNumber4.json" });
		params.add(new Object[] { "snapshots/roleNumber5.json" });
		return params;
	}


	private void runWithGamestate(final GameState gameState) throws IOException
	{
		setGameState(gameState);
		loadSnapshot(snapFile);
		nextFrame();
		nextFrame();
		nextFrame();
		assertNoErrorLog();
		assertNoWarnLog();
	}


	@Test
	public void testRunning() throws IOException
	{
		runWithGamestate(GameState.RUNNING);
	}


	@Test
	public void testStop() throws IOException
	{
		runWithGamestate(GameState.STOP);
	}


	@Test
	public void testDirectFree() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.DIRECT_FREE)
				.build());
	}


	@Test
	public void testIndirectFree() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.INDIRECT_FREE)
				.build());
	}


	@Test
	public void testPrepareKickoff() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PREPARE_KICKOFF)
				.build());
	}


	@Test
	public void testKickoff() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.KICKOFF)
				.build());
	}


	@Test
	public void testPreparePenalty() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PREPARE_PENALTY)
				.build());
	}


	@Test
	public void testPenalty() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PENALTY)
				.build());
	}


	@Test
	public void testBallPlacement() throws IOException
	{
		runWithGamestate(GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.BALL_PLACEMENT)
				.withBallPlacementPosition(Vector2.fromXY(-2800, 500))
				.build());
	}
}
