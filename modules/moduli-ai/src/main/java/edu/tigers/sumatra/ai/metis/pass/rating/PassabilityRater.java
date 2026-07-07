package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.math.SumatraMath;


/**
 * Rate the passibility based on the initial kick speed and the receiving speed.
 */
public class PassabilityRater implements IPassRater
{
	@Configurable(defValue = "1.0", comment = "If the kick speed is lower than this, the score is 0", unit = EConfigUnit.VELOCITY_M)
	private static double minPassSpeed = 1.0;

	@Configurable(defValue = "0.0", comment = "The receiving speed that gives the lowest score", unit = EConfigUnit.VELOCITY_M)
	private static double worstPassReceivingSpeed = 0.0;

	@Configurable(defValue = "2.0", comment = "The receiving speed that gives the highest score", unit = EConfigUnit.VELOCITY_M)
	private static double bestPassReceivingSpeed = 2.0;

	static
	{
		ConfigRegistration.registerClass("metis", RatedPassFactory.class);
	}

	@Override
	public double rate(Pass pass)
	{
		if (pass.getKick().getKickParams().getKickSpeed() < minPassSpeed)
		{
			return 0;
		}

		return SumatraMath.relative(pass.getReceivingBallSpeed(), worstPassReceivingSpeed, bestPassReceivingSpeed);
	}
}
