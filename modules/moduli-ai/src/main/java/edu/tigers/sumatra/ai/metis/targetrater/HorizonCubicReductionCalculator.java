/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


public class HorizonCubicReductionCalculator
{
	@Configurable(defValue = "0.02")
	private static double cubicComponent = 0.02;

	static
	{
		ConfigRegistration.registerClass("metis", HorizonCubicReductionCalculator.class);
	}

	public double reduceHorizon(double horizon)
	{
		if (horizon < 0)
		{
			return 0;
		}

		double border = cubicFunctionMax();
		double yBorder = cubicFunction(border);
		if (horizon < border)
		{
			return cubicFunction(horizon);
		}

		// at x = border the cubic function starts to decrease again. Those high horizons are pretty irrelevant,
		// for cubicComponent = 0.02 the border is 4 seconds, which is beyond any reasonable interception calculation.
		// However, we still want the function to increase steadily at the end. Thus, we switch to a linearly increasing
		// function at this point.
		return yBorder + (horizon - border) * 0.25;
	}


	private double cubicFunctionMax()
	{
		// f(x) = 0.95 * x - cubicComponent * x^3
		// f’(x) = 0.95 - cubicComponent * 3x^2
		// 0 = cubicComponent * 3x^2 - 0.95, solve for positive x1/2
		return Math.sqrt(4 * 3 * cubicComponent * 0.95) / (2 * 3 * cubicComponent);
	}


	private double cubicFunction(double horizon)
	{
		/*
		 * This is a cubic function that will cause the horizon grows slower for higher input numbers
		 * horizon, starts with slope of 1 at 0 and decreases slowly.
		 * |                                                          _
		 * |                                             _
		 * |                                  _
		 * |                       _
		 * |             _
		 * |       _
		 * |    _
		 * |  _
		 * | _
		 * |_
		 * --------------------------------------------------------------> x
		 */
		return 0.95 * horizon - cubicComponent * horizon * horizon * horizon;
	}
}
