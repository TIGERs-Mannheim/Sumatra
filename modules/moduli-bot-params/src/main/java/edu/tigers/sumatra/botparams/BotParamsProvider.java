package edu.tigers.sumatra.botparams;

import edu.tigers.sumatra.bot.params.BotParams;


public interface BotParamsProvider
{
	/**
	 * Get robot parameters for a specific label.
	 *
	 * @param label
	 * @return
	 */
	BotParams get(final EBotParamLabel label);
}
