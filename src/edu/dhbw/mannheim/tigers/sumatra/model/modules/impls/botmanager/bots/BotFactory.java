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

public class BotFactory
{
	public static ABot createBot(SubnodeConfiguration config) throws BotInitException
	{
		ABot bot = null;

		if (config.getString("type").equals("CtBot"))
		{
			bot = new CtBot(config);
		}
		
		if (config.getString("type").equals("SysoutBot"))
		{
			bot = new SysoutBot(config);
		}
		
		if(config.getString("type").equals("TigerBot"))
		{
			bot = new TigerBot(config);
		}
		
		return bot;
	}
	
	public static ABot createBot(EBotType type, int id, String name)
	{
		ABot bot = null;
		
		switch(type)
		{
			case CT: bot = new CtBot(id); break;
			case TIGER: bot = new TigerBot(id); break;
			case SYSOUT: bot = new SysoutBot(id); break;
		}
		
		bot.setName(name);
		
		return bot;
	}
}
