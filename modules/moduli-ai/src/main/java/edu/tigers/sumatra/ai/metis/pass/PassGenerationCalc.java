/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseThreatType;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.targetrater.AngleRange;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This class generates {@link Pass}s for each available bot.
 */
@RequiredArgsConstructor
public class PassGenerationCalc extends ACalculator
{
	private final Supplier<Map<BotID, KickOrigin>> kickOrigins;
	private final Supplier<Map<BotID, List<AngleRange>>> inaccessibleBallAngles;
	private final Supplier<List<BotID>> ballHandlingBots;

	private PassGenerator passGenerator;

	@Getter
	private Map<KickOrigin, List<Pass>> generatedPasses;


	@Override
	protected void start()
	{
		var rnd = new Random(getWFrame().getTimestamp());
		passGenerator = new PassGenerator(rnd, inaccessibleBallAngles);
	}


	@Override
	public void doCalc()
	{
		Set<BotID> consideredBots = consideredBots();

		passGenerator.update(getAiFrame());
		generatedPasses = kickOrigins.get().values().stream()
				.collect(Collectors.toUnmodifiableMap(
						kickOrigin -> kickOrigin,
						kickOrigin -> passGenerator.generatePasses(consideredBots, kickOrigin)
				));
	}


	private Set<BotID> consideredBots()
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.map(ITrackedBot::getBotId)
				.filter(this::notTheKeeper)
				.filter(this::notADefenderExceptM2M)
				.filter(this::notAnInterchangeBot)
				.filter(this::notAPrimaryAttacker)
				.collect(Collectors.toSet());
	}


	private boolean notADefenderExceptM2M(final BotID id)
	{
		if (notInPlay(EPlay.DEFENSIVE, id))
		{
			return true;
		}
		return getAiFrame().getPrevFrame().getTacticalField().getDefenseOuterThreatAssignments()
				.stream()
				.filter(a -> a.getThreat().getType() != EDefenseThreatType.BOT_M2M)
				.map(DefenseThreatAssignment::getBotIds)
				.flatMap(Collection::stream)
				.noneMatch(botId -> Objects.equals(botId, id));
	}


	private boolean notAnInterchangeBot(final BotID id)
	{
		return notInPlay(EPlay.INTERCHANGE, id);
	}


	private boolean notAPrimaryAttacker(final BotID id)
	{
		return !(ballHandlingBots.get().contains(id));
	}


	private boolean notInPlay(final EPlay play, final BotID id)
	{
		return !getAiFrame().getPrevFrame().getTacticalField().getDesiredBotMap()
				.getOrDefault(play, Collections.emptySet())
				.contains(id);
	}


	private boolean notTheKeeper(final BotID id)
	{
		return id != getAiFrame().getKeeperId();
	}

}
