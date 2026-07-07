package edu.tigers.sumatra.botparams;

import edu.tigers.sumatra.bot.params.BotParams;


public class NoBotParamsProvider implements BotParamsProvider
{
	@Override
	public BotParams get(final EBotParamLabel label)
	{
		return new BotParams();
	}
}
