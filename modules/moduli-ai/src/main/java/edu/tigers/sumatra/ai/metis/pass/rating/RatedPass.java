/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


@Value
@Builder(toBuilder = true)
public class RatedPass
{
	@NonNull
	Pass pass;
	@Singular
	Map<EPassRating, Double> scores;


	/**
	 * Get the score for the giving rating, or 0 if the rating is not present.
	 *
	 * @param passRating
	 * @return
	 */
	public double getScore(EPassRating passRating)
	{
		return scores.getOrDefault(passRating, 0.0);
	}


	/**
	 * Get the combined score of all giving ratings.
	 * The scores are combined by multiplication.
	 * If there is none of the ratings present, the combined score defaults to 1.
	 *
	 * @param passRatings
	 * @return
	 */
	public double getCombinedScore(EPassRating... passRatings)
	{
		return Arrays.stream(passRatings)
				.map(scores::get)
				.filter(Objects::nonNull)
				.reduce(1.0, (a, b) -> a * b);
	}


	/**
	 * Get the max score of all giving ratings.
	 * If there is none of the ratings present, the score defaults to 0.
	 *
	 * @param passRatings
	 * @return
	 */
	public double getMaxScore(EPassRating... passRatings)
	{
		return Arrays.stream(passRatings)
				.map(scores::get)
				.filter(Objects::nonNull)
				.reduce(0.0, Math::max);
	}
}
