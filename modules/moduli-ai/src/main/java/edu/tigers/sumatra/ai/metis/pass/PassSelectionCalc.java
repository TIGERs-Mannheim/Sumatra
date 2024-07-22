/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * This class selects the best Pass Targets and chooses the right score mode
 */
@RequiredArgsConstructor
public class PassSelectionCalc extends ACalculator
{
	private final Supplier<Map<KickOrigin, List<RatedPass>>> ratedPasses;

	@Getter
	private Map<KickOrigin, RatedPass> selectedPasses;

	@Configurable(comment = "Secondary rating grouping factor", defValue = "0.95")
	private static double secondaryRatingGroupingFactor = 0.95;


	@Override
	protected void reset()
	{
		selectedPasses = Map.of();
	}


	@Override
	public void doCalc()
	{
		selectedPasses = Collections.unmodifiableMap(selectPasses());
		this.selectedPasses.values().forEach(this::drawPass);
	}


	private Optional<KickOrigin> getPreviousKickOrigin(Map<KickOrigin, RatedPass> previousSelectedPasses, KickOrigin kickOrigin)
	{
		return previousSelectedPasses.keySet().stream()
				.filter(e -> e.getPos().distanceTo(kickOrigin.getPos()) < 50)
				.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(kickOrigin.getPos())));
	}

	private Map<KickOrigin, RatedPass> selectPasses()
	{
		var previousSelectedPasses = getPreviousPasses();
		Map<KickOrigin, RatedPass> newPasses = new HashMap<>();
		for (var entry : ratedPasses.get().entrySet())
		{
			KickOrigin kickOrigin = entry.getKey();
			List<RatedPass> passes = entry.getValue();

			var oldKickorigin = getPreviousKickOrigin(previousSelectedPasses, kickOrigin);
			RatedPass oldPass = null;
			if (oldKickorigin.isPresent())
			{
				oldPass = previousSelectedPasses.get(kickOrigin);
			}

			var pass = selectPass(passes, oldPass);
			if (pass != null)
			{
				newPasses.put(kickOrigin, pass);
			}
		}
		return newPasses;
	}


	private Map<KickOrigin,RatedPass> getPreviousPasses()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getSelectedPasses();
	}


	private RatedPass selectPass(List<RatedPass> passes, RatedPass oldPass)
	{
		// first phase: find best redirect
		var sortedPasses = passes.stream()
				.filter(e -> filterReflectPhase(e, oldPass))
				.sorted(Comparator.comparingDouble(e -> 1 - e.getScore(EPassRating.REFLECT_GOAL_KICK)))
				.toList();
		Optional<RatedPass> bestPass = getBestPass(sortedPasses, EPassRating.REFLECT_GOAL_KICK);
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "redirect", Vector2.fromY(50)));
			return bestPass.get();
		}

		// second phase: find best goal kick
		sortedPasses = passes.stream()
				.filter(e -> filterGoalKickPhase(e, oldPass))
				.sorted(Comparator.comparingDouble(e -> 1 - e.getScore(EPassRating.GOAL_KICK)))
				.toList();
		bestPass = getBestPass(sortedPasses, EPassRating.GOAL_KICK);
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "goal kick", Vector2.fromY(50)));
			return bestPass.get();
		}

		// third phase: find good pass Target that provides pressure
		sortedPasses = passes.stream()
				.filter(e -> filterPressurePhase(e, oldPass))
				.sorted(Comparator.comparingDouble(e -> 1 - e.getScore(EPassRating.PRESSURE)))
				.toList();
		bestPass = getBestPass(sortedPasses, EPassRating.PRESSURE);
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "pressure", Vector2.fromY(50)));
			return bestPass.get();
		}

		// fourth phase: take what is left, we do not apply hysteresis for such looser passes on purpose
		bestPass = passes.stream()
				.filter(e -> e.getScore(EPassRating.INTERCEPTION) > 0.5 &&
						e.getScore(EPassRating.PASSABILITY) > 0.5 &&
						e.getMaxScore(EPassRating.PRESSURE, EPassRating.GOAL_KICK, EPassRating.REFLECT_GOAL_KICK) > 0.3)
				.max(Comparator.comparingDouble(e -> e.getScore(EPassRating.INTERCEPTION)));
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "what is left",
							Vector2.fromY(50)));
			return bestPass.get();
		}

		if (getAiFrame().getGameState().isFreeKickForUs() || getAiFrame().getGameState().isKickoffForUs())
		{
			return passes.stream()
					.filter(p -> p.getPass().isChip())
					.max(Comparator.<RatedPass>comparingDouble(e -> e.getScore(EPassRating.PASSABILITY))
							.thenComparing(e -> e.getScore(EPassRating.INTERCEPTION))
							.thenComparing(e -> e.getScore(EPassRating.REFLECT_GOAL_KICK))
							.thenComparing(e -> e.getScore(EPassRating.GOAL_KICK))
							.thenComparing(e -> e.getScore(EPassRating.PRESSURE))
					)
					.orElse(null);
		}
		return null;
	}


	private boolean filterReflectPhase(RatedPass currentPass, RatedPass oldPass)
	{
		boolean applyHyst = shouldApplyHystForPass(currentPass, oldPass);
		return currentPass.getScore(EPassRating.INTERCEPTION) > 0.35 - (applyHyst ? 0.2 : 0) &&
				currentPass.getScore(EPassRating.REFLECT_GOAL_KICK) > 0.6 - (applyHyst ? 0.2 : 0) &&
				currentPass.getScore(EPassRating.PASSABILITY) > 0.7 - (applyHyst ? 0.2 : 0);
	}

	private boolean filterGoalKickPhase(RatedPass currentPass, RatedPass oldPass)
	{
		boolean applyHyst = shouldApplyHystForPass(currentPass, oldPass);
		return currentPass.getScore(EPassRating.INTERCEPTION) > 0.5 - (applyHyst ? 0.2 : 0) &&
				currentPass.getScore(EPassRating.GOAL_KICK) > 0.6 - (applyHyst ? 0.1 : 0) &&
				currentPass.getScore(EPassRating.PASSABILITY) > 0.7 - (applyHyst ? 0.3 : 0);
	}

	private boolean filterPressurePhase(RatedPass currentPass, RatedPass oldPass)
	{
		boolean applyHyst = shouldApplyHystForPass(currentPass, oldPass);
		return currentPass.getScore(EPassRating.INTERCEPTION) > 0.5 - (applyHyst ? 0.2 : 0) &&
				currentPass.getScore(EPassRating.PASSABILITY) > 0.7 - (applyHyst ? 0.4 : 0);
	}


	private static boolean shouldApplyHystForPass(RatedPass e, RatedPass oldPass)
	{
		return oldPass != null
				&& oldPass.getPass().getKick().getTarget().distanceTo(e.getPass().getKick().getTarget()) < 5;
	}


	private Optional<RatedPass> getBestPass(List<RatedPass> sortedPasses, EPassRating phaseRating)
	{
		double bestPassScore = sortedPasses.stream().findFirst()
				.map(e -> e.getScore(phaseRating))
				.orElse(0.0);
		return sortedPasses.stream()
				.filter(e -> e.getScore(phaseRating) > bestPassScore * secondaryRatingGroupingFactor)
				.max(Comparator.comparingDouble(e -> e.getScore(EPassRating.INTERCEPTION)));
	}


	private void drawPass(final RatedPass pass)
	{
		var color = new Color(220, 0, 209, 180);
		var passDrawables = pass.getPass().createDrawables();
		passDrawables.forEach(d -> d.setColor(color));
		getShapes(EAiShapesLayer.PASS_SELECTION).addAll(passDrawables);
	}
}
