/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot;

import java.util.HashMap;
import java.util.Map;


/**
 * The different available bot types in Sumatra.
 *
 * @author AndreR
 */
public enum EBotType
{
	/** */
	UNKNOWN(0, "Unknown", "Unknown Bot"),
	/** */
	GRSIM(1, "grSim", "grSim Bot"),
	/**  */
	TIGER_V3(4, "TigerBotV3", "Tiger Bot v2013/v2016"),
	/**  */
	SUMATRA(5, "sumatra", "SumatraBot"),
	/**  */
	SHARED_RADIO(7, "sharedRadio", "SharedRadio Bot");
	
	private final String	cfgName;
	private final String	displayName;
	private final int		versionId;
	
	
	EBotType(final int versionId, final String cfgName, final String displayName)
	{
		this.versionId = versionId;
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
		final Map<String, EBotType> types = new HashMap<>();
		
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
	public static EBotType getTypeFromCfgName(final String cfgName)
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
	
	
	/**
	 * At least same version as given type?
	 *
	 * @param type
	 * @return
	 */
	public boolean atLeast(final EBotType type)
	{
		return type.versionId <= versionId;
	}
}
