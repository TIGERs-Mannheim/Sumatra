/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Calculates the number of bots per play
 */
@Log4j2
@RequiredArgsConstructor
public class PlayNumberCalc extends ACalculator
{

	@Configurable(defValue = "true", comment = "KEEP THIS ALWAYS ACTIVE, unless you're 100% sure. This will deactivate Offense during Running")
	private static boolean activateOffenseDuringRunning = true;

	private final Supplier<Integer> numBallPlacementBots;
	private final Supplier<Integer> numDefender;
	private final Supplier<Integer> numInterchangeBots;
	private final Supplier<Integer> numOffenseBots;

	@Getter
	private Map<EPlay, Integer> playNumbers = Collections.emptyMap();


	@Override
	public void doCalc()
	{
		GameState gameState = getAiFrame().getGameState();

		if (gameState.getState() == EGameState.HALT)
		{
			// No plays
			playNumbers = Collections.emptyMap();
			return;
		}
		playNumbers = new EnumMap<>(EPlay.class);

		interchange();

		if (gameState.isPenaltyOrPreparePenalty() || gameState.isPenaltyShootout())
		{
			penalty();
		} else if (gameState.isBallPlacementForUs())
		{
			ballPlacement();
		} else if (gameState.isPausedGame())
		{
			pausedGame();
		} else if (gameState.getState() == EGameState.POST_GAME)
		{
			postGame();
		} else
		{
			normalMode();
		}
		playNumbers = Collections.unmodifiableMap(playNumbers);
		sanityCheck();
	}


	private void sanityCheck()
	{
		if (!SumatraModel.getInstance().isTournamentMode())
		{
			int assignedBots = assignedBots();
			if (assignedBots != availableBots())
			{
				log.warn(String.format("Assigned number of bots does not match number of available (%d): %s",
						availableBots(), playNumbers));
			}
		}
	}


	private void ballPlacement()
	{
		playNumbers.put(EPlay.BALL_PLACEMENT, Math.min(unassignedBots(), numBallPlacementBots.get()));
		keeper();
		defense();
		playNumbers.put(EPlay.SUPPORT, unassignedBots());
	}


	private void interchange()
	{
		playNumbers.put(EPlay.INTERCHANGE, numInterchangeBots.get());
	}


	private void penalty()
	{
		if (getAiFrame().getGameState().isGameStateForUs())
		{
			penaltyForUs();
		} else
		{
			penaltyForThem();
		}
	}


	private void penaltyForUs()
	{
		keeper();
		attack();
		playNumbers.put(EPlay.DEFENSIVE, Math.max(0, unassignedBots()));
	}


	private void penaltyForThem()
	{
		keeper();
		playNumbers.put(EPlay.PENALTY_THEM, Math.max(0, unassignedBots()));
	}


	private void pausedGame()
	{
		playNumbers.put(EPlay.MAINTENANCE, unassignedBots());
	}


	private void postGame()
	{
		playNumbers.put(EPlay.CHEERING, unassignedBots());
	}


	private void normalMode()
	{
		keeper();
		defense();
		if (activateOffenseDuringRunning || !getAiFrame().getGameState().isRunning())
		{
			attack();
		}
		support();
	}


	private void keeper()
	{
		if (getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId()) && unassignedBots() > 0)
		{
			playNumbers.put(EPlay.KEEPER, 1);
		}
	}


	private void defense()
	{
		int defenders = Math.min(unassignedBots(), numDefender.get());
		playNumbers.put(EPlay.DEFENSIVE, defenders);
	}


	private void attack()
	{
		int attackers = Math.min(unassignedBots(), numOffenseBots.get());
		playNumbers.put(EPlay.OFFENSIVE, attackers);
	}


	private void support()
	{
		playNumbers.put(EPlay.SUPPORT, unassignedBots());
	}


	private int availableBots()
	{
		return getWFrame().getTigerBotsAvailable().size();
	}


	private int assignedBots()
	{
		return playNumbers.values().stream().mapToInt(i -> i).sum();
	}


	private int unassignedBots()
	{
		return Math.max(0, availableBots() - assignedBots());
	}
}
