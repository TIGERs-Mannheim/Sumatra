package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
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
			comment = "Ball sliding acceleration",
			defValue = "-3000.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = {"-3000.0", "-3000.0", "-3000.0", "-3000.0", "-3000.0", "-3000.0", "-2460.0"},
			unit = EConfigUnit.ACCELERATION_MM
	)
	private double accSlide = -3000.0;

	@Configurable(
			comment = "Ball rolling acceleration",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = {"-260.0", "-260.0", "-260.0", "-260.0", "-260.0", "-260.0", "-290.0"},
			defValue = "-260.0",
			unit = EConfigUnit.ACCELERATION_MM
	)
	private double accRoll = -260.0;

	@Configurable(
			comment = "Fraction of the initial velocity where the ball starts to roll",
			defValue = "0.64",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			unit = EConfigUnit.PERCENTAGE
	)
	private double kSwitch = 0.64;

	@Configurable(
			comment = "Ball inertia distribution between 0.4 (massive sphere) and 0.66 (hollow sphere)",
			defValue = "0.5",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			unit = EConfigUnit.PERCENTAGE
	)
	private double inertiaDistribution = 0.5;

	@Configurable(
			comment = "Amount of spin transferred during a redirect. Multiplied to y-part (sidewards) of ballVelocity in robot coordinate system. Effects the redirect angle. 1 keeps full incoming ball velocity, 0 damps is away completely.",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = { "0.8", "0.4", "0.35", "0.4", "0.35", "0.35", "0.8" },
			defValue = "0.8",
			unit = EConfigUnit.PERCENTAGE
	)
	private double redirectSpinFactor = 0.8;

	@Configurable(
			comment = "Restitution coefficient for redirected balls from a bot. Effects the absolute ball speed. 1 keeps full incoming ball speed, 0 damps is away completely.",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			defValueSpezis = { "0.2", "0.1", "0.55", "0.1", "0.55", "0.55", "0.2" },
			defValue = "0.2",
			unit = EConfigUnit.PERCENTAGE
	)
	private double redirectRestitutionCoefficient = 0.2;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for the first hop",
			defValue = "0.8",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			unit = EConfigUnit.PERCENTAGE
	)
	private double chipDampingXYFirstHop = 0.8;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for all following hops",
			defValue = "0.85",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			unit = EConfigUnit.PERCENTAGE
	)
	private double chipDampingXYOtherHops = 0.85;

	@Configurable(
			comment = "Chip kick velocity damping factor in Z direction",
			defValue = "0.47",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "SIMULATOR" },
			unit = EConfigUnit.PERCENTAGE
	)
	private double chipDampingZ = 0.47;

	@Configurable(
			comment = "If a chipped ball does not reach this height it is considered rolling",
			defValue = "10.0",
			unit = EConfigUnit.DISTANCE_MM
	)
	private double minHopHeight = 10;

	@Configurable(
			comment = "Max. ball height that can be intercepted by robots",
			defValue = "150.0",
			unit = EConfigUnit.DISTANCE_MM
	)
	private double maxInterceptableHeight = 150.0;

	static
	{
		ConfigRegistration.registerClass("geom", BallParameters.class);
	}
}
