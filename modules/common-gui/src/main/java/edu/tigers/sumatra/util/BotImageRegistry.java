/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 11, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import javax.swing.ImageIcon;

import edu.tigers.sumatra.ids.BotID;


/**
 * Utility class that loads bot images from the classpath and stores scaled versions
 * 
 * @author "Lukas Magel"
 */
public class BotImageRegistry
{
	private static final String	prefix			= "/bots/";
	private static final String	yellowSuffix	= "Yellow";
	private static final String	blueSuffix		= "Blue";
	private static final String	extension		= ".png";
	
	private ImageIconCache			cache;
	
	
	/**
	 * 
	 */
	public BotImageRegistry()
	{
		cache = new ImageIconCache();
	}
	
	
	/**
	 * @param cache Cache to use for retrieving and storing the images
	 */
	public BotImageRegistry(final ImageIconCache cache)
	{
		this.cache = cache;
	}
	
	
	/**
	 * @param id
	 * @return the image or null if either the image file could not be found or the id is uninitialized
	 */
	public ImageIcon getBotIcon(final BotID id)
	{
		if (id.isUninitializedID())
		{
			return null;
		}
		String name = buildIconName(id);
		return cache.getImage(name);
	}
	
	
	private String buildIconName(final BotID id)
	{
		StringBuilder path = new StringBuilder();
		path.append(prefix);
		switch (id.getTeamColor())
		{
			case BLUE:
				path.append(blueSuffix);
				break;
			case YELLOW:
				path.append(yellowSuffix);
				break;
			default:
				return null;
		}
		
		path.append(id.getNumber());
		path.append(extension);
		
		return path.toString();
	}
}
