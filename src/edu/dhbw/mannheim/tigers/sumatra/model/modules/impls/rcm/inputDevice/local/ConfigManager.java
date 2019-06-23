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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.controller.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.AControllerPresenter;


/**
 * This class manages saving and loading of configs.
 * 
 * @author Lukas
 * 
 */

public final class ConfigManager
{
	
	// --------------------------------------------------------------------------
	// --- class variables ------------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log								= Logger.getLogger(ConfigManager.class
																											.getName());
	
	private static ConfigManager						instance							= null;
	private static final HashMap<String, String>	defaultGamePadConfigMap		= new HashMap<String, String>();
	private static final HashMap<String, String>	defaultKeyboardConfigMap	= new HashMap<String, String>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	private ConfigManager()
	{
		setDefaultConfig();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public static ConfigManager getInstance()
	{
		if (instance == null)
		{
			instance = new ConfigManager();
		}
		return instance;
	}
	
	
	private void setDefaultConfig()
	{
		defaultGamePadConfigMap.clear();
		defaultKeyboardConfigMap.clear();
		
		defaultGamePadConfigMap.put("-y", "forward");
		defaultGamePadConfigMap.put("y", "backward");
		defaultGamePadConfigMap.put("-x", "left");
		defaultGamePadConfigMap.put("x", "right");
		defaultGamePadConfigMap.put("z", "rotateLeft");
		defaultGamePadConfigMap.put("-z", "rotateRight");
		defaultGamePadConfigMap.put("1", "force");
		defaultGamePadConfigMap.put("4", "pass");
		defaultGamePadConfigMap.put("2", "chipKick");
		defaultGamePadConfigMap.put("5", "dribble");
		defaultGamePadConfigMap.put("0", "arm");
		defaultGamePadConfigMap.put("3", "disarm");
		defaultGamePadConfigMap.put("6", "chipArm");
		
		defaultKeyboardConfigMap.put("W", "forward");
		defaultKeyboardConfigMap.put("S", "backward");
		defaultKeyboardConfigMap.put("A", "left");
		defaultKeyboardConfigMap.put("D", "right");
		defaultKeyboardConfigMap.put("Q", "rotateLeft");
		defaultKeyboardConfigMap.put("E", "rotateRight");
		defaultKeyboardConfigMap.put("L", "force");
		defaultKeyboardConfigMap.put("K", "pass");
		defaultKeyboardConfigMap.put("J", "chipKick");
		defaultKeyboardConfigMap.put("U", "dribble");
		defaultKeyboardConfigMap.put("O", "arm");
		defaultKeyboardConfigMap.put("P", "disarm");
		defaultKeyboardConfigMap.put("I", "chipArm");
	}
	
	
	/**
	 * @param file
	 * @param map
	 */
	public void saveConfig(File file, Map<String, String> map)
	{
		try
		{
			file.createNewFile();
			final ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(map);
			output.flush();
			output.close();
		} catch (final IOException e)
		{
			log.error("IOException", e);
		}
		log.info("File " + file + " saved.");
	}
	
	
	/**
	 * @param file
	 * @return
	 */
	public Map<String, String> loadConfig(File file)
	{
		try
		{
			final ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			// actually, the config could also be from a keyboard...
			final Map<String, String> map = new HashMap<String, String>();
			
			// its hard to check for type safety here...
			@SuppressWarnings("unchecked")
			final Map<String, String> readMap = (Map<String, String>) input.readObject();
			map.putAll(readMap);
			input.close();
			return map;
		} catch (final FileNotFoundException e)
		{
			log.error("FileNotFoundException", e);
		} catch (final IOException e)
		{
			log.error("IOException", e);
		} catch (final ClassNotFoundException e)
		{
			log.error("ClassNotFoundException", e);
		}
		return null;
	}
	
	
	/**
	 * @param controllerPresenter
	 * @return
	 */
	public HashMap<String, String> loadDefaultConfig(AControllerPresenter controllerPresenter)
	{
		if (controllerPresenter.getType() == EControllerType.KEYBOARD)
		{
			return defaultKeyboardConfigMap;
		} else if (controllerPresenter.getType() == EControllerType.GAMEPAD)
		{
			return defaultGamePadConfigMap;
		} else if (controllerPresenter.getType() == EControllerType.STICK)
		{
			return defaultGamePadConfigMap;
		}
		return null;
	}
}
