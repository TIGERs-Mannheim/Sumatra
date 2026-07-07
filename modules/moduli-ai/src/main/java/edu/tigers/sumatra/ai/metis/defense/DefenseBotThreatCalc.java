package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
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
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

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
	@Configurable(comment = "additionally reduce the threat rating by this percentage if the same bot-strategy combination was chosen last frame", defValue = "-0.3", unit = EConfigUnit.PERCENTAGE)
	private static double strategySelectionHysteresis = -0.3;
	@Configurable(comment = "How many positions the selected def strategy must differ from previous frame till the hysteresis reaches 0", defValue = "2", unit = EConfigUnit.COUNT)
	private static int strategySelectionHysteresisPositionDifference = 2;
	@Configurable(comment = "DO NOT KEEP THIS ACTIVE, THIS IS TEST ONLY AND HURTS PERFORMANCE A LOT", defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean checkIfThreatReductionRaterCacheIsWorking = false;

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
	private Map<Integer, Rating> internalDefenseThreatRatingForNumDefender;


	@Override
	public void doCalc()
	{
		defenseThreatReductionRater.onNewFrame();

		allMan2manBotThreats = allBotThreatsDefData.get().stream()
				.filter(data -> data.canBeOuterDefendedWithStrategy(EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.map(data -> buildDefenseBotThreat(data, EDefenseBotThreatDefStrategy.MAN_2_MAN_MARKER))
				.toList();

		var defendedBotThreatsWithRating = buildDefendedBotThreatsWithRating();
		defenseBotThreats = defendedBotThreatsWithRating.stream().map(ThreatWithRating::threat).toList();

		internalDefenseThreatRatingForNumDefender = new HashMap<>();
		internalDefenseThreatRatingForNumDefender.put(
				0, new Rating(
						StatisticsMath.anyOccurs(
								inDangerZoneBotThreatsDefData.get().stream().map(DefenseBotThreatDefData::threatRating)), 0
				)
		);
		for (var threatWithRating : defendedBotThreatsWithRating)
		{
			internalDefenseThreatRatingForNumDefender.put(
					internalDefenseThreatRatingForNumDefender.size(), threatWithRating.rating);
		}
		internalDefenseThreatRatingForNumDefender = Collections.unmodifiableMap(
				internalDefenseThreatRatingForNumDefender);
		drawBotThreats();

		defenseThreatRatingForNumDefender = internalDefenseThreatRatingForNumDefender.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().withoutBonus()
				));
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

		return defensiveCandidates.stream().min(Comparator.comparingDouble(threat -> threat.rating.includingBonus()));
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


	private Rating rateDefensiveCandidate(
			DefenseBotThreat candidate,
			List<ThreatWithRating> defendedBotThreatsWithRating
	)
	{
		var newDefendedBotThreats = defendedBotThreatsWithRating.stream()
				.map(ThreatWithRating::threat)
				.collect(Collectors.toList());
		newDefendedBotThreats.add(candidate);
		newDefendedBotThreats = Collections.unmodifiableList(newDefendedBotThreats);

		var rating = defenseThreatReductionRater.calcThreatRatingWantedWithCaching(
				inDangerZoneBotThreatsDefData.get(),
				newDefendedBotThreats,
				defenseBallThreat.get(),
				numBallDefender.get()
		);
		if (checkIfThreatReductionRaterCacheIsWorking)
		{
			var ratingNoCache = defenseThreatReductionRater.calcThreatRatingWanted(
					inDangerZoneBotThreatsDefData.get(),
					newDefendedBotThreats,
					defenseBallThreat.get(),
					numBallDefender.get()
			);

			Validate.isTrue(rating == ratingNoCache, "%.5f != %.5f", ratingNoCache, rating);
		}
		getShapes(EAiShapesLayer.DEFENSE_THREAT_RATING_REDUCTION).addAll(defenseThreatReductionRater.getShapes());

		var thisFrameNumber = defendedBotThreatsWithRating.size();
		var factor = candidate.getThreatRating();
		double bonus = findLastFrameOrderingNumber(candidate)
				.map(lastFrameNumber -> Math.abs(lastFrameNumber - thisFrameNumber))
				.map(diff -> SumatraMath.cap(1 - (diff / (double) strategySelectionHysteresisPositionDifference), 0, 1)
						* strategySelectionHysteresis * factor)
				.orElse(0.0);

		return new Rating(rating, bonus);
	}


	private Optional<Integer> findLastFrameOrderingNumber(DefenseBotThreat candidate)
	{
		if (defenseBotThreats == null)
		{
			return Optional.empty();
		}

		for (int i = 0; i < defenseBotThreats.size(); i++)
		{
			var lastFrameThreat = defenseBotThreats.get(i);
			if (lastFrameThreat.getBotID().equals(candidate.getBotID())
					&& lastFrameThreat.getDefendStrategy().equals(candidate.getDefendStrategy()))
			{
				return Optional.of(i);
			}
		}
		return Optional.empty();
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
			IVector2 protectionPos;
			String strategyName;
			var protectionPosOpt = withRating.getProtectionPosition();
			if (protectionPosOpt.isPresent())
			{
				protectionPos = protectionPosOpt.get();
				strategyName = withRating.getDefendStrategy().toString();
			} else
			{
				protectionPos = Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius())
						.projectPointOnToPenaltyAreaBorder(withRating.getPos());
				strategyName = "PEN_AREA_DEFENDER";
			}


			shapes.add(
					new DrawableCircle(Circle.createCircle(protectionPos, 1.25 * Geometry.getBotRadius()), Color.BLACK));
			shapes.add(new DrawableAnnotation(
					protectionPos, String.format("%d", defenderCount),
					getWFrame().getTeamColor().getColor()
			).withCenterHorizontally(true));

			texts.get(withRating.getBotID()).append(
					String.format(
							"%n%d  |  %.2f->%.2f (%.2f->%.2f) with %s", defenderCount,
							internalDefenseThreatRatingForNumDefender.get(defenderCount - 1).withoutBonus(),
							internalDefenseThreatRatingForNumDefender.get(defenderCount).withoutBonus(),
							internalDefenseThreatRatingForNumDefender.get(defenderCount).bonus(),
							internalDefenseThreatRatingForNumDefender.get(defenderCount).includingBonus(),
							strategyName
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


	private record Rating(double rating, double bonus)
	{
		double includingBonus()
		{
			return rating + bonus;
		}


		double withoutBonus()
		{
			return rating;
		}
	}

	private record ThreatWithRating(DefenseBotThreat threat, Rating rating)
	{
	}
}
