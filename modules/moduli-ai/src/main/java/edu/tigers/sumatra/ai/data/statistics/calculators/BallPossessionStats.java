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
public class BallPossessionStats extends AStats
{
	private int													ballPossessionEventCount	= 0;
	/** Zweikampf flag, wird für folgende Events gesetzt und ausgewertet */
	private final Map<EBallPossession, Percentage>	ballPossessionGeneral		= new HashMap<EBallPossession, Percentage>();
	private final Map<BotID, Percentage>				ballPossessionOpponents		= new HashMap<BotID, Percentage>();
	private final Map<BotID, Percentage>				ballPossessionTigers			= new HashMap<BotID, Percentage>();
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStatistics matchStatistics)
	{
		matchStatistics.getBallPossessionGeneral().putAll(ballPossessionGeneral);
		matchStatistics.getBallPossessionOpponents().putAll(ballPossessionOpponents);
		
		StatisticData ballPossessionTigers = new StatisticData(this.ballPossessionTigers,
				ballPossessionGeneral.get(EBallPossession.WE));
		matchStatistics.putStatisticData(EAvailableStatistic.BallPossession, ballPossessionTigers);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		BallPossession curBallPossession = newTacticalField.getBallPossession();
		EBallPossession curEBallPosession = curBallPossession.getEBallPossession();
		
		// update ball Possession Counter
		ballPossessionEventCount++;
		
		// General Statistics over Ball Possesion
		if (!ballPossessionGeneral.containsKey(curEBallPosession))
		{
			ballPossessionGeneral.put(curEBallPosession, new Percentage());
		}
		ballPossessionGeneral.get(curEBallPosession).inc();
		
		// Update per bot ballpossession count
		switch (curEBallPosession)
		{
			case BOTH:
			{
				BotID tackleTiger = curBallPossession.getTigersId();
				
				if (!ballPossessionTigers.containsKey(tackleTiger))
				{
					ballPossessionTigers.put(tackleTiger, new Percentage());
				}
				ballPossessionTigers.get(tackleTiger).inc();
				BotID opponent = curBallPossession.getOpponentsId();
				if (!ballPossessionOpponents.containsKey(opponent))
				{
					ballPossessionOpponents.put(opponent, new Percentage());
				}
				ballPossessionOpponents.get(opponent).inc();
			}
				break;
			case WE:
			{
				BotID tigerSingle = curBallPossession.getTigersId();
				if (!ballPossessionTigers.containsKey(tigerSingle))
				{
					ballPossessionTigers.put(tigerSingle, new Percentage());
				}
				ballPossessionTigers.get(tigerSingle).inc();
			}
				break;
			case THEY:
				BotID opponentSingle = curBallPossession.getOpponentsId();
				if (!ballPossessionOpponents.containsKey(opponentSingle))
				{
					ballPossessionOpponents.put(opponentSingle, new Percentage());
				}
				ballPossessionOpponents.get(opponentSingle).inc();
				break;
			default:
				break;
		}
		
		updateBallPossessionPercentageWithMap(ballPossessionGeneral);
		updateBallPossessionPercentageWithMap(ballPossessionTigers);
		updateBallPossessionPercentageWithMap(ballPossessionOpponents);
	}
	
	
	private void updateBallPossessionPercentageWithMap(final Map<?, Percentage> containingMap)
	{
		for (Percentage ballPossessionCount : containingMap.values())
		{
			ballPossessionCount.setAll(ballPossessionEventCount);
		}
	}
}
