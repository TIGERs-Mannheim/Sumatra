/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

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


	private Map<KickOrigin, RatedPass> selectPasses()
	{
		Map<KickOrigin, RatedPass> newPasses = new HashMap<>();
		for (var entry : ratedPasses.get().entrySet())
		{
			KickOrigin kickOrigin = entry.getKey();
			List<RatedPass> passes = entry.getValue();
			var pass = selectPass(kickOrigin, passes);
			if (pass != null)
			{
				newPasses.put(kickOrigin, pass);
			}
		}
		return newPasses;
	}


	private RatedPass selectPass(KickOrigin kickOrigin, List<RatedPass> passes)
	{
		// first phase: find best redirect
		var bestPass = passes.stream()
				.filter(e -> e.getScore(EPassRating.INTERCEPTION) > 0.25 &&
						e.getScore(EPassRating.REFLECT_GOAL_KICK) > 0.6 &&
						e.getScore(EPassRating.PASSABILITY) > 0.7)
				.max(Comparator.comparingDouble(e -> e.getScore(EPassRating.REFLECT_GOAL_KICK)));
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "redirect", Vector2.fromY(50)));
			return bestPass.get();
		}

		// second phase: find best goal kick
		bestPass = passes.stream()
				.filter(e -> e.getScore(EPassRating.INTERCEPTION) > 0.4 &&
						e.getScore(EPassRating.GOAL_KICK) > 0.6 &&
						e.getScore(EPassRating.PASSABILITY) > 0.7)
				.max(Comparator.comparingDouble(e -> e.getScore(EPassRating.GOAL_KICK)));
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "goal kick", Vector2.fromY(50)));
			return bestPass.get();
		}

		// third phase: find good pass Target that provides pressure
		bestPass = passes.stream()
				.filter(e -> e.getScore(EPassRating.INTERCEPTION) > 0.7 &&
						e.getScore(EPassRating.PASSABILITY) > 0.7)
				.max(Comparator.comparingDouble(e -> e.getScore(EPassRating.PRESSURE)));
		if (bestPass.isPresent())
		{
			getShapes(EAiShapesLayer.PASS_SELECTION).add(
					new DrawableAnnotation(bestPass.get().getPass().getKick().getTarget(), "pressure", Vector2.fromY(50)));
			return bestPass.get();
		}

		// fourth phase: take what is  left
		bestPass = passes.stream()
				.filter(e -> e.getScore(EPassRating.INTERCEPTION) > 0.3 &&
						e.getScore(EPassRating.PASSABILITY) > 0.3)
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
			return selectedPasses.keySet().stream()
					.filter(k -> k.getPos().distanceTo(kickOrigin.getPos()) < 100)
					.findAny().map(selectedPasses::get).orElse(null);
		}
		return null;
	}


	private void drawPass(final RatedPass pass)
	{
		var color = new Color(220, 0, 209, 180);
		var passDrawables = pass.getPass().createDrawables();
		passDrawables.forEach(d -> d.setColor(color));
		getShapes(EAiShapesLayer.PASS_SELECTION).addAll(passDrawables);
	}
}
