/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics.calculators;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.MatchStatistics.EAvailableStatistic;
import edu.tigers.sumatra.ai.data.Percentage;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.statistics.AStats;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class TackleStats extends AStats
{
	private final Percentage					tackleGeneralWon	= new Percentage();
	private final Percentage					tackleGeneralLost	= new Percentage();
	private final Map<BotID, Percentage>	tackleWon			= new HashMap<BotID, Percentage>();
	private final Map<BotID, Percentage>	tackleLost			= new HashMap<BotID, Percentage>();
	private boolean								tackle				= false;
	/** Tiger BotID which is in a tackle */
	private BotID									tackleTiger			= null;
	private int										tackleCount			= 0;
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStatistics matchStatistics)
	{
		matchStatistics.setTackleGeneral(tackleGeneralWon, tackleGeneralLost);
		matchStatistics.setTackleLost(tackleLost);
		matchStatistics.setTackleWon(tackleWon);
		
		StatisticData tacklesWonStatistic = new StatisticData(tackleWon, tackleGeneralWon);
		matchStatistics.putStatisticData(EAvailableStatistic.TacklesWon, tacklesWonStatistic);
		
		StatisticData tacklesLostStatistic = new StatisticData(tackleLost, tackleGeneralLost);
		matchStatistics.putStatisticData(EAvailableStatistic.TacklesLost, tacklesLostStatistic);
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
				incrementTackleStatistics(tackleGeneralWon, tackleWon);
				break;
			case THEY:
			case NO_ONE:
				// tackle lost
				incrementTackleStatistics(tackleGeneralLost, tackleLost);
				break;
			default:
				tackle = false;
				break;
		}
		
		// update percentages
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
	
	
	private void incrementTackleStatistics(final Percentage percentageGeneralTackle,
			final Map<BotID, Percentage> percentagesSpecificBots)
	{
		if (tackle && (tackleTiger != null))
		{
			tackleCount++;
			percentageGeneralTackle.inc();
			if (!percentagesSpecificBots.containsKey(tackleTiger))
			{
				percentagesSpecificBots.put(tackleTiger, new Percentage());
			}
			percentagesSpecificBots.get(tackleTiger).inc();
			
			tackleTiger = null;
		}
		tackle = false;
	}
	
}
