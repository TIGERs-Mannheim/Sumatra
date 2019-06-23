/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 17, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.EnumMap;
import java.util.Map;


/**
 * This class defines what features are available for each single bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public enum EFeature
{
	/**  */
	CHIP_KICKER("Chip Kicker"),
	/**  */
	STRAIGHT_KICKER("Straight Kicker"),
	/**  */
	DRIBBLER("Dribbler"),
	/**  */
	MOVE("Move"),
	/**  */
	BARRIER("Barrier", "Limited: barrier feedback to Sumatra not working, Kaput: Not working at all");
	
	private final String	name;
	private final String	desc;
	
	/**
	 */
	public enum EFeatureState
	{
		/**  */
		WORKING,
		/**  */
		LIMITED,
		/**  */
		KAPUT,
		/**  */
		UNKNOWN;
	}
	
	
	private EFeature(String name)
	{
		this.name = name;
		desc = "";
	}
	
	
	private EFeature(String name, String desc)
	{
		this.name = name;
		this.desc = desc;
	}
	
	
	/**
	 * Create new feature list with all available features
	 * 
	 * @return
	 */
	public static Map<EFeature, EFeatureState> createFeatureList()
	{
		Map<EFeature, EFeatureState> map = new EnumMap<EFeature, EFeatureState>(EFeature.class);
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
	 * @return the desc
	 */
	public final String getDesc()
	{
		return desc;
	}
	
}
