/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;


/**
 * This factory can build bot presenter, depending on the bot type.
 * 
 * @author AndreR
 * 
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
	public static ABotPresenter createBotPresenter(ABot bot)
	{
		ABotPresenter presenter = null;
		
		switch (bot.getType())
		{
			case GRSIM:
			case TIGER:
			{
				presenter = new TigerBotPresenter(bot);
			}
				break;
			case TIGER_V2:
			{
				presenter = new TigerBotV2Presenter(bot);
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
