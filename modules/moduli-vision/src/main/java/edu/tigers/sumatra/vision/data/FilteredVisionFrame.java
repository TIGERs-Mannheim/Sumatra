/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.drawable.ShapeMap;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * The output frame of a vision filter
 */
@Value
@Builder(setterPrefix = "with")
public class FilteredVisionFrame
{
	@NonNull
	Long id;
	@NonNull
	Long timestamp;
	@NonNull
	FilteredVisionBall ball;
	@NonNull
	List<FilteredVisionBot> bots;
	@NonNull
	ShapeMap shapeMap;
	FilteredVisionKick kick;


	/**
	 * Create an empty frame, id is zero, ball at (0,0)
	 *
	 * @return a new empty frame
	 */
	public static FilteredVisionFrame createEmptyFrame()
	{
		FilteredVisionBall b = FilteredVisionBall.builder()
				.withTimestamp(0L)
				.withBallState(new BallState())
				.withLastVisibleTimestamp(0L)
				.build();

		return builder()
				.withId(0L)
				.withTimestamp(0L)
				.withBots(Collections.emptyList())
				.withBall(b)
				.withShapeMap(new ShapeMap())
				.build();
	}


	public Optional<FilteredVisionKick> getKick()
	{
		return Optional.ofNullable(kick);
	}
}
