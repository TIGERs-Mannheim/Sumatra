/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.statistics.AStats;
import edu.tigers.sumatra.ai.data.statistics.EStatistics;
import edu.tigers.sumatra.ai.data.statistics.calculators.BallPossessionStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.GoalStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.PassAccuracyStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.PenaltyStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.RoleChangeStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.RoleTimeStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.TackleStats;
import edu.tigers.sumatra.ai.data.statistics.calculators.PenaltyStats.PenaltyStatsComparator;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;


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
	private Map<EStatistics, AStats>			statisticsSubscriber;
	
	/** Information used for best penalty shooter */
	// Indicates that there was a penalty shot (is true as long as botLastTouched (from tacticalField) is same as penalty
	// shooter)
	private boolean								penalty						= false;
	private final List<PenaltyStats>			bestPenaltyShooters;
	private boolean								penaltyFirstTouched		= false;
	private static PenaltyStatsComparator	penaltyStatsComparator	= new PenaltyStatsComparator();
	
	
	/**
	 */
	public StatisticsCalc()
	{
		statisticsSubscriber = new ConcurrentHashMap<>();
		
		statisticsSubscriber.put(EStatistics.BallPossession, new BallPossessionStats());
		statisticsSubscriber.put(EStatistics.Goal, new GoalStats());
		statisticsSubscriber.put(EStatistics.PassAccuracy, new PassAccuracyStats());
		statisticsSubscriber.put(EStatistics.RoleTime, new RoleTimeStats());
		statisticsSubscriber.put(EStatistics.RoleChange, new RoleChangeStats());
		statisticsSubscriber.put(EStatistics.Tackle, new TackleStats());
		
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
		if (newTacticalField.getGameState().equals(EGameStateTeam.PREPARE_PENALTY_WE))
		{
			penaltyModeStart(newTacticalField, baseAiFrame);
		}
		else if (!newTacticalField.getGameState().equals(EGameStateTeam.RUNNING))
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
		
		calcBestPenaltyShooterBot(newTacticalField, baseAiFrame);
		
		fillDataPackage(newTacticalField);
		
		updateStatistics(newTacticalField, baseAiFrame);
		
		createMatchStatistics(newTacticalField);
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
	
	
	private void calcBestPenaltyShooterBot(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!bestPenaltyShooters.isEmpty())
		{
			// First bot is always best shooter as the list is sorted.
			PenaltyStats bestShooter = bestPenaltyShooters.get(0);
			// state after penalty awarded (EGameState was PREPARE_PENALTY_WE, now RUNNING)
			if (penalty && newTacticalField.getGameState().equals(EGameStateTeam.RUNNING))
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
	
	
	private void updateStatistics(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		for (AStats statisticToUpdate : statisticsSubscriber.values())
		{
			statisticToUpdate.onStatisticUpdate(newTacticalField, baseAiFrame);
		}
	}
	
	
	private MatchStatistics createMatchStatistics(final TacticalField newTacticalField)
	{
		MatchStatistics matchStatistics = newTacticalField.getStatistics();
		
		for (AStats statisticsToReceiveDataFrom : statisticsSubscriber.values())
		{
			statisticsToReceiveDataFrom.saveStatsToMatchStatistics(matchStatistics);
		}
		
		return matchStatistics;
	}
	
	
	/**
	 * Fills the Data holder package with current values of this Calculator
	 * 
	 * @param newTacticalField
	 */
	private void fillDataPackage(final TacticalField newTacticalField)
	{
		// Fill Data Holder package
		MatchStatistics stats = newTacticalField.getStatistics();
		stats.setBestPenaltyShooterStats(bestPenaltyShooters);
	}
}
