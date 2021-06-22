/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;


/**
 * All parameters required for ball models.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class BallParameters
{
	@Configurable(
			comment = "Ball sliding acceleration [mm/s^2]",
			defValue = "-3000.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double accSlide = -3000.0;

	@Configurable(
			comment = "Ball rolling acceleration [mm/s^2]",
			defValue = "-260.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double accRoll = -260.0;

	@Configurable(
			comment = "Fraction of the initial velocity where the ball starts to roll",
			defValue = "0.64",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double kSwitch = 0.64;

	@Configurable(
			comment = "Ball inertia distribution between 0.4 (massive sphere) and 0.66 (hollow sphere)",
			defValue = "0.5",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double inertiaDistribution = 0.5;

	@Configurable(
			comment = "Amount of spin transferred during a redirect.",
			defValue = "0.8",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double redirectSpinFactor = 0.8;

	@Configurable(
			comment = "Restitution coefficient for redirected balls from a bot.",
			defValue = "0.2",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double redirectRestitutionCoefficient = 0.2;

	@Configurable(
			comment = "Fixed velocity where the ball starts to roll [mm/s]",
			defValue = "2000.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double vSwitch = 2000.0;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for the first hop",
			defValue = "0.75",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double chipDampingXYFirstHop = 0.75;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for all following hops",
			defValue = "0.95",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double chipDampingXYOtherHops = 0.95;

	@Configurable(
			comment = "Chip kick velocity damping factor in Z direction",
			defValue = "0.5",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI", "SIMULATOR" }
	)
	private double chipDampingZ = 0.5;

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
