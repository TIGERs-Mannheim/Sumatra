/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics.stats;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ids.BotID;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class BallPossessionStatsCalc extends AStatsCalc
{
	private int ballPossessionEventCount = 0;
	private int ballPossessionEventCountBots = 0;
	private final Map<EBallPossession, Percentage> ballPossessionGeneral = new EnumMap<>(EBallPossession.class);
	private final Map<Integer, Percentage> ballPossessionPerBot = new HashMap<>();

	private final Supplier<BallPossession> ballPossession;


	/**
	 * Default
	 *
	 * @param ballPossession
	 */
	public BallPossessionStatsCalc(
			Supplier<BallPossession> ballPossession)
	{
		this.ballPossession = ballPossession;
		Arrays.asList(EBallPossession.values()).forEach(b -> ballPossessionGeneral.put(b, new Percentage()));
	}


	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		matchStatistics.getBallPossessionGeneral().putAll(ballPossessionGeneral);

		StatisticData ballPossessionTigers = new StatisticData(
				this.ballPossessionPerBot,
				ballPossessionGeneral.get(EBallPossession.WE));
		matchStatistics.putStatisticData(EMatchStatistics.BALL_POSSESSION, ballPossessionTigers);
	}


	@Override
	public void onStatisticUpdate(final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPossession = ballPossession.get();
		EBallPossession curEBallPossession = curBallPossession.getEBallPossession();

		if (curEBallPossession == EBallPossession.NO_ONE)
		{
			return;
		}

		// update ball Possession Counter
		ballPossessionEventCount++;

		// General Statistics over Ball Possession
		ballPossessionGeneral.get(curEBallPossession).inc();
		ballPossessionGeneral.values().forEach(a -> a.setAll(ballPossessionEventCount));

		// Update per bot ball possession count
		if (curEBallPossession == EBallPossession.WE)
		{
			ballPossessionEventCountBots++;
			BotID tackleTiger = curBallPossession.getTigersId();
			ballPossessionPerBot.computeIfAbsent(tackleTiger.getNumber(), tt -> new Percentage()).inc();
			ballPossessionPerBot.values().forEach(a -> a.setAll(ballPossessionEventCountBots));
		}
	}
}
