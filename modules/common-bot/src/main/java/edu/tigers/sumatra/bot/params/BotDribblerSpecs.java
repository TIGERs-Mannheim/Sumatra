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
	private double defaultSpeed = 4;
	private double defaultForce = 3.0;
	private double highPowerSpeed = 5;
	private double highPowerForce = 5.0;
}
