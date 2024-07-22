/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.support.behaviors.BreakThroughDefenseBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
@Log4j2
public class SupportBehaviorAssignmentCalc extends ACalculator
{
	@Configurable(comment = "[mm]", defValue = "-2000.0")
	private static double xPosMidfieldSingleMidfieldSupporter = -2000;

	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>>> viabilityMap;
	private final PointChecker pointChecker = new PointChecker().checkConfirmWithKickOffRules();


	@Getter
	private Map<BotID, ESupportBehavior> behaviorAssignment = new HashMap<>();


	@Override
	protected void reset()
	{
		behaviorAssignment = Map.of();
	}


	@Override
	protected void doCalc()
	{
		behaviorAssignment = doAssignment();
	}


	private int getMaxRolesForBehaviour(ESupportBehavior behavior)
	{
		return switch (behavior)
		{
			case BREAKTHROUGH_DEFENSIVE -> BreakThroughDefenseBehavior.getMaxNumberAtPenaltyArea();
			case PENALTY_AREA_ATTACKER, DIRECT_REDIRECTOR -> 1;
			case MIDFIELD -> getMaxNumberMidfield();
			default -> Integer.MAX_VALUE;
		};
	}


	private int getMaxNumberMidfield()
	{
		if (getWFrame().getBall().getPos().x() > xPosMidfieldSingleMidfieldSupporter)
		{
			return 1;
		}
		return 2;
	}


	/**
	 * Filter function that checks if a BotID has already been handled
	 *
	 * @param alreadyHandled list of all bots that have been handled so far
	 * @param botID          that should be checked
	 * @return True if bot has not been handled before
	 */
	private boolean roleIsNotHandledYet(Collection<BotID> alreadyHandled, BotID botID)
	{
		return !alreadyHandled.contains(botID);
	}


	private Map<BotID, ESupportBehavior> doAssignment()
	{
		Map<BotID, ESupportBehavior> assignment = new HashMap<>();
		var supporter = desiredBots.get().get(EPlay.SUPPORT);

		if (supporter == null)
		{
			return Map.of();
		}

		for (ESupportBehavior behavior : ESupportBehavior.values())
		{
			int limit = getMaxRolesForBehaviour(behavior);

			supporter.stream()
					.filter(botId -> roleIsNotHandledYet(assignment.keySet(), botId))
					.filter(botId -> getViability(behavior, botId) > 0)
					.sorted(Comparator.comparingDouble((BotID botId) -> getViability(behavior, botId)).reversed())
					.limit(limit)
					.forEach(botID -> assignment.put(botID, behavior));
		}
		return Collections.unmodifiableMap(assignment);
	}


	private double getViability(ESupportBehavior behavior, BotID botId)
	{
		SupportBehaviorPosition supportBehaviorPosition = viabilityMap.get().get(botId).get(behavior);
		if (supportBehaviorPosition == null)
		{
			return 0;
		}
		if (supportBehaviorPosition.getPosition() == null ||
				!pointChecker.allMatch(getAiFrame(), supportBehaviorPosition.getPosition()))
		{
			return 0;
		}
		return supportBehaviorPosition.getViability();
	}
}
