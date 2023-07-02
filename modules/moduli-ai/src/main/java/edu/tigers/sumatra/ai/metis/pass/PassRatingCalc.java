/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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

	@Configurable(defValue = "true")
	private static boolean useDynamicInterceptionRating = true;

	private final Supplier<Map<KickOrigin, List<Pass>>> generatedPasses;

	private final Supplier<PassStats> passStats;

	private final Supplier<OffensiveZones> offensiveZones;

	private final RatedPassFactory ratingFactory = new RatedPassFactory();


	@Getter
	private Map<KickOrigin, List<RatedPass>> passesRated;


	@Override
	public void doCalc()
	{
		// all opponents except those which are in a skirmish
		var consideredBots = getWFrame().getOpponentBots().values().stream()
				.filter(p -> p.getPos().distanceTo(getBall().getPos()) > Geometry.getBotRadius() + 100)
				.toList();

		if (useDynamicInterceptionRating)
		{
			ratingFactory.updateDynamic(consideredBots, passStats.get(), offensiveZones.get());
		} else
		{
			ratingFactory.update(consideredBots);
		}

		passesRated = generatedPasses.get().entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(
						Map.Entry::getKey,
						e -> ratePasses(e.getValue()))
				);

		passesRated.values().stream().flatMap(Collection::stream).forEach(this::drawLinesToPassTargets);
		passesRated.values().stream().flatMap(Collection::stream).forEach(this::drawRatedPass);
	}


	private List<RatedPass> ratePasses(List<Pass> passes)
	{
		return passes.parallelStream()
				.map(ratingFactory::rate)
				.sorted(Comparator.comparing(this::compareRatedPasses))
				.toList();
	}


	private double compareRatedPasses(RatedPass p)
	{
		return p.getScore(EPassRating.REFLECT_GOAL_KICK) * 100 +
				p.getScore(EPassRating.GOAL_KICK) * 10 +
				p.getScore(EPassRating.PRESSURE) * 1;
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


	private void drawRatedPass(final RatedPass ratedPass)
	{
		var color = new Color(0, 120, 100, 150);
		var target = ratedPass.getPass().getKick().getTarget();
		getShapes(EAiShapesLayer.PASS_SELECTION).add(new DrawableCircle(target, 30, color).setFill(true));

		boolean chip = ratedPass.getPass().getKick().getKickParams().getDevice() == EKickerDevice.CHIP;
		var text = (chip ? "ch: " : "st: ") +
				Arrays.stream(EPassRating.values()).map(p -> passRatingToStr(ratedPass, p))
						.collect(Collectors.joining(" | "));

		var chipOffset = chip ? 6.0 : 0.0;
		getShapes(EAiShapesLayer.PASS_RATING)
				.add(new DrawableAnnotation(target, text)
						.setColor(Color.black)
						.withFontHeight(6)
						.withCenterHorizontally(true)
						.withOffset(Vector2.fromXY(0, 33 + chipOffset)));
	}


	private String passRatingToStr(RatedPass ratedPass, EPassRating passRating)
	{
		return passRating.getAbbreviation() + ":" + scoreToStr(ratedPass.getScore(passRating));
	}


	private String scoreToStr(final double passScore)
	{
		return Long.toString(Math.round(passScore * 100));
	}
}
