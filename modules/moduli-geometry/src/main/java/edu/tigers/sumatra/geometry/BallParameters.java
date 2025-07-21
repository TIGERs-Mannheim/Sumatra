/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.Data;
import lombok.Setter;


/**
 * All parameters required for ball models.
 */
@Setter
@Data
public class BallParameters
{
	@Configurable(
			comment = "Ball sliding acceleration [mm/s^2]",
			defValue = "-3000.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = {"-3000.0", "-3000.0", "-3000.0", "-3000.0", "-3000.0", "-3000.0", "-2460.0"}
	)
	private double accSlide = -3000.0;

	@Configurable(
			comment = "Ball rolling acceleration [mm/s^2]",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = {"-260.0", "-260.0", "-260.0", "-260.0", "-260.0", "-260.0", "-290.0"}
	)
	private double accRoll = -260.0;

	@Configurable(
			comment = "Fraction of the initial velocity where the ball starts to roll",
			defValue = "0.64",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" }
	)
	private double kSwitch = 0.64;

	@Configurable(
			comment = "Ball inertia distribution between 0.4 (massive sphere) and 0.66 (hollow sphere)",
			defValue = "0.5",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" }
	)
	private double inertiaDistribution = 0.5;

	@Configurable(
			comment = "Amount of spin transferred during a redirect. Multiplied to y-part (sidewards) of ballVelocity in robot coordinate system. Effects the redirect angle. 1 keeps full incoming ball velocity, 0 damps is away completely.",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = { "0.8", "0.4", "0.35", "0.4", "0.35", "0.35", "0.8" }
	)
	private double redirectSpinFactor = 0.8;

	@Configurable(
			comment = "Restitution coefficient for redirected balls from a bot. Effects the absolute ball speed. 1 keeps full incoming ball speed, 0 damps is away completely.",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = { "0.2", "0.1", "0.55", "0.1", "0.55", "0.55", "0.2" }
	)
	private double redirectRestitutionCoefficient = 0.2;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for the first hop",
			defValue = "0.8",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" }
	)
	private double chipDampingXYFirstHop = 0.8;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for all following hops",
			defValue = "0.85",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" }
	)
	private double chipDampingXYOtherHops = 0.85;

	@Configurable(
			comment = "Chip kick velocity damping factor in Z direction",
			defValue = "0.47",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" }
	)
	private double chipDampingZ = 0.47;

	@Configurable(
			comment = "If a chipped ball does not reach this height it is considered rolling [mm]",
			defValue = "10.0"
	)
	private double minHopHeight = 10;

	@Configurable(
			comment = "Max. ball height that can be intercepted by robots [mm]",
			defValue = "150.0"
	)
	private double maxInterceptableHeight = 150.0;

	static
	{
		ConfigRegistration.registerClass("geom", BallParameters.class);
	}
}
