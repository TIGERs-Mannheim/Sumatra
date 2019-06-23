/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Hashtable;
import java.util.Map;


/**
 */
public enum EBotType
{
	/** */
	UNKNOWN("Unknown", "Unknown Bot"),
	/** */
	TIGER("TigerBot", "Tiger Bot"),
	/** */
	TIGER_V2("TigerBotV2", "Tiger Bot v2013"),
	/** */
	GRSIM("grSim", "grSim Bot");
	
	private String	cfgName;
	private String	displayName;
	
	
	private EBotType(String cfgName, String displayName)
	{
		this.cfgName = cfgName;
		this.displayName = displayName;
	}
	
	
	/**
	 * Generate a map with bot type and display name.
	 * 
	 * @return
	 */
	public static Map<String, EBotType> getBotTypeMap()
	{
		final Map<String, EBotType> types = new Hashtable<String, EBotType>();
		
		for (EBotType t : EBotType.values())
		{
			types.put(t.displayName, t);
		}
		
		return types;
	}
	
	
	/**
	 * Get an EBotType from a cfgName string.
	 * 
	 * @param cfgName
	 * @return
	 */
	public static EBotType getTypeFromCfgName(String cfgName)
	{
		for (EBotType t : EBotType.values())
		{
			if (t.cfgName.equals(cfgName))
			{
				return t;
			}
		}
		
		return UNKNOWN;
	}
	
	
	/**
	 * @return the cfgName
	 */
	public String getCfgName()
	{
		return cfgName;
	}
	
	
	/**
	 * @return the displayName
	 */
	public String getDisplayName()
	{
		return displayName;
	}
}
