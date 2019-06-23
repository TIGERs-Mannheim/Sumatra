/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Percentage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.statistics.PenaltyStats;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.statistics.PenaltyStats.PenaltyStatsComparator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * This calculator generates statistics for a game for:
 * - ball possession
 * - ball lost after zweikampf
 * - ball win after zweikampf
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsCalc extends ACalculator
{
	
	private int											ballPosEventCount			= 0;
	/** Zweikampf flag, wird für folgende Events gesetzt und ausgewertet */
	private Map<EBallPossession, Percentage>	ballPossessionGeneral;
	private Map<Integer, Percentage>				ballPossessionTigers;
	private Map<BotID, Percentage>				ballPossessionOpponents;
	
	private Percentage								tackleGeneralWon;
	private Percentage								tackleGeneralLost;
	private Map<BotID, Percentage>				tackleWon;
	private Map<BotID, Percentage>				tackleLost;
	private boolean									tackle						= false;
	/** Tiger BotID which is in a tackle */
	private BotID										tackleTiger					= null;
	private int											tackleCount					= 0;
	
	/** Possible Goal members */
	private Map<BotID, Percentage>				possibleBotGoals;
	private int											possibleTigersGoals		= 0;
	private int											possibleOpponentsGoals	= 0;
	private EPossibleGoal							lastPossibleGoalVal		= EPossibleGoal.NO_ONE;
	
	/** Information used for best penalty shooter */
	// Indicates that there was a penalty shot (is true as long as botLastTouched (from tacticalField) is same as penalty
	// shooter)
	private boolean									penalty						= false;
	private List<PenaltyStats>						bestPenaltyShooters;
	private boolean									penaltyFirstTouched		= false;
	private static PenaltyStatsComparator		penaltyStatsComparator	= new PenaltyStatsComparator();
	
	
	/**
	 */
	public StatisticsCalc()
	{
		ballPossessionGeneral = new HashMap<EBallPossession, Percentage>();
		ballPossessionTigers = new HashMap<Integer, Percentage>();
		ballPossessionOpponents = new HashMap<BotID, Percentage>();
		tackleWon = new HashMap<BotID, Percentage>();
		tackleLost = new HashMap<BotID, Percentage>();
		tackleGeneralWon = new Percentage();
		tackleGeneralLost = new Percentage();
		possibleBotGoals = new HashMap<BotID, Percentage>();
		bestPenaltyShooters = new LinkedList<PenaltyStats>();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getBots().isEmpty())
		{
			fallbackCalc(newTacticalField, baseAiFrame);
			return;
		}
		// If the game is not running, take the last values...
		if (newTacticalField.getGameState().equals(EGameState.PREPARE_PENALTY_WE))
		{
			penaltyModeStart(newTacticalField, baseAiFrame);
		}
		else if (!newTacticalField.getGameState().equals(EGameState.RUNNING))
		{
			if (penalty)
			{
				// If there is a best shooter decrease his score, as there is no goal
				if (!bestPenaltyShooters.isEmpty())
				{
					bestPenaltyShooters.get(0).addNotScoredGoal();
				}
				penalty = false;
				penaltyFirstTouched = false;
			}
			fallbackCalc(newTacticalField, baseAiFrame);
			return;
		}
		
		calcBallPossesionAndTackle(newTacticalField, baseAiFrame);
		
		calcPossibleGoals(newTacticalField.getPossibleGoal(), newTacticalField.getBotLastTouchedBall());
		
		calcBestPenaltyShooterBot(newTacticalField, baseAiFrame);
		
		fillDataPackage(newTacticalField);
	}
	
	
	/**
	 * Sets in tacticalField a statistics object with the last values. So the statistics object contains non-empty
	 * values.
	 */
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		fillDataPackage(newTacticalField);
	}
	
	
	private void calcBallPossesionAndTackle(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPos = newTacticalField.getBallPossession();
		EBallPossession curEBallPos = curBallPos.getEBallPossession();
		// update ball Possession Counter
		ballPosEventCount++;
		
		// General Statistics over Ball Possesion
		if (!ballPossessionGeneral.containsKey(curEBallPos))
		{
			ballPossessionGeneral.put(curEBallPos, new Percentage());
		}
		ballPossessionGeneral.get(curEBallPos).inc();
		
		// Check for tackle in previous event and if it was lost or won
		// TO DO Maybe use more intelligent tackle indicator for bots. In a tackle the bots can change, which is a lost
		// tackle and a new tackle. This is not implemented here
		switch (curEBallPos)
		{
			case BOTH:
				// tackle is still active
				tackle = true;
				break;
			case WE:
				// tackle won
				if (tackle && (tackleTiger != null))
				{
					tackleCount++;
					tackleGeneralWon.inc();
					if (!tackleWon.containsKey(tackleTiger))
					{
						tackleWon.put(tackleTiger, new Percentage());
					}
					tackleWon.get(tackleTiger).inc();
					
					tackleTiger = null;
				}
				tackle = false;
				break;
			case THEY:
			case NO_ONE:
				// tackle lost
				if (tackle && (tackleTiger != null))
				{
					tackleCount++;
					tackleGeneralLost.inc();
					if (!tackleLost.containsKey(tackleTiger))
					{
						tackleLost.put(tackleTiger, new Percentage());
					}
					tackleLost.get(tackleTiger).inc();
					tackleTiger = null;
				}
				tackle = false;
				break;
			default:
				tackle = false;
				break;
		}
		
		// Update per bot ballpossession count
		switch (curEBallPos)
		{
			case BOTH:
			{
				tackle = true;
				BotID tiger = curBallPos.getTigersId();
				tackleTiger = tiger;
				TrackedTigerBot tBot = baseAiFrame.getWorldFrame().getBot(tackleTiger);
				int tigerHwId = -1;
				if (tBot != null)
				{
					tigerHwId = tBot.getBot().getHardwareId();
				}
				if (!ballPossessionTigers.containsKey(tigerHwId))
				{
					ballPossessionTigers.put(tigerHwId, new Percentage());
				}
				ballPossessionTigers.get(tigerHwId).inc();
				BotID opponent = curBallPos.getOpponentsId();
				if (!ballPossessionOpponents.containsKey(opponent))
				{
					ballPossessionOpponents.put(opponent, new Percentage());
				}
				ballPossessionOpponents.get(opponent).inc();
			}
				break;
			case WE:
			{
				BotID tigerSingle = curBallPos.getTigersId();
				TrackedTigerBot tBotSingle = baseAiFrame.getWorldFrame().getBot(tigerSingle);
				int tigerSingleHwId = -1;
				if (tBotSingle != null)
				{
					tigerSingleHwId = tBotSingle.getBot().getHardwareId();
				}
				if (!ballPossessionTigers.containsKey(tigerSingleHwId))
				{
					ballPossessionTigers.put(tigerSingleHwId, new Percentage());
				}
				ballPossessionTigers.get(tigerSingleHwId).inc();
			}
				break;
			case THEY:
				BotID opponentSingle = curBallPos.getOpponentsId();
				if (!ballPossessionOpponents.containsKey(opponentSingle))
				{
					ballPossessionOpponents.put(opponentSingle, new Percentage());
				}
				ballPossessionOpponents.get(opponentSingle).inc();
				break;
			default:
				break;
		}
		
		
		// update percentages
		for (Percentage ballPosCount : ballPossessionGeneral.values())
		{
			ballPosCount.setAll(ballPosEventCount);
		}
		for (Percentage ballPosBot : ballPossessionTigers.values())
		{
			ballPosBot.setAll(ballPosEventCount);
		}
		for (Percentage ballPosOppBot : ballPossessionOpponents.values())
		{
			ballPosOppBot.setAll(ballPosEventCount);
		}
		for (Percentage tackleCounter : tackleWon.values())
		{
			tackleCounter.setAll(tackleCount);
		}
		for (Percentage tackleCounter : tackleLost.values())
		{
			tackleCounter.setAll(tackleCount);
		}
		tackleGeneralWon.setAll(tackleCount);
		tackleGeneralLost.setAll(tackleCount);
	}
	
	
	private void calcPossibleGoals(final EPossibleGoal ePossibleGoal, final BotID botLastTouched)
	{
		switch (ePossibleGoal)
		{
			case WE:
				if (ePossibleGoal == lastPossibleGoalVal)
				{
					break;
				}
				possibleTigersGoals++;
				if (!possibleBotGoals.containsKey(botLastTouched))
				{
					possibleBotGoals.put(botLastTouched, new Percentage());
				}
				possibleBotGoals.get(botLastTouched).inc();
				break;
			case THEY:
				if (ePossibleGoal == lastPossibleGoalVal)
				{
					break;
				}
				possibleOpponentsGoals++;
				if (!possibleBotGoals.containsKey(botLastTouched))
				{
					possibleBotGoals.put(botLastTouched, new Percentage());
				}
				possibleBotGoals.get(botLastTouched).inc();
				break;
			case NO_ONE:
				break;
		}
		lastPossibleGoalVal = ePossibleGoal;
	}
	
	
	private void calcBestPenaltyShooterBot(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!bestPenaltyShooters.isEmpty())
		{
			// First bot is always best shooter as the list is sorted.
			PenaltyStats bestShooter = bestPenaltyShooters.get(0);
			// state after penalty awarded (EGameState was PREPARE_PENALTY_WE, now RUNNING)
			if (penalty && newTacticalField.getGameState().equals(EGameState.RUNNING))
			{
				// Goal is only awarded if it has not touched another bot
				if (newTacticalField.getBotLastTouchedBall().equals(bestShooter.getBotID()))
				{
					penaltyFirstTouched = true;
					// If a possible goal occurs increase the score for Bot. Didn't uses Referee Goal, because it is delayed
					// through human interaction
					if (newTacticalField.getPossibleGoal().equals(EPossibleGoal.WE))
					{
						bestShooter.addScoredGoal();
						penalty = false;
						penaltyFirstTouched = false;
						bestPenaltyShooters.sort(penaltyStatsComparator);
						return;
					}
					// else wait until goal is awarded, gamestate changes or other bot touches ball
				}
				else
				{
					if (penaltyFirstTouched)
					{
						bestShooter.addNotScoredGoal();
						penalty = false;
						penaltyFirstTouched = false;
						bestPenaltyShooters.sort(penaltyStatsComparator);
						return;
					}
					// shooter has not touched the ball yet
				}
			}
			
		} else
		{
			penaltyInitBestShooter(newTacticalField, baseAiFrame);
			if (!bestPenaltyShooters.isEmpty())
			{
				calcBestPenaltyShooterBot(newTacticalField, baseAiFrame);
			}
		}
	}
	
	
	/**
	 * Init List for best shooters if it is empty
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	void penaltyInitBestShooter(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		for (BotID bot : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			// Do not add keeper to avoid that he shoots the penalty and has to be fast in our goal afterwards
			if (!bot.equals(baseAiFrame.getKeeperId()))
			{
				bestPenaltyShooters.add(new PenaltyStats(bot));
			}
		}
	}
	
	
	/**
	 * If Penalty is awarded to Tigers set Mode to Penalty (penalty=true) and add the RoleFinderInfo with best shooter
	 * 
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	void penaltyModeStart(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (bestPenaltyShooters.isEmpty())
		{
			penaltyInitBestShooter(newTacticalField, baseAiFrame);
		}
		if (!penalty)
		{
			// First bot is always best shooter as the list is sorted.
			assert bestPenaltyShooters.size() != 0;
			PenaltyStats bestShooter = bestPenaltyShooters.get(0);
			penalty = true;
			RoleFinderInfo penaltyInfo = newTacticalField.getRoleFinderInfos().get(EPlay.PENALTY_WE);
			if (penaltyInfo != null)
			{
				penaltyInfo.getDesiredBots().add(0, bestShooter.getBotID());
				// On penalty there is only one Bot allowed
				// penaltyInfo.setForceNumDesiredBots(1);
			}
		}
	}
	
	
	/**
	 * Fills the Data holder package with current values of this Calculator
	 * 
	 * @param newTacticalField
	 */
	private void fillDataPackage(final TacticalField newTacticalField)
	{
		// Fill Data Holder package
		Statistics stats = newTacticalField.getStatistics();
		stats.getBallPossessionGeneral().putAll(ballPossessionGeneral);
		stats.getBallPossessionOpponents().putAll(ballPossessionOpponents);
		stats.setBallPossessionTigers(ballPossessionTigers);
		stats.setTackleGeneral(tackleGeneralWon, tackleGeneralLost);
		stats.setTackleLost(tackleLost);
		stats.setTackleWon(tackleWon);
		stats.setPossibleTigersGoals(possibleTigersGoals);
		stats.setPossibleOpponentsGoals(possibleOpponentsGoals);
		stats.setPossibleBotGoals(possibleBotGoals);
		stats.setBestPenaltyShooterStats(bestPenaltyShooters);
	}
}
