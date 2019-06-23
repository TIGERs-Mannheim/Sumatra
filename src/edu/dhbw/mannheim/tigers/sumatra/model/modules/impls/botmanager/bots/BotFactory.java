/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 */
public final class BotFactory
{
	private static final Logger	log	= Logger.getLogger(BotFactory.class.getName());
	
	private static final String	TYPE	= "type";
	
	
	private BotFactory()
	{
	}
	
	
	/**
	 * @param config
	 * @return
	 * @throws BotInitException
	 */
	public static ABot createBot(final SubnodeConfiguration config) throws BotInitException
	{
		ABot bot = null;
		EBotType type = EBotType.getTypeFromCfgName(config.getString(TYPE));
		
		switch (type)
		{
			case GRSIM:
				bot = new GrSimBot(config);
				break;
			case TIGER:
				bot = new TigerBot(config);
				break;
			case SUMATRA:
				bot = new SumatraBot(config);
				break;
			default:
				throw new IllegalStateException("Bot type could not be read: " + config.getString(TYPE));
		}
		
		return bot;
	}
	
	
	/**
	 * @param type
	 * @param id
	 * @param name
	 * @return
	 */
	public static ABot createBot(final EBotType type, final BotID id, final String name)
	{
		ABot bot = null;
		
		switch (type)
		{
			case GRSIM:
				bot = new GrSimBot(id);
				break;
			case TIGER:
				bot = new TigerBot(id);
				break;
			default:
				log.error("!! createBot() type was NOT found. Bot remains NULL !!");
				break;
		}
		
		// In case something was typo'ed
		if (bot != null)
		{
			bot.setName(name);
		}
		
		return bot;
	}
}
