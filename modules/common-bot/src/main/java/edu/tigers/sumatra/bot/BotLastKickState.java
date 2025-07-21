/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Kicker and dribbler state during last executed kick.
 */
@Value
@AllArgsConstructor
public class BotLastKickState
{
	/**
	 * True if this was a chip kick
	 */
	boolean chipKick;
	/**
	 * Kick duration in [ms]
	 */
	double kickDuration;
	/**
	 * Dribbling bar surface speed in [m/s]
	 */
	double dribblerSpeed;
	/**
	 * Dribbling bar surface force in [N]
	 */
	double dribblerForce;


	public BotLastKickState()
	{
		chipKick = false;
		kickDuration = 0;
		dribblerSpeed = 0;
		dribblerForce = 0;
	}


	@Override
	public String toString()
	{
		return String.format(
				"%s %.2fms %.2fm/s %.2fN", chipKick ? "CHIP" : "STRAIGHT", kickDuration, dribblerSpeed, dribblerForce);
	}
}
