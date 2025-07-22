/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ball;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;


/**
 * All parameters required for ball models.
 */
@Value
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BallParameters
{
	/**
	 * Radius of the ball in [mm].
	 */
	double ballRadius;
	/**
	 * Sliding acceleration in [mm/s^2], expected to be negative.
	 */
	double accSlide;
	/**
	 * Rolling acceleration in [mm/s^2], expected to be negative.
	 */
	double accRoll;
	/**
	 * Ball inertia distribution between 0.4 (massive sphere) and 0.66 (hollow sphere).
	 */
	double inertiaDistribution;
	/**
	 * Chip kick velocity damping factor in XY direction for the first hop.
	 */
	double chipDampingXYFirstHop;
	/**
	 * Chip kick velocity damping factor in XY direction for all following hops.
	 */
	double chipDampingXYOtherHops;
	/**
	 * Chip kick velocity damping factor in Z direction.
	 */
	double chipDampingZ;
	/**
	 * If a chipped ball does not reach this height it is considered rolling [mm].
	 */
	double minHopHeight;
	/**
	 * Max. ball height that can be intercepted by robots [mm].
	 */
	double maxInterceptableHeight;
}
