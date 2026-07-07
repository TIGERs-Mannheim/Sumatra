package edu.tigers.sumatra.botmanager.sim.skills;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.data.MatchBroadcast;
import edu.tigers.sumatra.sim.dynamics.bot.SimBotState;


/**
 * Additional bot skill input data.
 */
public record BotSkillInput(
		ABotSkill skill,
		SimBotState state,
		BotParams botParams,
		long tNow,
		MatchBroadcast broadcast
)
{
}
