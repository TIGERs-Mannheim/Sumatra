/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;


class RoleNumberIntegrationTest extends AAiIntegrationTest
{
	private static Stream<String> snapshots()
	{
		return Stream.of(
				"snapshots/roleNumber1.json",
				"snapshots/roleNumber2.json",
				"snapshots/roleNumber3.json",
				"snapshots/roleNumber4.json",
				"snapshots/roleNumber5.json"
		);
	}


	private void runWithGamestate(String snapFile, final GameState gameState) throws IOException
	{
		setGameState(gameState);
		loadSnapshot(snapFile);
		nextFrame();
		nextFrame();
		nextFrame();
		assertNoErrorLog();
		assertNoWarnLog();
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testRunning(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.RUNNING);
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testStop(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.STOP);
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testDirectFree(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.DIRECT_FREE)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testIndirectFree(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.INDIRECT_FREE)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testPrepareKickoff(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PREPARE_KICKOFF)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testKickoff(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.KICKOFF)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testPreparePenalty(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PREPARE_PENALTY)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testPenalty(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.PENALTY)
				.build());
	}


	@ParameterizedTest(name = "[{index}] {displayName} {argumentSetNameOrArgumentsWithNames}")
	@MethodSource("snapshots")
	void testBallPlacement(String snapFile) throws IOException
	{
		runWithGamestate(snapFile, GameState.empty()
				.withForTeam(ETeamColor.YELLOW)
				.withState(EGameState.BALL_PLACEMENT)
				.withBallPlacementPosition(Vector2.fromXY(-2800, 500))
				.build());
	}
}
