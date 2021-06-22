/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.planarcurve;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;


@Value
public class PlanarCurveState
{
	/**
	 * Position in [mm].
	 */
	IVector2 pos;

	/**
	 * Velocity in [mm/s].
	 */
	IVector2 vel;

	/**
	 * Acceleration in [mm/s^2].
	 */
	IVector2 acc;
}
