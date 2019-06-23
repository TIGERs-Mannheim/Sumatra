/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAnalysedBotFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveBotFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.GameState;
import org.apache.log4j.Logger;

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
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger log = Logger
			.getLogger(OffensiveStrategyCalc.class.getName());
	
	private long n = 0;
	private long minRolesSum = 0;
	private long maxRolesSum = 0;
	private long desiredRolesSum = 0;
	private Map<BotID, Long> primaryBotSums = new HashMap<>();
	
	private Map<BotID, StatisticsData> botData = new HashMap<>();
	
	private class StatisticsData
	{
		Map<EOffensiveActionMove, Map<EActionViability, Long>> moveViabilitiesSums = null;
		Map<EOffensiveActionMove, Double> moveViabilitiyScoreSums = null;
		Map<OffensiveAction.EOffensiveAction, Long> activeActionMoveSums = null;
		Map<OffensiveStrategy.EOffensiveStrategy, Long> activeFeatureSums = null;
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
			activeActionMoveSums = new EnumMap<>(OffensiveAction.EOffensiveAction.class);
			for (OffensiveAction.EOffensiveAction key : OffensiveAction.EOffensiveAction.values())
			{
				activeActionMoveSums.put(key, 0L);
			}
			activeFeatureSums = new EnumMap<>(OffensiveStrategy.EOffensiveStrategy.class);
			for (OffensiveStrategy.EOffensiveStrategy strategy : OffensiveStrategy.EOffensiveStrategy.values())
			{
				activeFeatureSums.put(strategy, 0L);
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------- ------------------------------------
	
	
	public OffensiveStatisticsPostAnalysisCalc()
	{
		// nothing to do here yet.
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		GameState gameState = newTacticalField.getGameState();
		
		if (OffensiveConstants.isEnableOffensiveStatistics()
				&& (gameState.isRunning() || gameState.isStandardSituationForUs()))
		{
			updateData(newTacticalField);
			analyzeData(newTacticalField);
		}
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
		maxRolesSum += frame.getMaxNumBots();
		minRolesSum += frame.getMinNumBots();
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
			currentData.activeActionMoveSums.put(bFrame.getActiveAction(),
					currentData.activeActionMoveSums.get(bFrame.getActiveAction()) + 1);
			currentData.activeFeatureSums.put(bFrame.getActiveStrategy(),
					currentData.activeFeatureSums.get(bFrame.getActiveStrategy()) + 1);
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
	}
	
	
	private void analyzeData(final TacticalField newTacticalField)
	{
		OffensiveAnalysedFrame frame = new OffensiveAnalysedFrame();
		frame.setAvgDesiredRoles(desiredRolesSum / (double) n);
		frame.setAvgMaxRoles(maxRolesSum / (double) n);
		frame.setAvgMinRoles(minRolesSum / (double) n);
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
