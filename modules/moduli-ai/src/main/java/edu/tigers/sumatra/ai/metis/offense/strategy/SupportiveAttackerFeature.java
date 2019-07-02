/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Activate the supportive attacker if required
 */
public class SupportiveAttackerFeature extends AOffensiveStrategyFeature
{

	@Configurable(defValue = "500.0")
	private static double marginEnemyPenAreaToDeactivateSupportiveAttacker = 500.0;

	@Configurable(defValue = "true")
	private static boolean activateSupportiveInterceptor = true;


	@Override
	public void doCalc(final TacticalField newTacticalField, final OffensiveStrategy strategy)
	{
		boolean isEnabledAndShouldAdd = isEnabledAndShouldAddSupportiveAttacker(newTacticalField);
		boolean isEnabledForNoSkirmishAndBallPossessionNotWe = isAddNonSkirmishSupportiveAttacker(newTacticalField);

		RedirectorDetectionInformation rInfo = newTacticalField.getRedirectorDetectionInformation();
		boolean additionalInterceptorNeeded = isAdditionalInterceptorNeeded(rInfo);

		if (isEnabledAndShouldAdd && additionalInterceptorNeeded && activateSupportiveInterceptor)
		{
			activateSupportiveAttacker(newTacticalField, strategy, rInfo.getEnemyReceiverPos());
			return;
		}

		boolean isSkirmish = newTacticalField.getSkirmishInformation().isSkirmishDetected();
		if ((isSkirmish || isEnabledForNoSkirmishAndBallPossessionNotWe)
				&& isEnabledAndShouldAdd)
		{
			activateSupportiveAttacker(newTacticalField, strategy, getBall().getPos());
		}
	}


	private boolean isAdditionalInterceptorNeeded(final RedirectorDetectionInformation rInfo)
	{
		return rInfo.getRecommendedAction() == ERecommendedReceiverAction.DISRUPT_ENEMY ||
				rInfo.getRecommendedAction() == ERecommendedReceiverAction.DOUBLE_ATTACKER;
	}


	private boolean isAddNonSkirmishSupportiveAttacker(final TacticalField newTacticalField)
	{
		return OffensiveConstants.isEnableNoSkirmishSupportiveAttacker()
				&& newTacticalField.getBallPossession().getEBallPossession() != EBallPossession.WE;
	}


	private boolean isEnabledAndShouldAddSupportiveAttacker(final TacticalField newTacticalField)
	{
		return OffensiveConstants.isSupportiveAttackerEnabled()
				&& shouldAddSupportiveAttacker(newTacticalField)
				&& Geometry.getField().isPointInShape(getBall().getPos());
	}


	private void activateSupportiveAttacker(final TacticalField newTacticalField, final OffensiveStrategy strategy,
			final IVector2 pos)
	{
		Set<BotID> bots = new HashSet<>(newTacticalField.getPotentialOffensiveBots());
		bots.removeAll(strategy.getDesiredBots());
		Optional<BotID> attacker = strategy.getAttackerBot();
		attacker.ifPresent(bots::remove);

		Optional<BotID> nextClosestBotToTarget = getWFrame().getTigerBotsAvailable().values().stream()
				.filter(e -> bots.contains(e.getBotId()))
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(pos)))
				.map(ITrackedBot::getBotId);

		if (nextClosestBotToTarget.isPresent())
		{
			strategy.addDesiredBot(nextClosestBotToTarget.get());
			strategy.putPlayConfiguration(nextClosestBotToTarget.get(),
					EOffensiveStrategy.SUPPORTIVE_ATTACKER);
		}
	}


	private boolean shouldAddSupportiveAttacker(final TacticalField newTacticalField)
	{
		return newTacticalField.getGameState().isRunning()
				&& (getBall().getPos().x() > OffensiveConstants.getMinXPosForSupportiveAttacker())
				&& !Geometry.getPenaltyAreaTheir().withMargin(marginEnemyPenAreaToDeactivateSupportiveAttacker)
						.isPointInShape(getBall().getPos());
	}
}
