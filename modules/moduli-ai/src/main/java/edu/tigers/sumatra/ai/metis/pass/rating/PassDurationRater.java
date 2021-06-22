/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.math.SumatraMath;


public class PassDurationRater implements IPassRater
{
	@Configurable(comment = "[s] factor for passRating: the longer the pass the worse the rating gets", defValue = "1.2")
	private static double optimalPassDuration = 1.2;

	static
	{
		ConfigRegistration.registerClass("metis", RatedPassFactory.class);
	}

	@Override
	public double rate(Pass pass)
	{
		double diff = Math.abs(optimalPassDuration - pass.getDuration());
		return 1 - SumatraMath.relative(diff, 0, optimalPassDuration);
	}
}
