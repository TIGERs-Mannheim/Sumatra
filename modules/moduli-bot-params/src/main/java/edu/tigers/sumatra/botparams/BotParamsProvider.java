/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botparams;

import edu.tigers.sumatra.bot.params.IBotParams;


public interface BotParamsProvider
{
	/**
	 * Get robot parameters for a specific label.
	 *
	 * @param label
	 * @return
	 */
	IBotParams get(final EBotParamLabel label);
}
