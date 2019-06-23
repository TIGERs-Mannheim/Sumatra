/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This class defines what features are available for each single bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @note IDs correspond to TigerBotV3 match feedback bitfield, do not change!
 */
public enum EFeature
{
	/**  */
	MOVE("Move", 0x0001),
	/**  */
	DRIBBLER("Dribbler", 0x0002),
	/**  */
	STRAIGHT_KICKER("Straight Kicker", 0x0004),
	/**  */
	CHIP_KICKER("Chip Kicker", 0x0008),
	/**  */
	BARRIER("Barrier", 0x0010),
	/** */
	V2016("v2016", 0x0020),
	/** */
	EXT_BOARD("Ext. Board", 0x0040);
	
	private final String	name;
	private final int		id;
	
	
	private EFeature(final String name, final int id)
	{
		this.name = name;
		this.id = id;
	}
	
	
	private EFeature(final String name, final int id, final String desc)
	{
		this.name = name;
		this.id = id;
	}
	
	
	/**
	 * Create new feature list with all available features
	 * 
	 * @return
	 */
	public static Map<EFeature, EFeatureState> createFeatureList()
	{
		Map<EFeature, EFeatureState> map = new LinkedHashMap<EFeature, EFeatureState>();
		for (EFeature f : EFeature.values())
		{
			map.put(f, EFeatureState.UNKNOWN);
		}
		return map;
	}
	
	
	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}
	
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * Convert an id to an enum.
	 * 
	 * @param id
	 * @return enum
	 */
	public static EFeature getFeatureConstant(final int id)
	{
		for (EFeature s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}
		
		return null;
	}
	
}
