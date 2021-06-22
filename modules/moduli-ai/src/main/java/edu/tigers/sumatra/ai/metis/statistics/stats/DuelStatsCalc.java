/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ids.BotID;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@RequiredArgsConstructor
public class DuelStatsCalc extends AStatsCalc
{
	private final Supplier<BallPossession> ballPossession;

	private int duelsGeneralWon = 0;
	private int duelsGeneralLost = 0;
	private final Map<Integer, Percentage> duelsWon = new HashMap<>();
	private final Map<Integer, Percentage> duelsLost = new HashMap<>();

	/**
	 * Tiger BotID which is in a tackle
	 */
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
	public void onStatisticUpdate(final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPossession = ballPossession.get();
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


	private int updateTackleStatistics(final Map<Integer, Percentage> percentagesSpecificBots, int tackleCount)
	{
		final int newTackleCount;
		if (tackle && (tackleTiger != null))
		{
			newTackleCount = tackleCount + 1;
			percentagesSpecificBots
					.computeIfAbsent(tackleTiger.getNumber(), tt -> new Percentage()).inc();
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
