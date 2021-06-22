/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Activate the supportive attacker if required
 */
@RequiredArgsConstructor
public class SupportiveAttackerCalc extends ACalculator
{
	@Configurable(defValue = "500.0")
	private static double marginOpponentPenAreaToDeactivateSupportiveAttacker = 500.0;

	@Configurable(defValue = "true")
	private static boolean activateSupportiveInterceptor = true;

	@Configurable(comment = "enable supportive Attacker", defValue = "true")
	private static boolean enableSupportiveAttacker = true;

	@Configurable(comment = "time in seconds", defValue = "-2500.0")
	private static double minXPosForSupportiveAttacker = -2500;

	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<RedirectorDetectionInformation> redirectorDetectionInformation;
	private final Supplier<Set<BotID>> potentialOffensiveBots;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<IVector2> supportiveAttackerPos;

	@Getter
	private List<BotID> supportiveAttackers;


	@Override
	protected boolean isCalculationNecessary()
	{
		return enableSupportiveAttacker && shouldAddSupportiveAttacker();
	}


	@Override
	protected void reset()
	{
		supportiveAttackers = Collections.emptyList();
	}


	@Override
	public void doCalc()
	{
		if (skirmishInformation.get().getStrategy() != ESkirmishStrategy.NONE
				|| (activateSupportiveInterceptor && redirectDetectionRequiresSupportiveAttacker()))
		{
			supportiveAttackers = activateSupportiveAttacker();
		} else
		{
			supportiveAttackers = Collections.emptyList();
		}
	}


	private boolean redirectDetectionRequiresSupportiveAttacker()
	{
		RedirectorDetectionInformation rInfo = redirectorDetectionInformation.get();
		// DISRUPT_OPPONENT: The disrupt is done by the attacker, but an additional supportive attacker is also added
		// DOUBLE_ATTACKER: Add a supportive attacker that supports the attacker
		return rInfo.getRecommendedAction() == ERecommendedReceiverAction.DISRUPT_OPPONENT ||
				rInfo.getRecommendedAction() == ERecommendedReceiverAction.DOUBLE_ATTACKER;
	}


	private List<BotID> activateSupportiveAttacker()
	{
		return potentialOffensiveBots.get().stream()
				.filter(b -> !ballHandlingBots.get().contains(b))
				.map(b -> getWFrame().getBot(b))
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(supportiveAttackerPos.get())))
				.map(ITrackedBot::getBotId)
				.stream()
				.collect(Collectors.toUnmodifiableList());
	}


	private boolean shouldAddSupportiveAttacker()
	{
		return getAiFrame().getGameState().isRunning()
				&& (getBall().getPos().x() > minXPosForSupportiveAttacker)
				&& !Geometry.getPenaltyAreaTheir().withMargin(marginOpponentPenAreaToDeactivateSupportiveAttacker)
				.isPointInShape(getBall().getPos());
	}
}
