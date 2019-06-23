/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;


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
	private Map<EStatistics, AStats> statisticsSubscriber;
	
	
	/**
	 * Default
	 */
	public StatisticsCalc()
	{
		statisticsSubscriber = new EnumMap<>(EStatistics.class);
		
		statisticsSubscriber.put(EStatistics.BALL_POSSESSION, new BallPossessionStats());
		statisticsSubscriber.put(EStatistics.GOAL, new GoalStats());
		statisticsSubscriber.put(EStatistics.PASS_ACCURACY, new PassAccuracyStats());
		statisticsSubscriber.put(EStatistics.ROLE_TIME, new RoleTimeStats());
		statisticsSubscriber.put(EStatistics.TACKLE, new DuelStats());
		statisticsSubscriber.put(EStatistics.DIRECT_SHOT, new DirectShotsStats());
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!baseAiFrame.getWorldFrame().getBots().isEmpty() && !baseAiFrame.getGamestate().isIdleGame())
		{
			updateStatistics(newTacticalField, baseAiFrame);
		}
		
		createMatchStatistics(newTacticalField);
	}
	
	
	private void updateStatistics(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		for (AStats statisticToUpdate : statisticsSubscriber.values())
		{
			statisticToUpdate.onStatisticUpdate(newTacticalField, baseAiFrame);
		}
	}
	
	
	private void createMatchStatistics(final TacticalField newTacticalField)
	{
		MatchStats matchStatistics = newTacticalField.getMatchStatistics();
		
		for (AStats statisticsToReceiveDataFrom : statisticsSubscriber.values())
		{
			statisticsToReceiveDataFrom.saveStatsToMatchStatistics(matchStatistics);
		}
	}
}
