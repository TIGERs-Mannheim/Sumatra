/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Additional bot skill input data.
 */
public record BotSkillInput(
		ABotSkill skill,
		SimBotState state,
		IBotParams botParams,
		long tNow,
		boolean strictVelocityLimit
)
{
}
