/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreatDefData;
import edu.tigers.sumatra.ai.metis.defense.data.EDefenseBotThreatDefStrategy;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class DefenseBotThreatCalc extends ADefenseThreatCalc
{
	private final Supplier<List<DefenseBotThreatDefData>> inDangerZoneBotThreatsDefData;
	private final Supplier<List<DefenseBotThreatDefData>> allBotThreatsDefData;
	private final Supplier<DefenseBallThreat> defenseBallThreat;
	private final Supplier<Integer> numBallDefender;

	private final DefenseThreatReductionRater defenseThreatReductionRater = new DefenseThreatReductionRater();
	@Getter
	private List<DefenseBotThreat> defenseBotThreats;
	@Getter
	private List<DefenseBotThreat> allMan2manBotThreats;
	@Getter
	private Map<Integer, Double> defenseThreatRatingForNumDefender;


	@Override
	public void doCalc()
	{
		allMan2manBotThreats = allBotThreatsDefData.get().stream()
				.filter(data -> data.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.map(data -> buildDefenseBotThreat(data, EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.toList();

		var defendedBotThreatsWithRating = buildDefendedBotThreatsWithRating();
		defenseBotThreats = defendedBotThreatsWithRating.stream().map(ThreatWithRating::threat).toList();

		defenseThreatRatingForNumDefender = new HashMap<>();
		defenseThreatRatingForNumDefender.put(
				0, StatisticsMath.anyOccurs(
						inDangerZoneBotThreatsDefData.get().stream().map(DefenseBotThreatDefData::threatRating))
		);
		for (var threatWithRating : defendedBotThreatsWithRating)
		{
			defenseThreatRatingForNumDefender.put(defenseThreatRatingForNumDefender.size(), threatWithRating.rating);
		}
		defenseThreatRatingForNumDefender = Collections.unmodifiableMap(defenseThreatRatingForNumDefender);
		drawBotThreats();
	}


	private List<ThreatWithRating> buildDefendedBotThreatsWithRating()
	{
		List<ThreatWithRating> defendedBotThreatsWithRating = new ArrayList<>();

		while (defendedBotThreatsWithRating.size() < 100)
		{
			var defendedBotThreat = chooseBestDefensiveStrategy(defendedBotThreatsWithRating);
			if (defendedBotThreat.isEmpty())
			{
				break;
			} else
			{
				defendedBotThreatsWithRating.add(defendedBotThreat.get());
			}
		}
		return Collections.unmodifiableList(defendedBotThreatsWithRating);
	}


	private Optional<ThreatWithRating> chooseBestDefensiveStrategy(List<ThreatWithRating> defendedBotThreatsWithRating)
	{
		List<ThreatWithRating> defensiveCandidates = new ArrayList<>();
		defensiveCandidates.addAll(inDangerZoneBotThreatsDefData.get().stream()
				.filter(data -> isStillUndefended(
						data, defendedBotThreatsWithRating,
						EDefenseBotThreatDefStrategy.CENTER_BACK
				))
				.map(data -> buildDefenseBotThreat(data, EDefenseBotThreatDefStrategy.CENTER_BACK))
				.map(t -> new ThreatWithRating(t, rateDefensiveCandidate(t, defendedBotThreatsWithRating)))
				.toList()
		);
		defensiveCandidates.addAll(inDangerZoneBotThreatsDefData.get().stream()
				.filter(data -> isStillUndefended(
						data, defendedBotThreatsWithRating,
						EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER
				))
				.filter(data -> data.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.map(data -> buildDefenseBotThreat(data, EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.map(t -> new ThreatWithRating(t, rateDefensiveCandidate(t, defendedBotThreatsWithRating)))
				.toList()
		);

		return defensiveCandidates.stream().min(Comparator.comparingDouble(ThreatWithRating::rating));
	}


	private boolean isStillUndefended(
			DefenseBotThreatDefData defData,
			List<ThreatWithRating> defendedBotThreatsWithRating, EDefenseBotThreatDefStrategy defStrategy
	)
	{
		return defendedBotThreatsWithRating.stream()
				.noneMatch(t -> defData.getBotId().equals(t.threat.getBotID())
						&& t.threat.getDefendStrategy() == defStrategy);
	}


	private DefenseBotThreat buildDefenseBotThreat(
			DefenseBotThreatDefData defData,
			EDefenseBotThreatDefStrategy defStrategy
	)
	{

		var strategyData = switch (defStrategy)
		{
			case CENTER_BACK -> defData.centerBackDefStrategyData();
			case MAN_2_MAN_MARKER -> defData.man2manDefStrategyData();
		};

		return new DefenseBotThreat(strategyData, defData.bot(), defData.threatRating());
	}


	private double rateDefensiveCandidate(
			DefenseBotThreat candidate,
			List<ThreatWithRating> defendedBotThreatsWithRating
	)
	{
		var newDefendedBotThreats = defendedBotThreatsWithRating.stream().map(ThreatWithRating::threat)
				.collect(Collectors.toList());
		newDefendedBotThreats.add(candidate);
		var rating = defenseThreatReductionRater.calcThreatRatingWanted(
				inDangerZoneBotThreatsDefData.get(),
				newDefendedBotThreats,
				defenseBallThreat.get(),
				numBallDefender.get()
		);
		getShapes(EAiShapesLayer.DEFENSE_THREAT_RATING_REDUCTION)
				.addAll(defenseThreatReductionRater.getShapes());
		return rating;
	}


	private void drawBotThreats()
	{
		int defenderCount = 1;
		var shapes = getShapes(EAiShapesLayer.DEFENSE_BOT_THREATS);
		Map<BotID, StringBuilder> texts = new HashMap<>();
		inDangerZoneBotThreatsDefData.get().forEach(t -> {
			if (!texts.containsKey(t.getBotId()))
			{
				texts.put(t.getBotId(), new StringBuilder(String.format("Rating: %.2f", t.threatRating())));
			}
		});
		for (var withRating : defenseBotThreats)
		{
			final var finalDefenderCount = defenderCount;
			withRating.getProtectionPosition().ifPresent(pos -> {
				shapes.add(new DrawableCircle(Circle.createCircle(pos, 1.25 * Geometry.getBotRadius()), Color.BLACK));
				shapes.add(new DrawableAnnotation(
						pos, String.format("%d", finalDefenderCount),
						getWFrame().getTeamColor().getColor()
				).withCenterHorizontally(true));
			});
			texts.get(withRating.getBotID()).append(
					String.format(
							"%n%d  |  %.2f->%.2f with %s", defenderCount,
							defenseThreatRatingForNumDefender.get(defenderCount - 1),
							defenseThreatRatingForNumDefender.get(defenderCount),
							withRating.getDefendStrategy().toString()
					));
			++defenderCount;
			drawThreat(withRating);
		}
		texts.forEach((botID, text) -> shapes.add(
				new DrawableAnnotation(
						getWFrame().getBot(botID).getPos().addNew(Vector2.fromY(200)), text.toString(),
						Color.BLACK
				).withCenterHorizontally(true)));
	}


	private record ThreatWithRating(DefenseBotThreat threat, double rating)
	{
	}
}
