package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Builder;


@Builder
public record MovingRobotParams(
		IVector2 position,
		IVector2 velocity,
		double vLimit,
		double aLimit,
		double brkLimit,
		double radius,
		double reactionTime)
{}
