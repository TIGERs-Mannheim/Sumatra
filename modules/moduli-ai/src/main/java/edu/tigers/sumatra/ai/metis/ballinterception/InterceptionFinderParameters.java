/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballinterception;

public record InterceptionFinderParameters(
		double maxSlackTimeToAccept,
		boolean isOvershootAllowed,
		double maxVelocityAtInterceptWithOvershoot
)
{
}
