/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.support.behaviors.AggressiveMan2ManMarkerBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.BreakThroughDefenseRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.DirectRedirectorSupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.FakePassReceiverSupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.ISupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.KickoffSupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.MidfieldRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.MoveOnVoronoiRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.PenaltyAreaAttackerRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ai.metis.support.behaviors.repulsive.AttackerRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.repulsive.PassReceiverRepulsiveBehavior;
import edu.tigers.sumatra.ai.metis.targetrater.GoalKick;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
public class SupportBehaviorCalc extends ACalculator
{
	static
	{
		for (ESupportBehavior supportBehavior : ESupportBehavior.values())
		{
			ConfigRegistration.registerClass("metis", supportBehavior.getInstanceableClass().getImpl());
		}
	}

	private final EnumMap<ESupportBehavior, ISupportBehavior> behaviors = new EnumMap<>(ESupportBehavior.class);
	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<BallPossession> ballPossession;
	private final Supplier<OffensiveStrategy> offensiveStrategy;
	private final Supplier<List<IVector2>> supportiveGoalPositions;
	private final Supplier<List<IVector2>> possibleSupporterMidfieldPositions;

	private final Supplier<List<IVector2>> possibleSupporterKickoffPositions;
	private final Supplier<List<ICircle>> freeSpots;
	private final Supplier<Map<BotID, OffensiveAction>> offensiveActions;
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;
	private final Supplier<GoalKick> bestGoalKick;
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<List<IArc>> offensiveShadows;
	private final Supplier<Optional<OngoingPass>> ongoingPass;
	private final Supplier<Map<BotID, DefenseBotThreat>> supporterToBotThreatMapping;

	@Getter
	private final Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportViabilities = new HashMap<>();

	@Getter
	private final EnumMap<ESupportBehavior, Boolean> activeBehaviors = new EnumMap<>(ESupportBehavior.class);


	@Override
	protected void reset()
	{
		supportViabilities.clear();
		activeBehaviors.clear();
	}


	@Override
	protected void start()
	{
		behaviors.put(ESupportBehavior.DIRECT_REDIRECTOR, new DirectRedirectorSupportBehavior(
				kickOrigins,
				supportiveGoalPositions
		));
		behaviors.put(ESupportBehavior.FAKE_PASS_RECEIVER, new FakePassReceiverSupportBehavior(
				offensiveActions,
				ongoingPass
		));
		behaviors.put(ESupportBehavior.PENALTY_AREA_ATTACKER, new PenaltyAreaAttackerRepulsiveBehavior(
				offensiveStrategy,
				kickOrigins,
				bestGoalKick,
				ballPossession
		));
		behaviors.put(ESupportBehavior.BREAKTHROUGH_DEFENSIVE, new BreakThroughDefenseRepulsiveBehavior());
		behaviors.put(ESupportBehavior.MIDFIELD, new MidfieldRepulsiveBehavior(
				possibleSupporterMidfieldPositions
		));
		behaviors.put(ESupportBehavior.KICKOFF, new KickoffSupportBehavior(possibleSupporterKickoffPositions));
		behaviors.put(ESupportBehavior.MAN_2_MAN_MARKING,
				new AggressiveMan2ManMarkerBehavior(ballPossession, supporterToBotThreatMapping));
		behaviors.put(ESupportBehavior.REPULSIVE_PASS_RECEIVER, new PassReceiverRepulsiveBehavior(
				desiredBots,
				offensiveActions,
				offensiveStrategy,
				skirmishInformation,
				offensiveShadows
		));
		behaviors.put(ESupportBehavior.REPULSIVE_ATTACKER, new AttackerRepulsiveBehavior(
				desiredBots,
				offensiveActions,
				supportiveGoalPositions
		));
		behaviors.put(ESupportBehavior.MOVE_VORONOI, new MoveOnVoronoiRepulsiveBehavior(
				freeSpots
		));
	}


	@Override
	protected void stop()
	{
		behaviors.clear();
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return desiredBots.get().get(EPlay.SUPPORT) != null;
	}


	@Override
	protected void doCalc()
	{
		behaviors.values().forEach(b -> b.updateData(getAiFrame()));

		for (BotID bot : desiredBots.get().get(EPlay.SUPPORT))
		{
			EnumMap<ESupportBehavior, SupportBehaviorPosition> viabilitiesForRobot = new EnumMap<>(ESupportBehavior.class);

			for (var behaviorCalcEntry : behaviors.entrySet())
			{
				ESupportBehavior behaviorType = behaviorCalcEntry.getKey();
				ISupportBehavior behavior = behaviorCalcEntry.getValue();
				if (behavior.isEnabled())
				{
					viabilitiesForRobot.put(behaviorType, behavior.calculatePositionForRobot(bot));
				} else
				{
					viabilitiesForRobot.put(behaviorType, SupportBehaviorPosition.notAvailable());
				}
			}
			supportViabilities.put(bot, viabilitiesForRobot);
		}

		// save which support behaviors were enabled during this frame
		for (var behaviorCalcEntry : behaviors.entrySet())
		{
			activeBehaviors.put(behaviorCalcEntry.getKey(), behaviorCalcEntry.getValue().isEnabled());
		}
	}
}
