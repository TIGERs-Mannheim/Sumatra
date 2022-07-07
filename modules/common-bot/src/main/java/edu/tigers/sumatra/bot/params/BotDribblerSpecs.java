/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;
import lombok.Data;


/**
 * Robot dribbler specifications.
 */
@Persistent
@Data
public class BotDribblerSpecs implements IBotDribblerSpecs
{
	private double maxBallAcceleration = 5.0;
	private double maxRetainingBallAngle = 0.75;
	private double defaultSpeed = 15000;
	private double defaultMaxCurrent = 3.0;
	private double highPowerSpeed = 22000;
	private double highPowerMaxCurrent = 5.0;
}
