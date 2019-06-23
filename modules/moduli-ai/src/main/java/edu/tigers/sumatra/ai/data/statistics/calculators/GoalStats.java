/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 23, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.Percentage;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.statistics.AStats;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class GoalStats extends AStats
{
	private final Map<BotID, Percentage>	possibleBotGoals			= new HashMap<BotID, Percentage>();
	private int										possibleTigersGoals		= 0;
	private int										possibleOpponentsGoals	= 0;
	private EPossibleGoal						lastPossibleGoalVal		= EPossibleGoal.NO_ONE;
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStatistics matchStatistics)
	{
		matchStatistics.setPossibleTigersGoals(possibleTigersGoals);
		matchStatistics.setPossibleOpponentsGoals(possibleOpponentsGoals);
		
		// StatisticData possibleGoalsTigersStats = new StatisticData(possibleBotGoals, new Integer(possibleTigersGoals));
		// matchStatistics.putStatisticData(EAvailableStatistic.GoalsScored, possibleGoalsTigersStats);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		EPossibleGoal ePossibleGoal = newTacticalField.getPossibleGoal();
		
		BotID botLastTouched = newTacticalField.getBotLastTouchedBall();
		
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
}
