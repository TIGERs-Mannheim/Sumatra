/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;


import edu.tigers.sumatra.math.AngleMath;


public class RotationTimeHelper
{
	private RotationTimeHelper()
	{
		// hide default constructor
	}

	public static double calcRotationTime(double v0, double s0, double s2, double vMax, double aMax)
	{
		var s = AngleMath.difference(s2, s0);

		var v1 = (s >= 0) ? vMax : -vMax;
		var a = (v0 < v1) ? aMax : -aMax;

		var t1 = (v1 - v0) / a;
		var s1 = 0.5 * (v0 + v1) * t1;

		if ((s > 0) == (s1 > s))
		{
			var sqrt = Math.sqrt(2 * a * s + v0 * v0);
			var ta = -(sqrt + v0) / a;
			var tb = (sqrt - v0) / a;
			return Math.max(ta, tb);
		}

		return t1 + Math.abs((s - s1) / v1);
	}
}
