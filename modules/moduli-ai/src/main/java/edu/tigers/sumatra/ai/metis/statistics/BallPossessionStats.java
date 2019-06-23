/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.MatchStats.EMatchStatistics;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.Percentage;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class BallPossessionStats extends AStats
{
	private int ballPossessionEventCount = 0;
	private int ballPossessionEventCountBots = 0;
	private final Map<EBallPossession, Percentage> ballPossessionGeneral = new EnumMap<>(EBallPossession.class);
	private final Map<BotID, Percentage> ballPossessionPerBot = new HashMap<>();


	/**
	 * Default
	 */
	public BallPossessionStats()
	{
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
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPossession = newTacticalField.getBallPossession();
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
			ballPossessionPerBot.computeIfAbsent(tackleTiger, tt -> new Percentage()).inc();
			ballPossessionPerBot.values().forEach(a -> a.setAll(ballPossessionEventCountBots));
		}
	}
}
