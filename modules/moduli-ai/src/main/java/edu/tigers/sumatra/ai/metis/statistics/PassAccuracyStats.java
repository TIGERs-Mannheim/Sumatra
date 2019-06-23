/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.MatchStats.EMatchStatistics;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.statistics.Percentage;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * This class will calculate various statistics concerned with Passes
 *
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class PassAccuracyStats extends AStats
{
	
	private IPassTarget oldPassTarget = null;
	
	private boolean targetCounted = false;
	private int timeBeingPassTarget = 0;
	
	private Map<BotID, Integer> countBeingActivePassTarget = new HashMap<>();
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
		matchStatistics.putStatisticData(EMatchStatistics.RATIO_SUCCESFUL_PASSES, ratioSuccesfulPasses);
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
		
		IPassTarget newPassTarget = baseAiFrame.getPrevFrame().getAICom().getPassTarget();
		
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
				incrementEntryForBotIDInMap(newPassTarget.getBotId(), countBeingActivePassTarget);
			}
		}
		
		oldPassTarget = newPassTarget;
		
		passProcessor.processPossiblePass(baseAiFrame);
		
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
		
		private Map<BotID, Integer> countPasses = new HashMap<>();
		private Integer countPassesTotal = 0;
		
		private Map<BotID, Percentage> successfulPasses = new HashMap<>();
		private Percentage successfulPassesTotal = new Percentage();
		
		private BotID currentPassReceiver;
		private BotID kickingBot;
		
		
		private void processPossiblePass(final BaseAiFrame baseAiFrame)
		{
			Optional<IKickEvent> kickEvent = tacticalField.getKicking();
			
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
				IPassTarget passTarget = baseAiFrame.getPrevFrame().getAICom().getPassTarget();
				
				if (passTarget != null)
				{
					currentPassReceiver = passTarget.getBotId();
				}
			}
			
		}
		
		
		private void processActivePass()
		{
			final boolean passStillActive = kickingBot == tacticalField.getBotLastTouchedBall();
			
			if (!passStillActive)
			{
				countPassesTotal++;
				
				Integer totalPassesBot = countPasses.get(kickingBot);
				
				if (totalPassesBot == null)
				{
					totalPassesBot = 0;
				}
				
				totalPassesBot++;
				countPasses.put(kickingBot, totalPassesBot);
				
				if (currentPassReceiver == tacticalField.getBotLastTouchedBall())
				{
					processSuccessfulPass();
				}
				
				currentPassReceiver = null;
				passActive = false;
			}
		}
		
		
		private void processSuccessfulPass()
		{
			Percentage percentage = successfulPasses.get(kickingBot);
			
			if (percentage == null)
			{
				percentage = new Percentage();
			}
			
			percentage.setAll(countPasses.get(kickingBot));
			percentage.inc();
			
			successfulPasses.put(kickingBot, percentage);
			
			successfulPassesTotal.inc();
			successfulPassesTotal.setAll(countPassesTotal);
		}
		
	}
	
}
