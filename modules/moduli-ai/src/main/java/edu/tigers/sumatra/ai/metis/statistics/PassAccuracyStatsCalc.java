/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * This class will calculate various statistics concerned with Passes
 *
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class PassAccuracyStatsCalc extends AStatsCalc
{
	
	private IPassTarget oldPassTarget = null;
	
	private boolean targetCounted = false;
	private int timeBeingPassTarget = 0;
	
	private Map<Integer, Integer> countBeingActivePassTarget = new HashMap<>();
	private Integer countActivePassTargetsGeneral = 0;
	
	private ITacticalField tacticalField;
	
	private PassProcessor passProcessor = new PassProcessor();
	
	
	@Override
	public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
	{
		StatisticData countActivePassTargets = new StatisticData(countBeingActivePassTarget,
				countActivePassTargetsGeneral);
		matchStatistics.putStatisticData(EMatchStatistics.ACTIVE_PASS_TARGET, countActivePassTargets);
		
		StatisticData countTotalPasses = new StatisticData(passProcessor.countPasses, passProcessor.countPassesTotal);
		matchStatistics.putStatisticData(EMatchStatistics.COUNT_PASSES, countTotalPasses);
		
		StatisticData ratioSuccesfulPasses = new StatisticData(passProcessor.successfulPasses,
				passProcessor.successfulPassesTotal);
		matchStatistics.putStatisticData(EMatchStatistics.RATIO_SUCCESSFUL_PASSES, ratioSuccesfulPasses);
	}
	
	
	@Override
	public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		
		/*
		 * The general flow of this should be something like:
		 * - Is a pass played?
		 * - If no -> do nothing
		 * - If yes -> to whom is it played?
		 * - Is the ball controlled?
		 * - Is the receiving bot the intended target?
		 */
		
		tacticalField = newTacticalField;
		
		IPassTarget newPassTarget = newTacticalField.getOffensiveStrategy().getActivePassTarget().orElse(null);
		
		if ((oldPassTarget != newPassTarget) && (newPassTarget != null))
		{
			timeBeingPassTarget = 0;
			targetCounted = false;
		} else if (newPassTarget != null)
		{
			long timeBetweenFrames = getTimeBetweenFrames(baseAiFrame);
			
			timeBeingPassTarget += timeBetweenFrames;
			
			if (isNewActivePassTarget())
			{
				targetCounted = true;
				countActivePassTargetsGeneral++;
				incrementEntryForBotIDInMap(newPassTarget.getBotId().getNumber(), countBeingActivePassTarget);
			}
		}
		
		oldPassTarget = newPassTarget;
		
		passProcessor.processPossiblePass(baseAiFrame, newTacticalField);
		
	}
	
	
	private long getTimeBetweenFrames(final BaseAiFrame baseAiFrame)
	{
		return baseAiFrame.getWorldFrame().getTimestamp()
				- baseAiFrame.getPrevFrame().getWorldFrame().getTimestamp();
	}
	
	
	private boolean isNewActivePassTarget()
	{
		final int timeThreshold = (int) (0.5 * 1e9);
		return (timeBeingPassTarget > timeThreshold) && (!targetCounted);
	}
	
	
	private class PassProcessor
	{
		private boolean passActive = false;
		
		private Map<Integer, Integer> countPasses = new HashMap<>();
		private Integer countPassesTotal = 0;
		
		private Map<Integer, Percentage> successfulPasses = new HashMap<>();
		private Percentage successfulPassesTotal = new Percentage();
		
		private BotID currentPassReceiver;
		private BotID kickingBot;
		
		
		private void processPossiblePass(final BaseAiFrame baseAiFrame, final TacticalField newTacticalField)
		{
			Optional<IKickEvent> kickEvent = baseAiFrame.getWorldFrame().getKickEvent();
			
			boolean ourBotIsKicking = false;
			
			if (kickEvent.isPresent() && !passActive)
			{
				kickingBot = kickEvent.get().getKickingBot();
				
				ourBotIsKicking = kickingBot.getTeamColor().equals(baseAiFrame.getTeamColor());
			}
			
			
			if (ourBotIsKicking && currentPassReceiver != null)
			{
				passActive = true;
			}
			
			
			if (passActive)
			{
				processActivePass();
			} else
			{
				newTacticalField.getOffensiveStrategy().getActivePassTarget()
						.ifPresent(p -> currentPassReceiver = p.getBotId());
			}
		}
		
		
		private void processActivePass()
		{
			final boolean passStillActive = tacticalField.getBotsLastTouchedBall().contains(kickingBot);
			
			if (!passStillActive)
			{
				countPassesTotal++;
				
				Integer totalPassesBot = countPasses.get(kickingBot.getNumber());
				
				if (totalPassesBot == null)
				{
					totalPassesBot = 0;
				}
				
				totalPassesBot++;
				countPasses.put(kickingBot.getNumber(), totalPassesBot);
				
				if (tacticalField.getBotsLastTouchedBall().contains(currentPassReceiver))
				{
					processSuccessfulPass();
				}
				
				currentPassReceiver = null;
				passActive = false;
			}
		}
		
		
		private void processSuccessfulPass()
		{
			Percentage percentage = successfulPasses.get(kickingBot.getNumber());
			
			if (percentage == null)
			{
				percentage = new Percentage();
			}
			
			percentage.setAll(countPasses.get(kickingBot.getNumber()));
			percentage.inc();
			
			successfulPasses.put(kickingBot.getNumber(), percentage);
			
			successfulPassesTotal.inc();
			successfulPassesTotal.setAll(countPassesTotal);
		}
		
	}
	
}
