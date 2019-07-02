/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import java.util.Collections;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * Calculates the number of bots per play
 */
public class PlayNumberCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(PlayNumberCalc.class.getName());


	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		GameState gameState = getNewTacticalField().getGameState();

		if (gameState.getState() == EGameState.HALT)
		{
			// No plays
			return;
		}

		interchange();

		if (gameState.isPenaltyOrPreparePenalty() || gameState.isPenaltyShootout())
		{
			penalty();
		} else if (gameState.isBallPlacementForUs())
		{
			ballPlacement();
		} else if (gameState.isDirectFreeForUs() || gameState.isDirectFreeForThem())
		{
			directFreeKick();
		} else if (gameState.isKickoffOrPrepareKickoff())
		{
			kickoff();
		} else if (gameState.isPausedGame())
		{
			pausedGame();
		} else if (gameState.getState() == EGameState.POST_GAME)
		{
			postGame();
		} else if (getNewTacticalField().isInsaneKeeper())
		{
			insaneKeeper();
		} else
		{
			normalMode();
		}

		sanityCheck();
	}


	private void sanityCheck()
	{
		if (SumatraModel.getInstance().isTestMode())
		{
			int assignedBots = assignedBots();
			if (assignedBots != availableBots())
			{
				log.warn(String.format("Assigned number of bots does not match number of available (%d): %s",
						availableBots(), getNewTacticalField().getPlayNumbers()));
			}
		}
	}


	private void ballPlacement()
	{
		keeper();

		int ballPlacementBots = getNewTacticalField().getDesiredBotMap()
				.getOrDefault(EPlay.BALL_PLACEMENT, Collections.emptySet()).size();

		getNewTacticalField().putPlayNumbers(EPlay.BALL_PLACEMENT, Math.min(unassignedBots(), ballPlacementBots));
		defense();
		getNewTacticalField().putPlayNumbers(EPlay.SUPPORT, unassignedBots());
	}


	private void interchange()
	{
		getNewTacticalField().putPlayNumbers(EPlay.INTERCHANGE,
				getNewTacticalField().getBotInterchange().getNumInterchangeBots());
	}


	private void directFreeKick()
	{
		if (getNewTacticalField().getGameState().isGameStateForUs())
		{
			directFreeKickForUs();
		} else
		{
			directFreeKickForThem();
		}
	}


	private void directFreeKickForUs()
	{
		keeper();
		defense();
		attack();
		support();
	}


	private void directFreeKickForThem()
	{
		keeper();
		defense();
		attack();
		support();
	}


	private void kickoff()
	{
		if (getNewTacticalField().getGameState().isGameStateForUs())
		{
			kickoffForUs();
		} else
		{
			kickoffForThem();
		}
	}


	private void kickoffForUs()
	{
		keeper();

		int numKickoffBots = 3;
		getNewTacticalField().putPlayNumbers(EPlay.KICKOFF, Math.min(unassignedBots(), numKickoffBots));
		getNewTacticalField().putPlayNumbers(EPlay.DEFENSIVE, unassignedBots());
	}


	private void kickoffForThem()
	{
		keeper();
		getNewTacticalField().putPlayNumbers(EPlay.DEFENSIVE, unassignedBots());
	}


	private void penalty()
	{
		if (getNewTacticalField().getGameState().isGameStateForUs())
		{
			penaltyForUs();
		} else
		{
			penaltyForThem();
		}
	}


	private void penaltyForUs()
	{
		if (!getNewTacticalField().getGameState().isPenaltyShootout()
				&& availableBots() > 1)
		{
			keeper();
		}


		if (getAiFrame().getGamestate().isPenaltyShootout())
		{
			int botsAvailable = availableBots();
			int numAttackers = 1;
			getNewTacticalField().putPlayNumbers(EPlay.ATTACKER_SHOOTOUT, Math.min(numAttackers, botsAvailable));
			getNewTacticalField().putPlayNumbers(EPlay.EXCHANGE_POSITIONING, unassignedBots());
		} else
		{
			defense();
			getNewTacticalField().putPlayNumbers(EPlay.PENALTY_WE, unassignedBots());
		}
	}


	private void penaltyForThem()
	{
		keeper();

		if (getNewTacticalField().getGameState().isPenaltyShootout())
		{
			getNewTacticalField().putPlayNumbers(EPlay.EXCHANGE_POSITIONING, unassignedBots());
		} else
		{
			getNewTacticalField().putPlayNumbers(EPlay.PENALTY_THEM, Math.max(0, unassignedBots()));
		}
	}


	private void pausedGame()
	{
		getNewTacticalField().putPlayNumbers(EPlay.MAINTENANCE, availableBots());
	}


	private void postGame()
	{
		getNewTacticalField().putPlayNumbers(EPlay.CHEERING, availableBots());
	}


	private void insaneKeeper()
	{
		keeper();
		attack();
		support();
	}


	private void normalMode()
	{
		keeper();
		defense();
		attack();
		support();
	}


	private void keeper()
	{
		if (!getNewTacticalField().isInsaneKeeper()
				&& getWFrame().getTigerBotsAvailable().keySet().contains(getAiFrame().getKeeperId()))
		{
			getNewTacticalField().putPlayNumbers(EPlay.KEEPER, 1);
		}
	}


	private void defense()
	{
		getNewTacticalField().putPlayNumbers(EPlay.DEFENSIVE, getNewTacticalField().getNumDefender());
	}


	private void attack()
	{
		OffensiveStrategy offensiveStrategy = getNewTacticalField().getOffensiveStrategy();
		int attackers = Math.min(unassignedBots(), offensiveStrategy.getDesiredBots().size());
		getNewTacticalField().putPlayNumbers(EPlay.OFFENSIVE, attackers);
	}


	private void support()
	{
		getNewTacticalField().putPlayNumbers(EPlay.SUPPORT, unassignedBots());
	}


	private int availableBots()
	{
		return getWFrame().getTigerBotsAvailable().size();
	}


	private int assignedBots()
	{
		return getNewTacticalField().getPlayNumbers().values().stream().mapToInt(i -> i).sum();
	}


	private int unassignedBots()
	{
		return Math.max(0, availableBots() - assignedBots());
	}
}
