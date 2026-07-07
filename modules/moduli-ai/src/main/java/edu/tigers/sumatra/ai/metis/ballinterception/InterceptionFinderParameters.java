package edu.tigers.sumatra.ai.metis.ballinterception;

public record InterceptionFinderParameters(
		double maxSlackTimeToAccept,
		boolean isOvershootAllowed,
		double maxVelocityAtInterceptWithOvershoot
)
{
}
