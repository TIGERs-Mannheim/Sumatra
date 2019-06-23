/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botparams;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;


public class NoBotParamsProvider implements BotParamsProvider
{
	@Override
	public IBotParams get(final EBotParamLabel label)
	{
		return new BotParams();
	}
}
