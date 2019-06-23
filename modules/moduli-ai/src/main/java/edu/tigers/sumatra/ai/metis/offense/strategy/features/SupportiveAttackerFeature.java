/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy.features;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.data.TemporaryOffensiveInformation;
import edu.tigers.sumatra.ai.metis.offense.strategy.AOffensiveStrategyFeature;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * MarkG
 */
public class SupportiveAttackerFeature extends AOffensiveStrategyFeature
{
	/**
	 * Default
	 */
	public SupportiveAttackerFeature()
	{
		super();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		boolean isEnabledAndShouldAdd = OffensiveConstants.isSupportiveAttackerEnabled()
				&& shouldAddSupportiveAttacker(newTacticalField, baseAiFrame);
		boolean isSkirmish = newTacticalField.getSkirmishInformation().isSkirmishDetected();
		boolean isEnabledForNoSkirmishAndBallPossesionNotWe = OffensiveConstants.isEnableNoSkirmishSupportiveAttacker()
				&& newTacticalField.getBallPossession().getEBallPossession() != EBallPossession.WE;
		if ((isSkirmish
				|| isEnabledForNoSkirmishAndBallPossesionNotWe)
				&& isEnabledAndShouldAdd)
		{
			activateSupportiveAttacker(newTacticalField, baseAiFrame, tempInfo, strategy);
		}
	}
	
	
	private void activateSupportiveAttacker(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final TemporaryOffensiveInformation tempInfo, final OffensiveStrategy strategy)
	{
		IBotIDMap<ITrackedBot> bots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField,
				baseAiFrame);
		bots.remove(tempInfo.getPrimaryBot().getBotId());
		BotID secondBestGetter = OffensiveMath.getBestGetter(baseAiFrame, bots, newTacticalField);
		
		if (!strategy.getDesiredBots().contains(secondBestGetter))
		{
			strategy.getDesiredBots().add(secondBestGetter);
			strategy.getCurrentOffensivePlayConfiguration().put(secondBestGetter,
					OffensiveStrategy.EOffensiveStrategy.SUPPORTIVE_ATTACKER);
			strategy.setMaxNumberOfBots(strategy.getMaxNumberOfBots() + 1);
		}
	}
	
	
	private boolean shouldAddSupportiveAttacker(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		return newTacticalField.getGameState().isRunning()
				&& (baseAiFrame.getWorldFrame().getBall().getPos().x() > OffensiveConstants
						.getMinXPosForSupportiveAttacker());
	}
}
