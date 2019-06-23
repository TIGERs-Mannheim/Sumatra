/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats.EMatchStatistics;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.Percentage;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class DuelStats extends AStats
{
	private int duelsGeneralWon = 0;
	private int duelsGeneralLost = 0;
	private final Map<BotID, Percentage> duelsWon = new HashMap<>();
	private final Map<BotID, Percentage> duelsLost = new HashMap<>();
	
	/** Tiger BotID which is in a tackle */
	private BotID tackleTiger = null;
	private boolean tackle = false;
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		StatisticData tacklesWonStatistic = new StatisticData(duelsWon, duelsGeneralWon);
		matchStatistics.putStatisticData(EMatchStatistics.DUELS_WON, tacklesWonStatistic);
		
		StatisticData tacklesLostStatistic = new StatisticData(duelsLost, duelsGeneralLost);
		matchStatistics.putStatisticData(EMatchStatistics.DUELS_LOST, tacklesLostStatistic);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPossession = newTacticalField.getBallPossession();
		EBallPossession curEBallPosession = curBallPossession.getEBallPossession();
		
		// Check for tackle in previous event and if it was lost or won
		// TO DO Maybe use more intelligent tackle indicator for bots. In a tackle the bots can change, which is a lost
		// tackle and a new tackle. This is not implemented here
		switch (curEBallPosession)
		{
			case BOTH:
				// tackle is still active
				tackleTiger = curBallPossession.getTigersId();
				
				tackle = true;
				break;
			case WE:
				// tackle won
				duelsGeneralWon = updateTackleStatistics(duelsWon, duelsGeneralWon);
				break;
			case THEY:
			case NO_ONE:
				// tackle lost
				duelsGeneralLost = updateTackleStatistics(duelsLost, duelsGeneralLost);
				break;
			default:
				tackle = false;
				break;
		}
	}
	
	
	private int updateTackleStatistics(final Map<BotID, Percentage> percentagesSpecificBots, int tackleCount)
	{
		final int newTackleCount;
		if (tackle && (tackleTiger != null))
		{
			newTackleCount = tackleCount + 1;
			percentagesSpecificBots
					.computeIfAbsent(tackleTiger, tt -> new Percentage()).inc();
			percentagesSpecificBots.values().forEach(p -> p.setAll(newTackleCount));
			
			tackleTiger = null;
		} else
		{
			newTackleCount = tackleCount;
		}
		tackle = false;
		return newTackleCount;
	}
	
}
