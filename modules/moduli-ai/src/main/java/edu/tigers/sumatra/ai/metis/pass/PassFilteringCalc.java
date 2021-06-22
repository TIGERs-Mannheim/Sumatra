/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.math.AngleMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * This class filters the Pass Targets
 */
@RequiredArgsConstructor
public class PassFilteringCalc extends ACalculator
{
	@Configurable(defValue = "10", comment = "How many passes to select")
	private static int maxPasses = 10;

	@Configurable(defValue = "0.1")
	private static double minAngleBetweenPassTargets = 0.1;

	@Configurable(defValue = "3")
	private static int numBins = 3;

	private final Supplier<Map<KickOrigin, List<RatedPass>>> ratedPasses;

	@Getter
	private Map<KickOrigin, List<RatedPass>> filteredAndRatedPasses;


	@Override
	public void doCalc()
	{
		filteredAndRatedPasses = new HashMap<>();
		for (var entry : ratedPasses.get().entrySet())
		{
			var filtered = filterPasses(groupPasses(entry.getValue()));
			filteredAndRatedPasses.put(entry.getKey(), filtered);
		}
	}


	private List<RatedPass> filterPasses(List<RatedPass> sortedPasses)
	{
		List<RatedPass> filteredPasses = new ArrayList<>(sortedPasses.size());
		for (var pass : sortedPasses)
		{
			if (filteredPasses.size() < maxPasses && isDistinctPassAngle(pass, filteredPasses))
			{
				filteredPasses.add(pass);
			}
		}
		return filteredPasses;
	}


	private boolean isDistinctPassAngle(RatedPass pass, List<RatedPass> passes)
	{
		double passAngle = getPassAngle(pass);
		for (var filteredPass : passes)
		{
			double filteredPassAngle = getPassAngle(filteredPass);
			if (AngleMath.diffAbs(passAngle, filteredPassAngle) < minAngleBetweenPassTargets)
			{
				return false;
			}
		}
		return true;
	}


	private double getPassAngle(RatedPass ratedPass)
	{
		Kick kick = ratedPass.getPass().getKick();
		return kick.getTarget().subtractNew(kick.getSource()).getAngle();
	}


	private List<RatedPass> groupPasses(List<RatedPass> passes)
	{
		List<List<RatedPass>> ratedPassGroups = new ArrayList<>();
		ratedPassGroups.add(passes);
		for (var rating : EPassRating.values())
		{
			List<List<RatedPass>> newPassGroups = new ArrayList<>();
			for (var passGroup : ratedPassGroups)
			{
				var passGroupsForGroup = groupPasses(passGroup, rating);
				newPassGroups.addAll(passGroupsForGroup);
			}
			ratedPassGroups = newPassGroups;
		}
		return ratedPassGroups.stream().flatMap(Collection::stream).collect(Collectors.toUnmodifiableList());
	}


	private List<List<RatedPass>> groupPasses(List<RatedPass> passes, EPassRating rating)
	{
		List<List<RatedPass>> ratedPassGroups = new ArrayList<>();
		IntStream.range(0, numBins).forEach(i -> ratedPassGroups.add(new ArrayList<>(0)));
		for (var ratedPass : passes.stream().sorted(byScore(rating)).collect(Collectors.toList()))
		{
			var score = ratedPass.getScore(rating);
			var i = Math.min((int) ((1 - score) * numBins), numBins - 1);
			var group = ratedPassGroups.get(i);
			group.add(ratedPass);
		}
		return ratedPassGroups.stream().filter(l -> !l.isEmpty()).collect(Collectors.toList());
	}


	private Comparator<RatedPass> byScore(EPassRating rating)
	{
		return Comparator.comparingDouble((RatedPass rt) -> rt.getScore(rating)).reversed();
	}


}
