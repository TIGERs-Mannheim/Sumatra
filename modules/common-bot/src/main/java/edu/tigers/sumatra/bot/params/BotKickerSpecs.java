/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;

import lombok.Data;


/**
 * Robot kicker specifications.
 */
@Data
public class BotKickerSpecs implements IBotKickerSpecs
{
	private double chipAngle = 45.0;
	private double maxAbsoluteChipVelocity = 8.0;
	private double maxAbsoluteStraightVelocity = 8.0;
}
