/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.pathfinder.obstacles.input;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class DynamicMargin
{
	@Configurable(defValue = "200", comment = "Base distance for extra margin")
	private static double distance = 200;

	@Configurable(defValue = "3", comment = "Max velocity limit, where current velocity is capped and considered 100%")
	private static double maxVel = 3;

	static
	{
		ConfigRegistration.registerClass("sisyphus", DynamicMargin.class);
	}

	/**
	 * Get the extra margin based on the current velocity
	 *
	 * @return extra margin
	 */
	public static double getExtraMargin(double curVel)
	{
		double vel = Math.min(maxVel, curVel);
		double relative = vel / maxVel;
		return relative * relative * distance;
	}
}
