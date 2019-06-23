/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.util;

import javax.swing.ImageIcon;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.BotImageRegistry;
import edu.tigers.sumatra.util.ImageIconCache;


/**
 * @author "Lukas Magel"
 */
public class AutoRefImageRegistry
{
	private static final String				blueFlagName	= "/blue_flag.png";
	private static final String				yellowFlagName	= "/yellow_flag.png";
	
	private static final String				blueCardName	= "/blue_card.png";
	private static final String				yellowCardName	= "/yellow_card.png";
	
	private static final String				urgentSignName	= "/urgent.png";
	private static final String				bulbSignName	= "/bulb.png";
	
	private static final String				rightArrowName	= "/right-arrow.png";
	
	private static final ImageIconCache		cache				= ImageIconCache.getGlobalCache();
	private static final BotImageRegistry	botRegistry		= new BotImageRegistry(cache);
	
	
	/**
	 * @param color
	 * @return
	 */
	public static ImageIcon getTeamFlagIcon(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return cache.getImage(blueFlagName);
			case YELLOW:
				return cache.getImage(yellowFlagName);
			default:
				return null;
		}
	}
	
	
	/**
	 * @param color
	 * @return
	 */
	public static ImageIcon getTeamCard(final ETeamColor color)
	{
		switch (color)
		{
			case BLUE:
				return cache.getImage(blueCardName);
			case YELLOW:
				return cache.getImage(yellowCardName);
			default:
				return null;
		}
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	public static ImageIcon getBotIcon(final BotID id)
	{
		return botRegistry.getBotIcon(id);
	}
	
	
	/**
	 * @param eventType
	 * @return
	 */
	public static ImageIcon getEventIcon(final EGameEvent eventType)
	{
		switch (eventType.getCategory())
		{
			case GENERAL:
				return cache.getImage(bulbSignName);
			case VIOLATION:
				return cache.getImage(urgentSignName);
			default:
				return null;
		}
	}
	
	
	/**
	 * @return
	 */
	public static ImageIcon getRightArrow()
	{
		return cache.getImage(rightArrowName);
	}
}
