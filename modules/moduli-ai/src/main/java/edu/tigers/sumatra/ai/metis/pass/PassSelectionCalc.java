/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
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
	@Configurable(defValue = "0.3")
	private static double minPassabilityScore = 0.3;

	@Configurable(defValue = "0.3")
	private static double minInterceptionScore = 0.3;

	private final Supplier<Map<KickOrigin, List<RatedPass>>> ratedPasses;

	@Getter
	private Map<KickOrigin, RatedPass> selectedPasses;


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
			var pass = onePassPerBot(entry.getValue());
			if (pass != null
					&& pass.getScore(EPassRating.PASSABILITY) > minPassabilityScore
					&& pass.getScore(EPassRating.INTERCEPTION) > minInterceptionScore)
			{
				newPasses.put(entry.getKey(), pass);
			}
		}
		return newPasses;
	}


	private RatedPass onePassPerBot(List<RatedPass> passes)
	{
		if (passes.isEmpty())
		{
			return null;
		}
		return passes.get(0);
	}


	private void drawPass(final RatedPass pass)
	{
		var color = new Color(220, 0, 209, 180);
		var passDrawables = pass.getPass().createDrawables();
		passDrawables.forEach(d -> d.setColor(color));
		getShapes(EAiShapesLayer.PASS_SELECTION).addAll(passDrawables);
	}
}
