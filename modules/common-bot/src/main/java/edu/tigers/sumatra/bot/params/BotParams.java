package edu.tigers.sumatra.bot.params;

import lombok.Getter;
import lombok.Setter;


/**
 * Data holder for all parameters of a robot.
 * Includes movement limits and physical properties.
 */
@Getter
public class BotParams
{
	@Setter
	private BotMovementLimits movementLimits = BotMovementLimits.ZERO;
	private final BotDimensions dimensions = new BotDimensions();
	private final BotKickerSpecs kickerSpecs = new BotKickerSpecs();
	private final BotDribblerSpecs dribblerSpecs = new BotDribblerSpecs();
}
