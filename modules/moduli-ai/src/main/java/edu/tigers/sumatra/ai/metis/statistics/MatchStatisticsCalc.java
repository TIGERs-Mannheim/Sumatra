/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.ArrayList;
import java.util.Collection;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;


/**
 * This calculator generates statistics for a game.
 */
public class MatchStatisticsCalc extends ACalculator
{
	private Collection<AStatsCalc> statisticsSubscriber = new ArrayList<>();
	
	
	public MatchStatisticsCalc()
	{
		statisticsSubscriber.add(new BallPossessionStatsCalc());
		statisticsSubscriber.add(new GoalStatsCalc());
		statisticsSubscriber.add(new PassAccuracyStatsCalc());
		statisticsSubscriber.add(new RoleTimeStatsCalc());
		statisticsSubscriber.add(new DuelStatsCalc());
		statisticsSubscriber.add(new DirectShotsStatsCalc());
		statisticsSubscriber.add(new DefenseCoverageStatsCalc());
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getBots().isEmpty() || !baseAiFrame.getGamestate().isGameRunning())
		{
			createMatchStatistics();
			return;
		}
		
		updateStatistics();
		createMatchStatistics();
	}
	
	
	private void updateStatistics()
	{
		for (AStatsCalc statisticToUpdate : statisticsSubscriber)
		{
			statisticToUpdate.onStatisticUpdate(getNewTacticalField(), getAiFrame());
		}
	}
	
	
	private void createMatchStatistics()
	{
		MatchStats matchStatistics = getNewTacticalField().getMatchStatistics();
		for (AStatsCalc statisticsToReceiveDataFrom : statisticsSubscriber)
		{
			statisticsToReceiveDataFrom.saveStatsToMatchStatistics(matchStatistics);
		}
	}
}
