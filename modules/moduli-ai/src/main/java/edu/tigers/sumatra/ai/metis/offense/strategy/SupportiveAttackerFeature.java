/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import java.util.Optional;
import java.util.Set;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Activate the supportive attacker if required
 */
public class SupportiveAttackerFeature extends AOffensiveStrategyFeature
{
	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		boolean isEnabledAndShouldAdd = OffensiveConstants.isSupportiveAttackerEnabled()
				&& shouldAddSupportiveAttacker(newTacticalField);
		boolean isSkirmish = newTacticalField.getSkirmishInformation().isSkirmishDetected();
		boolean isEnabledForNoSkirmishAndBallPossesionNotWe = OffensiveConstants.isEnableNoSkirmishSupportiveAttacker()
				&& newTacticalField.getBallPossession().getEBallPossession() != EBallPossession.WE;
		if ((isSkirmish
				|| isEnabledForNoSkirmishAndBallPossesionNotWe)
				&& isEnabledAndShouldAdd)
		{
			activateSupportiveAttacker(newTacticalField, strategy);
		}
	}
	
	
	private void activateSupportiveAttacker(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		Set<BotID> bots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, getAiFrame()).keySet();
		bots.removeAll(strategy.getDesiredBots());
		
		Optional<BotID> nextClosestBotToBall = newTacticalField.getTigersToBallDist().stream()
				.map(BotDistance::getBot)
				.map(ITrackedBot::getBotId)
				.filter(bots::contains)
				.findFirst();
		
		if (nextClosestBotToBall.isPresent())
		{
			strategy.addDesiredBot(nextClosestBotToBall.get());
			strategy.putPlayConfiguration(nextClosestBotToBall.get(),
					EOffensiveStrategy.SUPPORTIVE_ATTACKER);
		}
	}
	
	
	private boolean shouldAddSupportiveAttacker(final TacticalField newTacticalField)
	{
		return newTacticalField.getGameState().isRunning()
				&& (getBall().getPos().x() > OffensiveConstants.getMinXPosForSupportiveAttacker());
	}
}
