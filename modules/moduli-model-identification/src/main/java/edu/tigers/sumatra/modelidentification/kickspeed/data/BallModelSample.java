/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.modelidentification.kickspeed.data;

import edu.tigers.sumatra.bot.BotLastKickState;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;


/**
 * Sample for ball model identification.
 */
@Data
@RequiredArgsConstructor
public class BallModelSample implements Sample
{
	private final EBallModelIdentType type;
	private final long timestamp;
	private final String botId;
	private final Map<String, Double> parameters;
	private final BotLastKickState kickState;
	private boolean sampleUsed = true;


	@SuppressWarnings("unused") // Required for jackson binding
	protected BallModelSample()
	{
		type = EBallModelIdentType.STRAIGHT_TWO_PHASE;
		parameters = null;
		timestamp = 0;
		botId = "";
		kickState = new BotLastKickState();
	}
}
