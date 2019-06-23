/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * Calculates the number of bots per play
 *
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class PlayNumberCalc extends ACalculator
{
	@SuppressWarnings("unused")
	private final Logger log = LogManager.getLogger(PlayNumberCalc.class);
	
	
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	@Override
	public void doCalc(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		GameState gameState = tacticalField.getGameState();
		
		if (gameState.getState() == EGameState.HALT)
		{
			// No plays
			return;
		}
		
		if (gameState.isPenaltyOrPreparePenalty() || gameState.isPenaltyShootout())
		{
			handlePenaltyState(tacticalField, aiFrame, gameState);
		} else if (gameState.isBallPlacementForUs())
		{
			handleBallPlacementState(tacticalField, aiFrame);
		} else if (gameState.isDirectFreeForUs() || gameState.isDirectFreeForThem())
		{
			handleDirectFreeState(tacticalField, aiFrame, gameState);
		} else if (gameState.isKickoffOrPrepareKickoff())
		{
			handleKickoffState(tacticalField, aiFrame, gameState);
		} else
		{
			
			switch (gameState.getState())
			{
				case BREAK:
				case TIMEOUT:
					breakGame(tacticalField, aiFrame);
					break;
				case POST_GAME:
					postGame(tacticalField, aiFrame);
					break;
				default:
					keeper(tacticalField, aiFrame);
					normalMode(tacticalField, aiFrame);
					break;
			}
		}
	}
	
	
	private void handleBallPlacementState(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		keeper(tacticalField, aiFrame);
		ballPlacementMode(tacticalField, aiFrame);
	}
	
	
	private void handleDirectFreeState(final TacticalField tacticalField, final BaseAiFrame aiFrame,
			final GameState gameState)
	{
		if (gameState.isGameStateForUs())
		{
			directKickForUs(tacticalField, aiFrame);
		} else
		{
			keeper(tacticalField, aiFrame);
			normalMode(tacticalField, aiFrame);
		}
	}
	
	
	private void handleKickoffState(final TacticalField tacticalField, final BaseAiFrame aiFrame,
			final GameState gameState)
	{
		if (gameState.isGameStateForUs())
		{
			kickoff(tacticalField, aiFrame);
		} else
		{
			kickoffDefense(tacticalField, aiFrame);
		}
	}
	
	
	private void handlePenaltyState(final TacticalField tacticalField, final BaseAiFrame aiFrame,
			final GameState gameState)
	{
		if (gameState.isGameStateForUs())
		{
			if (!gameState.isPenaltyShootout())
			{
				keeper(tacticalField, aiFrame);
			}
			
			penaltyWeMode(tacticalField, aiFrame);
		} else
		{
			keeper(tacticalField, aiFrame);
			penaltyTheyMode(tacticalField);
		}
	}
	
	
	private void directKickForUs(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		if (OffensiveMath.isKeeperInsane(aiFrame, tacticalField))
		{
			normalMode(tacticalField, aiFrame);
		} else
		{
			keeper(tacticalField, aiFrame);
			normalMode(tacticalField, aiFrame);
		}
	}
	
	
	private void breakGame(final TacticalField tacticalField, final BaseAiFrame baseAiFrame)
	{
		int numRoles = baseAiFrame.getWorldFrame().getTigerBotsAvailable().size();
		tacticalField.getPlayNumbers().put(EPlay.MAINTENANCE, numRoles);
	}
	
	
	private void postGame(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int numRoles = aiFrame.getWorldFrame().getTigerBotsAvailable().size();
		tacticalField.getPlayNumbers().put(EPlay.CHEERING, numRoles);
	}
	
	
	private void penaltyWeMode(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int botsAvailable = aiFrame.getWorldFrame().getTigerBotsAvailable().size();
		
		if (aiFrame.getGamestate().isPenaltyShootout())
		{
			int numAttackers = 1;
			tacticalField.getPlayNumbers().put(EPlay.ATTACKER_SHOOTOUT, Math.min(numAttackers, botsAvailable));
			tacticalField.getPlayNumbers().put(EPlay.EXCHANGE_POSITIONING, Math.max(0, botsAvailable - numAttackers));
			return;
		}
		
		int numRoles = Math.max(1, botsAvailable);
		normalDefense(tacticalField, aiFrame);
		tacticalField.getPlayNumbers().put(EPlay.PENALTY_WE, numRoles);
	}
	
	
	private void penaltyTheyMode(final TacticalField tacticalField)
	{
		int botsAvailable = getAiFrame().getWorldFrame().getTigerBotsVisible().size() - getNumKeeper();
		
		if (tacticalField.getGameState().isPenaltyShootout())
		{
			tacticalField.getPlayNumbers().put(EPlay.EXCHANGE_POSITIONING, botsAvailable);
			return;
		}
		
		tacticalField.getPlayNumbers().put(EPlay.PENALTY_THEM, Math.max(0, botsAvailable));
	}
	
	
	private void kickoff(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int available = aiFrame.getWorldFrame().getTigerBotsVisible().size();
		
		keeper(tacticalField, aiFrame);
		
		int numKickoffBots = 3;
		tacticalField.getPlayNumbers().put(EPlay.KICKOFF,
				Math.max(0, Math.min(available - getNumKeeper(), numKickoffBots)));
		tacticalField.getPlayNumbers().put(EPlay.DEFENSIVE,
				Math.max(0, available - numKickoffBots - getNumKeeper()));
	}
	
	
	private void kickoffDefense(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int available = aiFrame.getWorldFrame().getTigerBotsVisible().size();
		
		keeper(tacticalField, aiFrame);
		tacticalField.getPlayNumbers().put(EPlay.DEFENSIVE, Math.max(0, available - getNumKeeper()));
		
	}
	
	
	private void keeper(final TacticalField tacticalField, final BaseAiFrame baseAiFrame)
	{
		// if keeper is present request it, else request no, because it is against the law to enter penArea with any other
		// id
		final int desired = getNumKeeper();
		
		if (baseAiFrame.getGamestate().isPenaltyShootout())
		{
			tacticalField.getPlayNumbers().put(EPlay.KEEPER_SHOOTOUT, desired);
		} else
		{
			tacticalField.getPlayNumbers().put(EPlay.KEEPER, desired);
		}
	}
	
	
	private int getNumKeeper()
	{
		final BaseAiFrame aiFrame = getAiFrame();
		final int desired;
		
		if (aiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(aiFrame.getKeeperId()))
		{
			desired = 1;
		} else
		{
			desired = 0;
		}
		
		return desired;
	}
	
	
	private void normalDefense(final TacticalField tacticalField, BaseAiFrame aiFrame)
	{
		int available = aiFrame.getWorldFrame().getTigerBotsVisible().size() - getNumKeeper();
		int numDesRoles = getNumDefenders(tacticalField);
		tacticalField.getPlayNumbers().put(EPlay.DEFENSIVE, Math.min(available, numDesRoles));
	}
	
	
	private void ballPlacementMode(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int available = aiFrame.getWorldFrame().getTigerBotsVisible().size() - getNumKeeper();
		
		int desired = 2;
		if (tacticalField.getThrowInInfo().isFinished())
		{
			desired = 0;
		}
		desired = Math.min(desired, available);
		
		tacticalField.getPlayNumbers().put(EPlay.AUTOMATED_THROW_IN, desired);
		tacticalField.getPlayNumbers().put(EPlay.DEFENSIVE, available - desired);
		tacticalField.getPlayNumbers().put(EPlay.SUPPORT, 0);
	}
	
	
	private void normalMode(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		normalDefense(tacticalField, aiFrame);
		
		OffensiveStrategy offensiveStrategy = tacticalField.getOffensiveStrategy();
		int nKeeper = getWFrame().getTigerBotsAvailable().containsKey(aiFrame.getKeeperId()) ? 1 : 0;
		int available = Math.max(0, aiFrame.getWorldFrame().getTigerBotsVisible().size() - nKeeper);
		int desiredBotsOffense = offensiveStrategy.getDesiredBots().size();
		
		if (isNoOffenseSpecialCase(tacticalField, aiFrame.getWorldFrame().getBall().getPos()))
		{
			desiredBotsOffense = 0;
			offensiveStrategy.getDesiredBots().clear();
		}
		
		desiredBotsOffense = Math.min(available, desiredBotsOffense);
		available -= desiredBotsOffense;
		
		tacticalField.getPlayNumbers().put(EPlay.OFFENSIVE, desiredBotsOffense);
		tacticalField.getPlayNumbers().put(EPlay.SUPPORT,
				Math.max(0, available));
	}
	
	
	private boolean isNoOffenseSpecialCase(TacticalField tacticalField, final IVector2 ballPos)
	{
		return tacticalField.getGameState().isStandardSituationForThem()
				&& Geometry.getPenaltyAreaOur().isPointInShape(ballPos, 1300);
	}
	
	
	private int getNumDefenders(final TacticalField tacticalField)
	{
		return tacticalField.getNumDefender();
	}
}
