/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotDistanceComparator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * This class rates the PassTargets created by PassGenerationCalc
 */
@RequiredArgsConstructor
public class PassRatingCalc extends ACalculator
{

	@Configurable(defValue = "true", comment = "Use dynamic pass interception rating based on pass stats",
			tags = { "pass" })
	private static boolean useDynamicInterceptionRating = true;

	@Configurable(defValue = "0.1", comment = "Hysteresis bonus multiplier for same receiver",
			tags = { "hysteresis", "pass" })
	private static double hystBonusMultiplierBot = 0.1;

	@Configurable(defValue = "0.15", comment = "Hysteresis bonus multiplier for same target",
			tags = { "hysteresis", "pass" })
	private static double hystBonusMultiplierTarget = 0.15;

	@Configurable(defValue = "false", comment = "Draw debug shapes", tags = { "debug", "pass" })
	private static boolean debugShapes = false;

	private final Supplier<Map<KickOrigin, List<Pass>>> generatedPasses;
	private final Supplier<PassStats> passStats;
	private final Supplier<OffensiveZones> offensiveZones;
	private final Supplier<List<BotID>> opponentMan2ManMarkers;

	private final RatedPassFactory ratingFactory = new RatedPassFactory();

	@Getter
	private Map<KickOrigin, List<RatedPass>> passesRated;


	@Override
	public void doCalc()
	{
		var consideredBots = getWFrame().getBots().values().stream()
				// Ignore the keeper
				.filter(e -> e.getBotId() != getAiFrame().getKeeperId())
				// Sort by distance to ball as rough estimate to get the closest bots first
				.sorted(new BotDistanceComparator(getBall().getPos()))
				.toList();
		var consideredBotsIntercept = consideredBots.stream().filter(e -> e.getBotId() != getAiFrame().getKeeperOpponentId()).toList();
		if (useDynamicInterceptionRating)
		{
			ratingFactory.updateDynamic(
					consideredBots,
					consideredBotsIntercept,
					passStats.get(),
					offensiveZones.get(),
					opponentMan2ManMarkers.get()
			);
		} else
		{
			ratingFactory.update(
					consideredBots,
					consideredBotsIntercept,
					opponentMan2ManMarkers.get()
			);
		}
		if (debugShapes)
		{
			ratingFactory.setShapes(getShapes(EAiShapesLayer.PASS_RATING_DEBUG));
		} else
		{
			ratingFactory.setShapes(null);
		}

		passesRated = generatedPasses.get().entrySet().stream().collect(Collectors.toUnmodifiableMap(
						Map.Entry::getKey,
						e -> ratePasses(e.getValue())
				)
		);

		passesRated.values().stream().flatMap(Collection::stream).forEach(this::drawLinesToPassTargets);
	}


	private List<RatedPass> ratePasses(List<Pass> passes)
	{
		return passes.parallelStream()
				.map(ratingFactory::rate)
				.map(this::addHysteresis)
				.sorted(Comparator.comparingDouble(e -> e.getScore(EPassRating.INTERCEPTION)))
				.toList()
				.reversed();
	}


	private RatedPass addHysteresis(RatedPass ratedPass)
	{
		RatedPass oldSelectedPass = getAiFrame().getPrevFrame().getTacticalField().getSelectedPasses().values().stream()
				.findFirst()
				.orElse(null);
		if (oldSelectedPass == null)
		{
			return ratedPass;
		}

		var oldReceiver = oldSelectedPass.getPass().getReceiver();
		var newReceiver = ratedPass.getPass().getReceiver();
		double hystBonus = 1.0;
		if (oldReceiver.equals(newReceiver))
		{
			// give bot base bonus
			hystBonus += hystBonusMultiplierBot;
		}

		if (oldSelectedPass.getPass().getKick().getTarget().distanceTo(ratedPass.getPass().getKick().getTarget())
				< 5)
		{
			// give target location based bonus
			hystBonus += hystBonusMultiplierTarget;
		}

		Map<EPassRating, Double> scores = new EnumMap<>(EPassRating.class);
		for (var rating : EPassRating.values())
		{
			scores.put(rating, ratedPass.getScore(rating) * hystBonus);
		}

		return ratedPass.toBuilder().scores(scores).build();
	}


	private void drawLinesToPassTargets(final RatedPass ratedPass)
	{
		ITrackedBot bot = getWFrame().getTiger(ratedPass.getPass().getReceiver());
		IVector2 kickerPos = bot.getBotKickerPos();

		IVector2 target = ratedPass.getPass().getKick().getTarget();
		if (!kickerPos.equals(target))
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(new DrawableLine(kickerPos, target, new Color(55, 55, 55, 70)));
		}
	}
}
