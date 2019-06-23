/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.07.2014
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2;

import java.util.EnumMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECommand;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData;
import edu.dhbw.mannheim.tigers.sumatra.util.serial.SerialData.ESerialDataType;


/**
 * Set and receive features and their state from bot.
 * 
 * @author AndreR
 */
public class TigerSystemFeatures extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@SerialData(type = ESerialDataType.UINT8)
	private int[]	features	= new int[EFeature.values().length];
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerSystemFeatures()
	{
		super(ECommand.CMD_SYSTEM_FEATURES, true);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set the features, which are then encoded in the byte stream
	 * 
	 * @param newFeatures
	 */
	public void setFeatures(final Map<EFeature, EFeatureState> newFeatures)
	{
		for (EFeature f : newFeatures.keySet())
		{
			int id = f.getId();
			if (id >= features.length)
			{
				continue;
			}
			
			features[id] = newFeatures.get(f).getId();
		}
	}
	
	
	/**
	 * Get the features encoded in the byte stream.
	 * 
	 * @return A map with features and their states
	 */
	public Map<EFeature, EFeatureState> getFeatures()
	{
		Map<EFeature, EFeatureState> map = new EnumMap<EFeature, EFeatureState>(EFeature.class);
		
		for (int i = 0; i < features.length; i++)
		{
			EFeature f = EFeature.getFeatureConstant(i);
			if (f == null)
			{
				continue;
			}
			
			map.put(f, EFeatureState.getFeatureStateConstant(features[i]));
		}
		
		return map;
	}
}
