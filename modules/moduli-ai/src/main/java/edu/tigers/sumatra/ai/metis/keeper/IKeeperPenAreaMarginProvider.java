/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

public interface IKeeperPenAreaMarginProvider
{
	double atBorder();

	double atGoal();

	default IKeeperPenAreaMarginProvider withExtraMargin(double extraMargin)
	{
		var original = this;
		return new IKeeperPenAreaMarginProvider()
		{
			@Override
			public double atBorder()
			{
				return original.atBorder() + extraMargin;
			}


			@Override
			public double atGoal()
			{
				return original.atGoal() + extraMargin;
			}
		};
	}
}
