/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.SimBot;


/**
 * This factory can build bot presenter, depending on the bot type.
 * 
 * @author AndreR
 */
public final class BotPresenterFactory
{
	private BotPresenterFactory()
	{
		
	}
	
	
	/**
	 * @param bot
	 * @return
	 */
	public static ABotPresenter createBotPresenter(final ABot bot)
	{
		ABotPresenter presenter = null;
		
		switch (bot.getType())
		{
			case GRSIM:
			case SUMATRA:
			{
				presenter = new SimBotPresenter((SimBot) bot);
			}
				break;
			case TIGER:
			{
				presenter = new TigerBotPresenter(bot);
			}
				break;
			case UNKNOWN:
				throw new IllegalStateException("bot type is unknown");
			default:
				break;
		}
		
		return presenter;
	}
}
