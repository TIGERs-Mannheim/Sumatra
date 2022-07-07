/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.GameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Calculates offensive Actions for the OffenseRole.
 */
@RequiredArgsConstructor
public class OffensiveStatisticsPostAnalysisCalc extends ACalculator
{
	private long n = 0;
	private long desiredRolesSum = 0;
	private Map<BotID, Long> primaryBotSums = new HashMap<>();

	private Map<BotID, StatisticsData> botData = new HashMap<>();

	private final Supplier<OffensiveStatisticsFrame> offensiveStatistics;

	@Getter
	private OffensiveAnalysedFrame analyzedOffensiveStatisticsFrame;


	@Override
	public boolean isCalculationNecessary()
	{
		GameState gameState = getAiFrame().getGameState();
		return OffensiveConstants.isEnableOffensiveStatistics()
				&& (gameState.isRunning() || gameState.isStandardSituationForUs());
	}


	@Override
	public void doCalc()
	{
		updateData();
		analyzedOffensiveStatisticsFrame = analyzeData();
	}


	/**
	 * creates empty datasets. they get filled in the OffensiveActions and OffensiveStrategy Calculators.
	 */
	private void updateData()
	{
		OffensiveStatisticsFrame frame = offensiveStatistics.get();
		if (frame == null)
		{
			return;
		}
		n++;

		handleDesiredRolesAndPrimaryBot(frame);
		handleBotsAndActionMoves(frame);
	}


	private void handleBotsAndActionMoves(OffensiveStatisticsFrame frame)
	{
		for (Map.Entry<BotID, OffensiveBotFrame> entry : frame.getBotFrames().entrySet())
		{
			if (!botData.containsKey(entry.getKey()))
			{
				botData.put(entry.getKey(), new StatisticsData());
			}
			OffensiveBotFrame bFrame = entry.getValue();
			StatisticsData currentData = botData.get(entry.getKey());

			storeData(bFrame, currentData);
		}
	}


	private void handleDesiredRolesAndPrimaryBot(OffensiveStatisticsFrame frame)
	{
		desiredRolesSum += frame.getDesiredNumBots();
		if ((frame.getPrimaryOffensiveBot() != null) && !primaryBotSums.containsKey(frame.getPrimaryOffensiveBot()))
		{
			primaryBotSums.put(frame.getPrimaryOffensiveBot(), 0L);
		} else if (frame.getPrimaryOffensiveBot() != null)
		{
			primaryBotSums.put(frame.getPrimaryOffensiveBot(), primaryBotSums.get(frame.getPrimaryOffensiveBot()) + 1L);
		}
	}


	private void storeData(final OffensiveBotFrame bFrame, final StatisticsData currentData)
	{
		if (bFrame.getActiveStrategy() != null)
		{
			final Long activeFeatureSum = currentData.activeFeatureSums
					.get(bFrame.getActiveStrategy());
			currentData.activeFeatureSums.put(bFrame.getActiveStrategy(),
					activeFeatureSum + 1);
		}

		currentData.n = currentData.n + 1;
		for (var move : bFrame.getMoveViabilityMap().entrySet())
		{
			var actionMove = move.getKey();
			var viability = move.getValue();

			var typeSums = currentData.moveViabilitiesSums.get(actionMove);
			typeSums.merge(viability.getType(), 1L, (k, v) -> v + 1);

			var scoreSums = currentData.moveViabilitiyScoreSums;
			scoreSums.merge(actionMove, viability.getScore(), (k, v) -> v + viability.getScore());
		}
	}


	private OffensiveAnalysedFrame analyzeData()
	{
		OffensiveAnalysedFrame frame = new OffensiveAnalysedFrame();
		frame.setAvgDesiredRoles(desiredRolesSum / (double) n);
		for (Map.Entry<BotID, StatisticsData> data : botData.entrySet())
		{
			OffensiveAnalysedBotFrame botFrame = new OffensiveAnalysedBotFrame();
			long size = data.getValue().n;

			// fill data here
			Map<EOffensiveActionMove, Map<EActionViability, Double>> moveVias = new EnumMap<>(EOffensiveActionMove.class);
			for (Map.Entry<EOffensiveActionMove, Map<EActionViability, Long>> entry : data.getValue().moveViabilitiesSums
					.entrySet())
			{
				Map<EActionViability, Double> innerVias = new EnumMap<>(EActionViability.class);
				for (Map.Entry<EActionViability, Long> e : entry.getValue().entrySet())
				{
					innerVias.put(e.getKey(), e.getValue() / (double) size);
				}
				moveVias.put(entry.getKey(), innerVias);
			}
			for (Map.Entry<EOffensiveActionMove, Double> entry : data.getValue().moveViabilitiyScoreSums.entrySet())
			{
				botFrame.getMoveViabilitiyScoreAvg().put(entry.getKey(), entry.getValue() / (size));
			}
			botFrame.setMoveViabilitiesAvg(moveVias);
			frame.getBotFrames().put(data.getKey(), botFrame);
		}
		for (Map.Entry<BotID, Long> entry : primaryBotSums.entrySet())
		{
			double percentage = entry.getValue() / (double) n;
			frame.getPrimaryPercantages().put(entry.getKey(), percentage);
		}
		return frame;
	}


	private static class StatisticsData
	{
		Map<EOffensiveActionMove, Map<EActionViability, Long>> moveViabilitiesSums;
		Map<EOffensiveActionMove, Double> moveViabilitiyScoreSums;
		Map<EOffensiveStrategy, Long> activeFeatureSums;
		long n = 0;


		StatisticsData()
		{
			moveViabilitiyScoreSums = new EnumMap<>(EOffensiveActionMove.class);
			// init all fields.
			Map<EOffensiveActionMove, Map<EActionViability, Long>> tmp2 = new EnumMap<>(EOffensiveActionMove.class);
			for (EOffensiveActionMove key : EOffensiveActionMove.values())
			{
				Map<EActionViability, Long> tmp = new EnumMap<>(EActionViability.class);
				for (EActionViability via : EActionViability.values())
				{
					tmp.put(via, 0L);
				}
				tmp2.put(key, tmp);
				moveViabilitiyScoreSums.put(key, 0.0);
			}
			moveViabilitiesSums = tmp2;
			activeFeatureSums = new EnumMap<>(EOffensiveStrategy.class);
			for (EOffensiveStrategy strategy : EOffensiveStrategy.values())
			{
				activeFeatureSums.put(strategy, 0L);
			}
		}
	}
}
