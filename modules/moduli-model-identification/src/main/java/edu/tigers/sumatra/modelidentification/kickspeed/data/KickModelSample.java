/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.modelidentification.kickspeed.data;

import edu.tigers.sumatra.bot.BotLastKickState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


/**
 * Sample for kick model identification.
 */
@Data
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor
public class KickModelSample implements Sample
{
	private final long kickTimestamp;
	private final String botId;
	/**
	 * [m/s]
	 */
	private final double measuredKickVel;
	/**
	 * [m/s], this may be null if a kick with ARM_TIME was commanded.
	 */
	private final Double cmdKickVel;
	private final double cmdDribbleSpeed;
	private final double cmdDribbleForce;
	private final boolean chip;
	private final BotLastKickState botStateAtKick;
	@Builder.Default
	private boolean sampleUsed = true;


	@SuppressWarnings("unused") // Required for jackson binding
	protected KickModelSample()
	{
		kickTimestamp = 0;
		botId = "none";
		measuredKickVel = 0;
		cmdKickVel = null;
		cmdDribbleSpeed = 0;
		cmdDribbleForce = 0;
		botStateAtKick = new BotLastKickState();
		chip = false;
	}
}
