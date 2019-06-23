/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.statistics;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.GameState;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * Calculates offensive Actions for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStatisticsPostAnalysisCalc extends ACalculator
{
	private long n = 0;
	private long desiredRolesSum = 0;
	private Map<BotID, Long> primaryBotSums = new HashMap<>();
	
	private Map<BotID, StatisticsData> botData = new HashMap<>();
	
	private class StatisticsData
	{
		Map<EOffensiveActionMove, Map<EActionViability, Long>> moveViabilitiesSums;
		Map<EOffensiveActionMove, Double> moveViabilitiyScoreSums;
		Map<EOffensiveAction, Long> activeActionMoveSums;
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
			activeActionMoveSums = new EnumMap<>(EOffensiveAction.class);
			for (EOffensiveAction key : EOffensiveAction.values())
			{
				activeActionMoveSums.put(key, 0L);
			}
			activeFeatureSums = new EnumMap<>(EOffensiveStrategy.class);
			for (EOffensiveStrategy strategy : EOffensiveStrategy.values())
			{
				activeFeatureSums.put(strategy, 0L);
			}
		}
	}
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		GameState gameState = tacticalField.getGameState();
		return OffensiveConstants.isEnableOffensiveStatistics()
				&& (gameState.isRunning() || gameState.isStandardSituationForUs());
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		updateData(newTacticalField);
		analyzeData(newTacticalField);
	}
	
	
	/**
	 * creates empty datasets. they get filled in the OffensiveActions and OffensiveStrategy Calculators.
	 * 
	 * @param newTacticalField
	 */
	private void updateData(final TacticalField newTacticalField)
	{
		n++;
		OffensiveStatisticsFrame frame = newTacticalField.getOffensiveStatistics();
		desiredRolesSum += frame.getDesiredNumBots();
		if ((frame.getPrimaryOffensiveBot() != null) && !primaryBotSums.containsKey(frame.getPrimaryOffensiveBot()))
		{
			primaryBotSums.put(frame.getPrimaryOffensiveBot(), 0L);
		} else if (frame.getPrimaryOffensiveBot() != null)
		{
			primaryBotSums.put(frame.getPrimaryOffensiveBot(), primaryBotSums.get(frame.getPrimaryOffensiveBot()) + 1L);
		}
		for (Map.Entry<BotID, OffensiveBotFrame> entry : frame.getBotFrames().entrySet())
		{
			if (!botData.containsKey(entry.getKey()))
			{
				botData.put(entry.getKey(), new StatisticsData());
			}
			OffensiveBotFrame bFrame = entry.getValue();
			StatisticsData currentData = botData.get(entry.getKey());
			
			if (bFrame == null || bFrame.getActiveAction() == null || currentData.activeActionMoveSums == null ||
					!currentData.activeActionMoveSums.containsKey(bFrame.getActiveAction()))
			{
				continue;
			}
			storeData(bFrame, currentData);
		}
	}
	
	
	private void storeData(final OffensiveBotFrame bFrame, final StatisticsData currentData)
	{
		currentData.activeActionMoveSums.put(bFrame.getActiveAction(),
				currentData.activeActionMoveSums.get(bFrame.getActiveAction()) + 1);
		
		if (bFrame.getActiveStrategy() != null)
		{
			final Long activeFeatureSum = currentData.activeFeatureSums
					.get(bFrame.getActiveStrategy());
			currentData.activeFeatureSums.put(bFrame.getActiveStrategy(),
					activeFeatureSum + 1);
		}
		
		currentData.n = currentData.n + 1;
		for (Map.Entry<EOffensiveActionMove, EActionViability> move : bFrame.getMoveViabilities().entrySet())
		{
			long val = currentData.moveViabilitiesSums.get(move.getKey()).get(move.getValue()) + 1;
			currentData.moveViabilitiesSums.get(move.getKey()).put(move.getValue(), val);
		}
		for (Map.Entry<EOffensiveActionMove, Double> move : bFrame.getMoveViabilityScores().entrySet())
		{
			double val = move.getValue() + currentData.moveViabilitiyScoreSums.get(move.getKey());
			currentData.moveViabilitiyScoreSums.put(move.getKey(), val);
		}
	}
	
	
	private void analyzeData(final TacticalField newTacticalField)
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
		newTacticalField.setAnalyzedOffensiveStatisticsFrame(frame);
	}
}
