/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam.data;

import lombok.Value;


@Value
public class CamBallModels
{
	StraightTwoPhase straightTwoPhase;
	ChipFixedLoss chipFixedLoss;

	@Value
	public static class StraightTwoPhase
	{
		double accSlide;
		double accRoll;
		double kSwitch;
	}

	@Value
	public static class ChipFixedLoss
	{
		double dampingXyFirstHop;
		double dampingXyOtherHops;
		double dampingZ;
	}
}
