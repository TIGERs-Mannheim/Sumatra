package edu.tigers.sumatra.bot.params;

import lombok.Data;


/**
 * Robot kicker specifications.
 */
@Data
public class BotKickerSpecs
{
	private double chipAngle = 45.0;
	private double maxAbsoluteChipVelocity = 8.0;
	private double maxAbsoluteStraightVelocity = 8.0;
}
