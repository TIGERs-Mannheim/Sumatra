/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - RCU
 * Date: 20.10.2010
 * Author(s): Lukas
 * 
 * *********************************************************
 */

package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.local;

import java.util.HashMap;
import java.util.Map;

import net.java.games.input.Component;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.AInputDevice;


/**
 * This class turns single componentactions of the controller into a HashMap with all
 * componentactions specified in the currentConfig.
 * 
 * @author Lukas
 * 
 */
public class ControllerInterpreter
{
	
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	private Map<String, String>				currentConfig;
	private final ActionTranslator			translator;
	private final HashMap<String, Double>	newActionMap	= new HashMap<String, Double>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param inputDevice
	 */
	public ControllerInterpreter(AInputDevice inputDevice)
	{
		translator = new ActionTranslator(inputDevice);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param currentConfig
	 */
	public void setCurrentConfig(Map<String, String> currentConfig)
	{
		this.currentConfig = currentConfig;
	}
	
	
	/**
	 * add componentID to newActionMap if contained in currentConfig
	 * @param comp
	 */
	public void interpret(Component comp)
	{
		if (currentConfig.containsKey(comp.getIdentifier().toString()))
		{
			final String command = currentConfig.get(comp.getIdentifier().toString());
			final double value = comp.getPollData();
			newActionMap.put(command, value);
			translateCommandMap();
		}
	}
	
	
	private void translateCommandMap()
	{
		if (newActionMap.size() == currentConfig.size())
		{
			translator.translate(newActionMap);
			newActionMap.clear();
		}
	}
	
	
	/**
	 * Add all componentIDs with value 0.0 that are contained in currentConfig but not yet in newActionMap.
	 */
	public void fillActionMapWithNulls()
	{
		for (final String cmd : currentConfig.values())
		{
			if (!newActionMap.containsKey(cmd))
			{
				newActionMap.put(cmd, 0d);
			}
		}
		translateCommandMap();
	}
}
